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
import controllers.auth.AuthAction
import forms.InflationProofForm._
import play.api.Logger
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.i18n.MessagesProvider
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.auth.core.AuthConnector

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class InflationProofController @Inject()( authAction: AuthAction,
                                          override val authConnector: AuthConnector,
                                          override val messagesControllerComponents: MessagesControllerComponents,
                                          implicit val executionContext: ExecutionContext,
                                          override implicit val messagesProvider: MessagesProvider
                                        ) extends GmpPageFlow(authConnector,messagesControllerComponents) {

  def get = authAction.async {
      implicit request => {
        Future.successful(Ok(views.html.inflation_proof(inflationProofForm)))
      }
  }

  def post = authAction.async {
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

  def back = authAction.async {
      implicit request => {
        sessionService.fetchGmpSession() map {
          case Some(session) => previousPage("InflationProofController", session)
          case _ => throw new RuntimeException
        }
      }
  }

}
