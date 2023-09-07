/*
 * Copyright 2023 HM Revenue & Customs
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
import config.{ApplicationConfig, GmpContext}
import controllers.auth.{AuthAction, ExternalUrls, UUIDGenerator}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionService
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.model.DataEvent
import views.Views

import scala.concurrent.ExecutionContext

@Singleton
class ApplicationController @Inject()(authAction: AuthAction,
                                      auditConnector: AuditConnector,
                                      val authConnector: AuthConnector,
                                      uuidGenerator: UUIDGenerator,
                                      sessionService: SessionService,implicit val config:GmpContext,
                                      messagesControllerComponents: MessagesControllerComponents,
                                      implicit val executionContext: ExecutionContext,
                                      ac:ApplicationConfig,
                                      views: Views,
                                      externalUrls: ExternalUrls)
                                      extends GmpController(messagesControllerComponents,ac,sessionService,config) {



  def unauthorised: Action[AnyContent] = Action {
    implicit request =>
      Ok(views.unauthorised())
  }


  def signout: Action[AnyContent] = authAction {
      implicit request =>
        val uuid: String = uuidGenerator.generate
        val auditData = Map("feedbackId" -> uuid)
        val dataEvent: DataEvent = DataEvent("GMP", "signout", detail = auditData)

        auditConnector.sendEvent(dataEvent)
        Redirect(externalUrls.signOutCallback).withSession(("feedbackId", uuid))
  }
}


