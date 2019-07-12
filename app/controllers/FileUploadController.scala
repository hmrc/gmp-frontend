/*
 * Copyright 2019 HM Revenue & Customs
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
import connectors.AttachmentsConnector
import controllers.auth.{AuthAction, GmpAuthConnector, GmpRegime}
import models.{CallBackData, GmpBulkSession}
import play.api.Play.current
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.mvc.Action
import services.SessionService
import uk.gov.hmrc.http.logging.SessionId
import uk.gov.hmrc.play.frontend.auth.Actions
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

@Singleton
class FileUploadController @Inject()(authAction: AuthAction,
                                     val authConnector: GmpAuthConnector,
                                     sessionService: SessionService,
                                     attachmentsConnector: AttachmentsConnector) extends GmpController {

  def get = authAction.async {
      implicit request =>
          attachmentsConnector.getFileUploadPartial().map {
            partial => Ok(views.html.upload_file(partial.successfulContentOrEmpty))
          }
  }

  def failure() = authAction {
      implicit request =>
        request.getQueryString("error_message") match {
          case Some(x) if x.toUpperCase.contains("VIRUS") => Ok(views.html.failure(Messages("gmp.bulk.failure.antivirus"),Messages("gmp.bulk.problem.header"),Messages("gmp.bulk_failure_antivirus.title")))
          case Some(x) if x.toUpperCase.contains("SELECT") => Ok(views.html.failure(Messages("gmp.bulk.failure.missing"),Messages("gmp.bulk.problem.header"),Messages("gmp.bulk_failure_missing.title")))
          case _ => Ok(views.html.failure(Messages("gmp.bulk.failure.generic"),Messages("gmp.bulk.problem.header"),Messages("gmp.bulk_failure_generic.title")))
        }
  }

  def callback() = Action.async(parse.json) {

    implicit request => {

      val callBackData: CallBackData = request.body.as[CallBackData]

      val result = sessionService.cacheCallBackData(Some(callBackData))(request,
        callBackData.sessionId match {
          case sid:String if !sid.isEmpty => hc.copy(sessionId = Some(SessionId(sid)))
          case _ => hc
        }
      )
      result.map {
        case callback: Option[GmpBulkSession] if callback.isDefined => Ok
        case _ => throw new RuntimeException
      }
    }
  }
}
