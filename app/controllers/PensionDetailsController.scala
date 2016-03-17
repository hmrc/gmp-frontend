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
import forms.PensionDetailsForm._
import metrics.Metrics
import models.{ValidateSconResponse, ValidateSconRequest, PensionDetails}
import play.api.Logger
import play.api.i18n.Messages
import services.SessionService
import uk.gov.hmrc.play.frontend.auth.Actions

import scala.concurrent.Future

object PensionDetailsController extends PensionDetailsController {
  val authConnector = GmpFrontendAuthConnector
  val gmpConnector = GmpConnector

  // $COVERAGE-OFF$Trivial and never going to be called by a test that uses it's own object implementation
  override def metrics = Metrics

  // $COVERAGE-ON$
}

trait PensionDetailsController extends GmpPageFlow {

  val gmpConnector: GmpConnector

  def metrics: Metrics

  def get = AuthorisedFor(GmpRegime, pageVisibilityPredicate).async {
    implicit user =>
      implicit request => {
        sessionService.fetchPensionDetails.map {
          sconOpt => sconOpt match {
            case Some(scon) => Ok(views.html.pension_details(pensionDetailsForm.fill(PensionDetails(scon))))
            case _ => Ok(views.html.pension_details(pensionDetailsForm))
          }
        }
      }
  }

  def post = AuthorisedFor(GmpRegime, pageVisibilityPredicate).async {
    implicit user =>
      implicit request => {
        Logger.debug(s"[PensionDetailsController][post][POST] : ${request.body}")
        pensionDetailsForm.bindFromRequest().fold(
          formWithErrors => {
            Future.successful(BadRequest(views.html.pension_details(formWithErrors)))
          },
          pensionDetails => {
            val validateSconRequest = ValidateSconRequest(pensionDetails.scon.toUpperCase)
            gmpConnector.validateScon(validateSconRequest) flatMap {
              response => {
                if (response.sconExists) {
                  sessionService.cachePensionDetails(pensionDetails.scon.toUpperCase).map { sessionOpt =>
                    sessionOpt match {
                      case Some(session) => nextPage("PensionDetailsController", session)
                      case _ => throw new RuntimeException
                    }
                  }
                }
                else {
                  metrics.countNpsSconInvalid()
                  Future.successful(BadRequest(views.html.pension_details(pensionDetailsForm.fill(
                    PensionDetails(pensionDetails.scon)).withError("scon", Messages("gmp.error.scon.nps_invalid")))))
                }

              }
            }
          }
        )
      }
  }
}
