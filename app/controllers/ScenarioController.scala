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
import connectors.GmpConnector
import controllers.auth.GmpRegime
import forms.ScenarioForm._
import models.{CalculationType, GmpDate}
import play.api.Logger
import play.api.data.Form
import play.api.mvc.Request
import play.twirl.api.HtmlFormat
import services.SessionService
import uk.gov.hmrc.play.frontend.auth.Actions

import scala.concurrent.Future


trait ScenarioController extends GmpPageFlow {

  def get = AuthorisedFor(GmpRegime, pageVisibilityPredicate).async {
    implicit user =>
      implicit request => Future.successful(Ok(views.html.scenario(scenarioForm)))
  }

  def post = AuthorisedFor(GmpRegime, pageVisibilityPredicate).async {

    implicit user =>
      implicit request => {
        Logger.debug(s"[ScenarioController][post][POST] : ${request.body}")
        scenarioForm.bindFromRequest().fold(
          formWithErrors => {
            Future.successful(BadRequest(views.html.scenario(formWithErrors)))
          }, calculationType => {
            sessionService.cacheScenario(calculationType.calcType.get) map { sessionOpt =>
              sessionOpt match {
                case Some(session) => nextPage("ScenarioController", session)
                case _ => throw new RuntimeException
              }
            }
          }

        )
      }
  }

  def back = AuthorisedFor(GmpRegime, pageVisibilityPredicate).async {

    implicit user =>
      implicit request => {
        sessionService.fetchGmpSession() map { gmpSessionOpt =>
          gmpSessionOpt match {
            case Some(session) => previousPage("ScenarioController", session)
            case _ => throw new RuntimeException
          }
        }
      }
  }
}

object ScenarioController extends ScenarioController {
  val authConnector = GmpFrontendAuthConnector
  val calculationConnector = GmpConnector
}
