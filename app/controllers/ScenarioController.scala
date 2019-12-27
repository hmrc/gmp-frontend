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
import controllers.auth.AuthAction
import forms.ScenarioForm._
import play.api.Logger
import play.api.Play.current
import play.api.i18n.{Messages, MessagesProvider}
import play.api.i18n.Messages.Implicits._
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.auth.core.AuthConnector

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ScenarioController @Inject()(authAction: AuthAction,
                                   override val authConnector: AuthConnector,
                                   override val messagesControllerComponents: MessagesControllerComponents,
                                   implicit val executionContext: ExecutionContext,
                                   override implicit val messagesProvider: MessagesProvider
                                  ) extends GmpPageFlow(authConnector,messagesControllerComponents) {

  def get = authAction.async {
      implicit request => sessionService.fetchGmpSession() map {
        case Some(session) => session match {
          case _ if session.scon == "" => Ok(views.html.failure(Messages("gmp.error.session_parts_missing", "/guaranteed-minimum-pension/pension-details"), Messages("gmp.cannot_calculate.gmp"), Messages("gmp.session_missing.title")))
          case _ if session.memberDetails.nino == "" || session.memberDetails.firstForename == "" || session.memberDetails.surname == "" => Ok(views.html.failure(Messages("gmp.error.session_parts_missing", "/guaranteed-minimum-pension/member-details"), Messages("gmp.cannot_calculate.gmp"), Messages("gmp.session_missing.title")))
          case _ => Ok(views.html.scenario(scenarioForm))
        }
        case _ => Ok(views.html.failure(Messages("gmp.error.session_parts_missing", "/guaranteed-minimum-pension/dashboard"), Messages("gmp.cannot_calculate.gmp"), Messages("gmp.session_missing.title")))
      }      
  }

  def post = authAction.async {
     implicit request => {

        Logger.debug(s"[ScenarioController][post][POST] : ${request.body}")

        scenarioForm.bindFromRequest().fold(
          formWithErrors => {
            Future.successful(BadRequest(views.html.scenario(formWithErrors)))
          }, calculationType => {
            sessionService.cacheScenario(calculationType.calcType.get) map {
              case Some(session) => nextPage("ScenarioController", session)
              case _ => throw new RuntimeException
            }
          }

        )
      }
  }

  def back = authAction.async {
     implicit request => {
        sessionService.fetchGmpSession() map {
          case Some(session) => previousPage("ScenarioController", session)
          case _ => throw new RuntimeException
        }
      }
  }
}

