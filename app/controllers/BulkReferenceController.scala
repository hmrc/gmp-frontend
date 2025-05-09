/*
 * Copyright 2024 HM Revenue & Customs
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
import forms.BulkReferenceForm
import play.api.Logging
import play.api.mvc.MessagesControllerComponents
import services.{GMPSessionService, SessionService}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import views.Views
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BulkReferenceController @Inject()(authAction: AuthAction,
                                        val authConnector: AuthConnector,
                                        auditConnector : AuditConnector,
                                        GMPSessionService: GMPSessionService,implicit val config:GmpContext,brf:BulkReferenceForm,
                                        override val messagesControllerComponents: MessagesControllerComponents,
                                        implicit val executionContext: ExecutionContext,ac:ApplicationConfig,
                                        implicit val gmpSessionCache: GmpSessionCache,
                                        views: Views)
  extends GmpController(messagesControllerComponents,ac,GMPSessionService,config) with Logging{

lazy val bulkReferenceForm = brf.bulkReferenceForm

  def get = authAction.async {
      implicit request =>
        Future.successful(Ok(views.bulkReference(bulkReferenceForm)))
  }

  def post = authAction.async {
      implicit request => {
        logger.debug(s"[BulkReferenceController][post]: ${request.body}")

        bulkReferenceForm.bindFromRequest().fold(
          formWithErrors => {Future.successful(BadRequest(views.bulkReference(formWithErrors)))},
          value => {
            GMPSessionService.cacheEmailAndReference(Some(value.email.trim), Some(value.reference.trim)).map {
              case Some(session) => Redirect(controllers.routes.BulkRequestReceivedController.get)
              case _ => throw new RuntimeException
            }
          }
        )
      }
  }

  def back = authAction.async {
      _ => {
        Future.successful(Redirect(routes.FileUploadController.get))
      }
  }
}


