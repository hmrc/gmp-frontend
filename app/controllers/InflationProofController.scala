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
import config.GmpFrontendAuthConnector
import controllers.auth.GmpRegime
import forms.InflationProofForm._
import play.api.Logger
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

import scala.concurrent.Future

@Singleton
class InflationProofController @Inject()(override val authConnector: AuthConnector) extends GmpPageFlow(authConnector) {

  def get = AuthorisedFor(GmpRegime, pageVisibilityPredicate).async {
    implicit user =>
      implicit request => {
        Future.successful(Ok(views.html.inflation_proof(inflationProofForm)))
      }
  }

  def post = AuthorisedFor(GmpRegime, pageVisibilityPredicate).async {
    implicit user =>
      implicit request => {

        Logger.debug(s"[InflationProofController][POST] : ${request.body}")

        inflationProofForm.bindFromRequest.fold(
          formWithErrors => {
            Future.successful(BadRequest(views.html.inflation_proof(formWithErrors)))
          },
          revaluation => {
            val dateToStore = revaluation.revaluate match {
              case Some("Yes") => Some(revaluation.revaluationDate)
              case _ => None
            }
            sessionService.cacheRevaluationDate(dateToStore).map {
              case Some(session) => nextPage("InflationProofController", session)
                  // $COVERAGE-OFF$
              case _ => throw new RuntimeException
                // $COVERAGE-ON$
            }
          }
        )
      }
  }

  def back = AuthorisedFor(GmpRegime, pageVisibilityPredicate).async {

    implicit user =>
      implicit request => {
        sessionService.fetchGmpSession() map {
          case Some(session) => previousPage("InflationProofController", session)
          case _ => throw new RuntimeException
        }
      }
  }

}
