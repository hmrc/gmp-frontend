/*
 * Copyright 2020 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers

import com.google.inject.{Inject, Singleton}
import config.{ApplicationConfig, GmpContext, GmpSessionCache}
import controllers.auth.AuthAction
import models.upscan._
import play.api.Logger
import play.api.i18n.Messages
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{SessionService, UpscanService}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.logging.SessionId
import views.html.upscan_csv_file_upload

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FileUploadController @Inject()(authAction: AuthAction,
                                     val authConnector: AuthConnector,
                                     sessionService: SessionService,
                                     implicit val config: GmpContext,
                                     upscanService: UpscanService,
                                     messagesControllerComponents: MessagesControllerComponents,
                                     ac: ApplicationConfig,
                                     implicit val executionContext: ExecutionContext,
                                     implicit val gmpSessionCache: GmpSessionCache)
  extends GmpController(messagesControllerComponents, ac, sessionService, config) {


  def get = authAction.async {
    implicit request =>
      for {
        response <- upscanService.getUpscanFormData()
        _ <- sessionService.createCallbackRecord
      } yield {
        Ok(upscan_csv_file_upload(response))
      }
  }

  def failure() = authAction {
    implicit request =>
      request.getQueryString("error_message") match {
        case Some(x) if x.toUpperCase.contains("VIRUS") => Ok(views.html.failure(Messages("gmp.bulk.failure.antivirus"), Messages("gmp.bulk.problem.header"), Messages("gmp.bulk_failure_antivirus.title")))
        case Some(x) if x.toUpperCase.contains("SELECT") => Ok(views.html.failure(Messages("gmp.bulk.failure.missing"), Messages("gmp.bulk.problem.header"), Messages("gmp.bulk_failure_missing.title")))
        case _ => Ok(views.html.failure(Messages("gmp.bulk.failure.generic"), Messages("gmp.bulk.problem.header"), Messages("gmp.bulk_failure_generic.title")))
      }
  }

  def callback(sessionId: String) = Action.async(parse.json) { implicit request =>
    implicit val headerCarrier: HeaderCarrier = hc.copy(sessionId = Some(SessionId(sessionId)))
    request.body.validate[UpscanCallback].fold(
      invalid = errors => {
        Logger.error(s"Failed to validate UpscanCallback json with errors: $errors")
        Future.successful(BadRequest(""))
      },
      valid = callback => {
        val uploadStatus = callback match {
          case callback: UpscanReadyCallback =>
            UploadedSuccessfully(callback.uploadDetails.fileName, callback.downloadUrl.toExternalForm)
          case UpscanFailedCallback(_, details) =>
            Logger.error(s"Callback for session id: $sessionId failed. Reason: ${details.failureReason}. Message: ${details.message}")
            Failed
        }
        Logger.info(s"Updating callback for session: $sessionId to ${uploadStatus.getClass.getSimpleName}")
        for {
          result <- sessionService.updateCallbackRecord(sessionId, uploadStatus)(request, headerCarrier).map(_ => Ok("")).recover {
            case e: Throwable =>
              Logger.error(s"Failed to update callback record for session: $sessionId, timestamp: ${System.currentTimeMillis()}.", e)
             InternalServerError("Exception occurred when attempting to update callback data")
          }
          callBackData = callback.asInstanceOf[UpscanReadyCallback]
          cacheResult <- sessionService.cacheCallBackData(Some(callBackData))(request, headerCarrier).map(_ => Ok("")).recover {
            case e: Throwable =>
              Logger.error(s"Failed to update gmp bulk session for: $sessionId, timestamp: ${System.currentTimeMillis()}.", e)
              InternalServerError("Exception occurred when attempting to update gmp bulk session")
          }
        } yield {
          cacheResult
        }
      }
    )
  }

}
