/*
 * Copyright 2020 HM Revenue & Customs
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
import play.api.Logger
import play.api.i18n.Messages
import play.api.mvc.MessagesControllerComponents
import services.SessionService
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.play.partials.FormPartialRetriever

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ScenarioController @Inject()(authAction: AuthAction,
                                   override val authConnector: AuthConnector,
                                   ac:ApplicationConfig,sessionService: SessionService,
                                   override val messagesControllerComponents: MessagesControllerComponents,sf:ScenarioForm, formPartialRetriever: FormPartialRetriever
)(implicit val executionContext: ExecutionContext, val gmpSessionCache: GmpSessionCache, val config:GmpContext)
  extends GmpPageFlow(authConnector,sessionService,config,messagesControllerComponents,ac) {


  lazy val scenarioForm=sf.scenarioForm

  def get = authAction.async {
      implicit request => sessionService.fetchGmpSession() map {
        case Some(session) => session match {
          case _ if session.scon == "" => Ok(views.html.failure(Messages("gmp.error.session_parts_missing", "/guaranteed-minimum-pension/pension-details"), Messages("gmp.cannot_calculate.gmp"), Messages("gmp.session_missing.title"), formPartialRetriever))
          case _ if session.memberDetails.nino == "" || session.memberDetails.firstForename == "" || session.memberDetails.surname == "" => Ok(views.html.failure(Messages("gmp.error.session_parts_missing", "/guaranteed-minimum-pension/member-details"), Messages("gmp.cannot_calculate.gmp"), Messages("gmp.session_missing.title"), formPartialRetriever))
          case _ => Ok(views.html.scenario(scenarioForm, formPartialRetriever))
        }
        case _ => Ok(views.html.failure(Messages("gmp.error.session_parts_missing", "/guaranteed-minimum-pension/dashboard"), Messages("gmp.cannot_calculate.gmp"), Messages("gmp.session_missing.title"), formPartialRetriever))
      }
  }

  def post = authAction.async {
     implicit request => {

        Logger.debug(s"[ScenarioController][post][POST] : ${request.body}")

        scenarioForm.bindFromRequest().fold(
          formWithErrors => {
            Future.successful(BadRequest(views.html.scenario(formWithErrors, formPartialRetriever)))
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

