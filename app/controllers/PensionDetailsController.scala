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
import connectors.GmpConnector
import controllers.auth.AuthAction
import forms.PensionDetailsForm
import metrics.ApplicationMetrics
import models.{PensionDetails, ValidateSconRequest}
import play.api.Logging
import play.api.i18n.Messages
import play.api.mvc.MessagesControllerComponents
import services.SessionService
import uk.gov.hmrc.auth.core.AuthConnector
import views.Views

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PensionDetailsController @Inject()(authAction: AuthAction,
                                         override val authConnector: AuthConnector,
                                         gmpConnector: GmpConnector,
                                         sessionService: SessionService,
                                         implicit val config:GmpContext,
                                         metrics: ApplicationMetrics,
                                         ac:ApplicationConfig,
                                         pdf:PensionDetailsForm,
                                         override val messagesControllerComponents: MessagesControllerComponents,
                                         implicit val executionContext: ExecutionContext,
                                         implicit val gmpSessionCache: GmpSessionCache,
                                         views: Views) extends GmpPageFlow(authConnector,sessionService,config,messagesControllerComponents,ac) with Logging {

  lazy val pensionDetailsForm=pdf.pensionDetailsForm

  def get = authAction.async {
      implicit request => {
        sessionService.fetchPensionDetails.map {
          case Some(scon) => Ok(views.pensionDetails(pensionDetailsForm.fill(PensionDetails(scon))))
          case _ => Ok(views.pensionDetails(pensionDetailsForm))
        }
      }
  }

  def post = authAction.async {
      implicit request => {
        val link = request.linkId
        logger.debug(s"[PensionDetailsController][post][POST] : ${request.body}")

        pensionDetailsForm.bindFromRequest().fold(
          formWithErrors => {
            Future.successful(BadRequest(views.pensionDetails(formWithErrors)))
          },
          pensionDetails => {

            val validateSconRequest = ValidateSconRequest(pensionDetails.scon.toUpperCase)

            gmpConnector.validateScon(validateSconRequest, link) flatMap {
              response => {
                if (response.sconExists) {
                  sessionService.cachePensionDetails(pensionDetails.scon.toUpperCase).map {
                    case Some(session) => nextPage("PensionDetailsController", session)
                    case _ => throw new RuntimeException
                  }
                }
                else {
                  metrics.countNpsSconInvalid()
                  Future.successful(BadRequest(views.pensionDetails(pensionDetailsForm.fill(
                    PensionDetails(pensionDetails.scon)).withError("scon", Messages("gmp.error.scon.nps_invalid")))))
                }

              }
            }
          }
        )
      }
  }
}
