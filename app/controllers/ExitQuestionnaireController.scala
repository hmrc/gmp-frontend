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

import config.{ApplicationGlobal, GmpFrontendAuthConnector}
import controllers.auth.GmpRegime
import events.ExitQuestionnaireEvent
import forms.ExitQuestionnaireForm
import play.api.Logger
import play.api.mvc.{AnyContent, Action}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.frontend.controller.UnauthorisedAction

import scala.concurrent.Future

trait ExitQuestionnaireController extends GmpController {

  val auditConnector : AuditConnector = ApplicationGlobal.auditConnector

  def get = AuthorisedFor(GmpRegime, pageVisibilityPredicate).async {
    implicit user =>
      implicit request =>
        Future.successful(Ok(views.html.exit_questionnaire(ExitQuestionnaireForm.exitQuestionnaireForm)))
  }

  def post = AuthorisedFor(GmpRegime, pageVisibilityPredicate).async {
    implicit user =>
      implicit request => {
        Logger.debug(s"[ExitQuestionnaireController][post]: ${request.body}")

        ExitQuestionnaireForm.exitQuestionnaireForm.bindFromRequest().fold(
          formWithErrors => {Future.successful(BadRequest(views.html.exit_questionnaire(formWithErrors)))},
          value => {
            val questionnaireResult = auditConnector.sendEvent(new ExitQuestionnaireEvent(value.serviceDifficulty.getOrElse(""), value.serviceFeel.getOrElse(""),
              value.comments.getOrElse("")))
            questionnaireResult.onFailure {
              case e: Throwable => Logger.warn("[ExitQuestionnaireController][post] : questionnaireResult: " + e.getMessage(), e)
            }
            Future.successful(Redirect(controllers.routes.ExitQuestionnaireController.showThankYou()))
          }
        )
      }
  }

  def showThankYou: Action[AnyContent] = UnauthorisedAction(implicit request => Ok(views.html.thank_you()))

}

object ExitQuestionnaireController extends ExitQuestionnaireController {
  val authConnector = GmpFrontendAuthConnector
}
