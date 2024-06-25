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
import forms.RevaluationRateForm
import models.{GmpSession, Leaving, RevaluationRate}
import play.api.Logging
import play.api.i18n.Messages
import play.api.mvc.MessagesControllerComponents
import services.SessionService
import uk.gov.hmrc.auth.core.AuthConnector
import views.Views

import scala.concurrent.ExecutionContext


@Singleton
class RevaluationRateController @Inject()( authAction: AuthAction,
                                           override val authConnector: AuthConnector,
                                           ac:ApplicationConfig,
                                           sessionService: SessionService,
                                           implicit val config:GmpContext,
                                           override val messagesControllerComponents: MessagesControllerComponents,
                                           rrf:RevaluationRateForm,
                                           implicit val executionContext: ExecutionContext,
                                           implicit val gmpSessionCache: GmpSessionCache,
                                           views: Views
                                         ) extends GmpPageFlow(authConnector,sessionService,config,messagesControllerComponents,ac) with Logging{


  lazy val revaluationRateForm = rrf.revaluationRateForm

  def get = authAction.async {
      implicit request => sessionService.fetchGmpSession() map {
        case Some(session) => session match {
          case _ if session.scon == "" =>
            Ok(views.failure(
              Messages("gmp.error.session_parts_missing", "/guaranteed-minimum-pension/pension-details"),
              Messages("gmp.cannot_calculate.gmp"),
              Messages("gmp.session_missing.title")
            ))
          case _ if session.memberDetails.nino == "" || session.memberDetails.firstForename == "" || session.memberDetails.surname == "" =>
            Ok(views.failure(
              Messages("gmp.error.session_parts_missing", "/guaranteed-minimum-pension/member-details"),
              Messages("gmp.cannot_calculate.gmp"),
              Messages("gmp.session_missing.title")
            ))
          case _ if session.scenario == "" =>
            Ok(views.failure(
              Messages("gmp.error.session_parts_missing", "/guaranteed-minimum-pension/calculation-reason"),
              Messages("gmp.cannot_calculate.gmp"),
              Messages("gmp.session_missing.title")
            ))
          case _ if session.leaving.leaving.isEmpty =>
            Ok(views.failure(
              Messages("gmp.error.session_parts_missing", "/guaranteed-minimum-pension/left-scheme"),
              Messages("gmp.cannot_calculate.gmp"),
              Messages("gmp.session_missing.title")
            ))
          case _ => Ok(views.revaluationRate(revaluationRateForm.fill(RevaluationRate(session.rate)), session))
        }
        case _ => throw new RuntimeException
      }

  }

  def post = authAction.async {
      implicit request => {

        logger.debug(s"[RevaluationRateController][post][POST] : ${request.body}")

        revaluationRateForm.bindFromRequest().fold(
          formWithErrors => {
            sessionService.fetchGmpSession() map {
              case Some(x) => BadRequest(views.revaluationRate(formWithErrors, x))
              case _ => throw new RuntimeException
            }
          },
          revaluationRate => {
            sessionService.cacheRevaluationRate(revaluationRate.rateType.get) map {
              case Some(session) => nextPage("RevaluationRateController", session)
              case _ => throw new RuntimeException
            }
          }
        )
      }
  }

  def back = authAction.async {
     implicit request => {
        sessionService.fetchGmpSession() map {
          case Some(session) => previousPage("RevaluationRateController", session)
          case _ => throw new RuntimeException
        }
      }
  }

}
