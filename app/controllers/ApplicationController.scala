/*
 * Copyright 2018 HM Revenue & Customs
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

import java.util.UUID

import config.{GmpFrontendAuditConnector, GmpFrontendAuthConnector}
import controllers.auth.{ExternalUrls, GmpRegime}
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.model.DataEvent
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
trait ApplicationController extends GmpController {

  val auditConnector: AuditConnector
  val authConnector: AuthConnector
  val uuidGenerator: UUIDGenerator
  
  def unauthorised: Action[AnyContent] = Action {
    implicit request =>
      Ok(views.html.unauthorised())
  }


  def signout: Action[AnyContent] = AuthorisedFor(GmpRegime, pageVisibilityPredicate) {
    implicit user =>
      implicit request =>
        val uuid: String = uuidGenerator.generate
        val auditData = Map("feedbackId" -> uuid)
        val dataEvent: DataEvent = DataEvent("GMP", "signout", detail = auditData)

        auditConnector.sendEvent(dataEvent)
        Redirect(ExternalUrls.signOutCallback).withSession(("feedbackId", uuid))
  }
}

object ApplicationController extends ApplicationController {
  override val auditConnector: AuditConnector = GmpFrontendAuditConnector
  override val authConnector: AuthConnector = GmpFrontendAuthConnector
  override val uuidGenerator: UUIDGenerator = UUIDGenerator
}

trait UUIDGenerator {
  def generate: String
}

object UUIDGenerator extends UUIDGenerator {
  override def generate: String = UUID.randomUUID().toString()
}