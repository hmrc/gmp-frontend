/*
 * Copyright 2016 HM Revenue & Customs
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

import config.GmpFrontendAuthConnector
import connectors.AttachmentsConnector
import controllers.auth.GmpRegime
import models.{GmpBulkSession, CallBackData}
import play.api.libs.json.{JsString, Json}
import play.api.mvc.Action
import services.SessionService
import uk.gov.hmrc.play.frontend.auth.Actions
import uk.gov.hmrc.play.http.logging.SessionId

import scala.concurrent.Future

object FileUploadController extends FileUploadController {
  val authConnector = GmpFrontendAuthConnector
  override val attachmentsConnector = AttachmentsConnector
  override val sessionService = SessionService
}

trait FileUploadController extends GmpController with Actions {

  val attachmentsConnector: AttachmentsConnector
  val sessionService: SessionService

  def get = AuthorisedFor(GmpRegime, pageVisibilityPredicate).async {

    implicit user =>
      implicit request =>
          attachmentsConnector.getFileUploadPartial().map {
            partial => Ok(views.html.upload_file(partial.successfulContentOrEmpty))
          }

  }

  def failure() = AuthorisedFor(GmpRegime, pageVisibilityPredicate) {
    implicit user =>
      implicit request =>
        Ok
  }

  def callback() = Action.async(parse.json) {

    implicit request => {

      val callBackData: CallBackData = request.body.as[CallBackData]

      val result = sessionService.cacheCallBackData(Some(callBackData))(request,
        callBackData.sessionId match {
          case sid:String if !sid.isEmpty => hc.copy(sessionId = Some(SessionId((sid))))
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
