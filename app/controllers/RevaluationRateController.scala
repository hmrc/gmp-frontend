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
import controllers.auth.GmpRegime
import forms.RevaluationRateForm._
import play.api.Logger
import services.SessionService
import uk.gov.hmrc.play.frontend.auth.Actions


object RevaluationRateController extends RevaluationRateController {
  val authConnector = GmpFrontendAuthConnector

}

trait RevaluationRateController extends GmpPageFlow {

  def get = AuthorisedFor(GmpRegime, pageVisibilityPredicate).async {
    implicit user =>
      implicit request => sessionService.fetchGmpSession() map {
        case Some(x) => Ok(views.html.revaluation_rate(revaluationRateForm, x))
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
