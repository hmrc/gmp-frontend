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
import forms.ScenarioForm
import models.CalculationType
import play.api.Logging
import play.api.i18n.Messages
import play.api.mvc.MessagesControllerComponents
import services.SessionService
import uk.gov.hmrc.auth.core.AuthConnector
import views.Views

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ScenarioController @Inject()(authAction: AuthAction,
                                   override val authConnector: AuthConnector,
                                   ac:ApplicationConfig,
                                   sessionService: SessionService,
                                   implicit val config:GmpContext,
                                   override val messagesControllerComponents: MessagesControllerComponents,
                                   sf:ScenarioForm,
                                   implicit val executionContext: ExecutionContext,
                                   implicit val gmpSessionCache: GmpSessionCache,
                                   views: Views
                                  ) extends GmpPageFlow(authConnector,sessionService,config,messagesControllerComponents,ac) with Logging {


  lazy val scenarioForm=sf.scenarioForm

  def get = authAction.async {
      implicit request => sessionService.fetchGmpSession() map {
        case Some(session) => session match {
          case _ if session.scon == "" => Ok(views.failure(Messages("gmp.error.session_parts_missing", "/guaranteed-minimum-pension/pension-details"), Messages("gmp.cannot_calculate.gmp"), Messages("gmp.session_missing.title")))
          case _ if session.memberDetails.nino == "" || session.memberDetails.firstForename == "" || session.memberDetails.surname == "" => Ok(views.failure(Messages("gmp.error.session_parts_missing", "/guaranteed-minimum-pension/member-details"), Messages("gmp.cannot_calculate.gmp"), Messages("gmp.session_missing.title")))
          case _ => Ok(views.scenario(scenarioForm.fill(CalculationType(Some(session.scenario)))))
        }
        case _ => Ok(views.failure(Messages("gmp.error.session_parts_missing", "/guaranteed-minimum-pension/dashboard"), Messages("gmp.cannot_calculate.gmp"), Messages("gmp.session_missing.title")))
      }      
  }

  def post = authAction.async {
     implicit request => {

        logger.debug(s"[ScenarioController][post][POST] : ${request.body}")

        scenarioForm.bindFromRequest().fold(
          formWithErrors => {
            Future.successful(BadRequest(views.scenario(formWithErrors)))
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

