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
import models.GmpBulkSession
import models.upscan._
import models.upscan.UploadStatus
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
        _ <- sessionService.resetGmpBulkSession()
        response <- upscanService.getUpscanFormData()
        _ <- sessionService.createCallbackRecord
      } yield {
        Ok(upscan_csv_file_upload(response))
      }
  }

  def showResult(): Action[AnyContent] = Action.async { implicit request =>
    val sessionId = hc.sessionId
    for {
      uploadResult <- sessionService.getCallbackRecord
    } yield {
          uploadResult match {
            case Some(result: UploadStatus) => Ok(views.html.upload_result(result))
            case None => BadRequest(s"Upload with session id ${sessionId.getOrElse("-")} not found")
          }
      }
  }

  def failure(errorCode: String, errorMessage: String, errorRequestId: String) = authAction {
    implicit request =>
      errorCode match {
        case a if a.toUpperCase.contains("INVALIDTEXTENCODING") =>
          Ok(views.html.failure(Messages("gmp.bulk.incorrectlyEncoded"),
            Messages("gmp.bulk.incorrectlyEncoded.header"),
            Messages("gmp.bulk_failure_generic.title")))
        case x if x.toUpperCase.contains("EMPTYREQUESTBODY") =>
          Ok(views.html.failure(Messages("gmp.bulk.failure.missing"),
            Messages("gmp.bulk.problem.header"),
            Messages("gmp.bulk_failure_missing.title")))
        case _ =>
          Ok(views.html.failure(Messages("gmp.bulk.failure.generic"),
            Messages("gmp.bulk.problem.header"),
            Messages("gmp.bulk_failure_generic.title")))
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
            UploadedSuccessfully(callback.reference, callback.uploadDetails.fileName, callback.downloadUrl.toExternalForm)
          case UpscanFailedCallback(ref, details) =>
            Logger.error(s"Callback for session id: $sessionId failed. Reason: ${details.failureReason}. Message: ${details.message}")
            UploadedFailed(ref, details)
        }
        Logger.info(s"Updating callback for session: $sessionId to ${uploadStatus.getClass.getSimpleName}")
        for {
          result <- sessionService.updateCallbackRecord(sessionId, uploadStatus)(request, headerCarrier).map(_ => Ok("")).recover {
            case e: Throwable =>
              Logger.error(s"Failed to update callback record for session: $sessionId, timestamp: ${System.currentTimeMillis()}.", e)
              InternalServerError("Exception occurred when attempting to update callback data")
          }
          _ <- sessionService.cacheCallBackData(Some(uploadStatus))(request, headerCarrier).map(_ => Ok("")).recover {
              case e: Throwable =>
                Logger.error(s"Failed to update gmp bulk session for: $sessionId, timestamp: ${System.currentTimeMillis()}.", e)
                InternalServerError("Exception occurred when attempting to update gmp bulk session")
            }

        } yield {
          result
        }
      }
    )
  }

}
