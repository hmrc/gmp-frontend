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
import controllers.auth.GmpRegime
import forms.RevaluationRateForm._
import play.api.Logger
import play.api.Play.current
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector


@Singleton
class RevaluationRateController @Inject()(val authConnector: AuthConnector) extends GmpPageFlow {

  def get = AuthorisedFor(GmpRegime, pageVisibilityPredicate).async {
    implicit user =>
      implicit request => sessionService.fetchGmpSession() map {
        case Some(session) => session match {
          case _ if session.scon == "" => Ok(views.html.failure(Messages("gmp.error.session_parts_missing", "/guaranteed-minimum-pension/pension-details"), Messages("gmp.cannot_calculate.gmp"), Messages("gmp.session_missing.title")))
          case _ if session.memberDetails.nino == "" || session.memberDetails.firstForename == "" || session.memberDetails.surname == "" => Ok(views.html.failure(Messages("gmp.error.session_parts_missing", "/guaranteed-minimum-pension/member-details"), Messages("gmp.cannot_calculate.gmp"), Messages("gmp.session_missing.title")))
          case _ if session.scenario == "" => Ok(views.html.failure(Messages("gmp.error.session_parts_missing", "/guaranteed-minimum-pension/calculation-reason"), Messages("gmp.cannot_calculate.gmp"), Messages("gmp.session_missing.title")))
          case _ if !session.leaving.leaving.isDefined => Ok(views.html.failure(Messages("gmp.error.session_parts_missing", "/guaranteed-minimum-pension/left-scheme"), Messages("gmp.cannot_calculate.gmp"), Messages("gmp.session_missing.title")))
          case _ => Ok(views.html.revaluation_rate(revaluationRateForm, session))
        }
        case _ => throw new RuntimeException
      }

  }

  def post = AuthorisedFor(GmpRegime, pageVisibilityPredicate).async {
    implicit user =>
      implicit request => {

        Logger.debug(s"[RevaluationRateController][post][POST] : ${request.body}")

        revaluationRateForm.bindFromRequest.fold(
          formWithErrors => {
            sessionService.fetchGmpSession() map {
              case Some(x) => BadRequest(views.html.revaluation_rate(formWithErrors, x))
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

  def back = AuthorisedFor(GmpRegime, pageVisibilityPredicate).async {

    implicit user =>
      implicit request => {
        sessionService.fetchGmpSession() map {
          case Some(session) => previousPage("RevaluationRateController", session)
          case _ => throw new RuntimeException
        }
      }
  }

}
