/*
 * Copyright 2021 HM Revenue & Customs
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
import forms.DateOfLeavingForm
import models.CalculationType
import play.api.Logging
import play.api.i18n.Messages
import play.api.mvc.MessagesControllerComponents
import services.SessionService
import uk.gov.hmrc.auth.core.AuthConnector
import views.Views

import scala.concurrent.ExecutionContext

@Singleton
class DateOfLeavingController @Inject()(authAction: AuthAction,
                                        override val authConnector: AuthConnector,
                                        sessionService: SessionService,ac:ApplicationConfig,
                                        implicit val config:GmpContext,dlf:DateOfLeavingForm,
                                        messagesControllerComponents: MessagesControllerComponents,
                                        implicit val executionContext: ExecutionContext,
                                        implicit val gmpSessionCache: GmpSessionCache,
                                        views: Views) extends GmpPageFlow(authConnector,sessionService,config,messagesControllerComponents,ac) with Logging{

  def dateOfLeavingForm(scenario: String)= {
    val mandatoryErrorMessage = scenario match {
      case CalculationType.DOL => Messages("gmp.leaving.dol.question.mandatory")
      case CalculationType.SPA | CalculationType.PAYABLE_AGE | CalculationType.REVALUATION => Messages("gmp.other.dol.left.question.mandatory")
      case CalculationType.SURVIVOR => Messages("gmp.survivor.dol.question.mandatory")
      case _ => Messages("gmp.leaving.dol.question.mandatory")
    }
    dlf.dateOfLeavingForm(mandatoryErrorMessage)
  }

  def get = authAction.async {
      implicit request =>
        sessionService.fetchGmpSession.map {
          case Some(session) => session match {
            case _ if session.scon == "" => Ok(views.failure(Messages("gmp.error.session_parts_missing", "/guaranteed-minimum-pension/pension-details"), Messages("gmp.cannot_calculate.gmp"), Messages("gmp.session_missing.title")))
            case _ if session.memberDetails.nino == "" || session.memberDetails.firstForename == "" || session.memberDetails.surname == "" => Ok(views.failure(Messages("gmp.error.session_parts_missing", "/guaranteed-minimum-pension/member-details"), Messages("gmp.cannot_calculate.gmp"), Messages("gmp.session_missing.title")))
            case _ if session.scenario == "" => Ok(views.failure(Messages("gmp.error.session_parts_missing", "/guaranteed-minimum-pension/calculation-reason"), Messages("gmp.cannot_calculate.gmp"), Messages("gmp.session_missing.title")))
            case _ => Ok (views.dateOfLeaving (dateOfLeavingForm(session.scenario), session.scenario) )
          }
          case _ => Ok(views.failure(Messages("gmp.error.session_parts_missing", "/guaranteed-minimum-pension/dashboard"), Messages("gmp.cannot_calculate.gmp"), Messages("gmp.session_missing.title")))
        }
  }

  def post = authAction.async {
      implicit request => {
        logger.debug(s"[DateOfLeavingController][post][POST] : ${request.body}")
        val form = sessionService.fetchGmpSession.map{
          case Some(session) => dateOfLeavingForm(session.scenario)
          case None => throw new RuntimeException("No session found in order to retrieve scenario")
        }

        form.flatMap {f => f.bindFromRequest.fold(
            formWithErrors => {
              sessionService.fetchGmpSession.map {
                case Some(session) => BadRequest(views.dateOfLeaving(formWithErrors, session.scenario))
                case _ => throw new RuntimeException
              }
            },
            leaving => {
              sessionService.cacheLeaving(leaving).map {
                case Some(session) => nextPage("DateOfLeavingController", session)
                case _ => throw new RuntimeException
              }
            }
          )
        }
      }
  }

  def back = authAction.async {
      implicit request => {
        sessionService.fetchGmpSession() map {
          case Some(session) => previousPage("DateOfLeavingController", session)
          case _ => throw new RuntimeException
        }
      }
  }
}
