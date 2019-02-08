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

import config.{ApplicationGlobal, GmpFrontendAuthConnector}
import controllers.auth.GmpRegime
import forms.BulkReferenceForm
import play.api.Logger
import services.SessionService
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import scala.concurrent.Future

trait BulkReferenceController extends GmpController {

  val auditConnector : AuditConnector = ApplicationGlobal.auditConnector

  def get = AuthorisedFor(GmpRegime, pageVisibilityPredicate).async {
    implicit user =>
      implicit request =>  Future.successful(Ok(views.html.bulk_reference(BulkReferenceForm.bulkReferenceForm)))
  }

  def post = AuthorisedFor(GmpRegime, pageVisibilityPredicate).async {
    implicit user =>
      implicit request => {
        Logger.debug(s"[BulkReferenceController][post]: ${request.body}")

        BulkReferenceForm.bulkReferenceForm.bindFromRequest().fold(
          formWithErrors => {Future.successful(BadRequest(views.html.bulk_reference(formWithErrors)))},
          value => {

            sessionService.cacheEmailAndReference(Some(value.email.trim), Some(value.reference.trim)).map {
              case Some(session) => Redirect(controllers.routes.BulkRequestReceivedController.get())
              case _ => throw new RuntimeException
            }
          }
        )
      }
  }

  def back = AuthorisedFor(GmpRegime, pageVisibilityPredicate).async {
    implicit user =>
      implicit request => {
        Future.successful(Redirect(routes.FileUploadController.get()))
      }
  }
}

object BulkReferenceController extends BulkReferenceController {
  val authConnector = GmpFrontendAuthConnector
}
