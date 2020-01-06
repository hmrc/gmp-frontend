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
import forms.RevaluationForm._
import models.{GmpDate, RevaluationDate}
import play.api.Logger
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.i18n.MessagesProvider
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.MessagesControllerComponents
import services.SessionService
import uk.gov.hmrc.auth.core.AuthConnector

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RevaluationController @Inject()( authAction: AuthAction,
                                       override val authConnector: AuthConnector,sessionService: SessionService,implicit val config:GmpContext,
                                       override val messagesControllerComponents: MessagesControllerComponents,
                                       ac:ApplicationConfig,
                                       implicit val executionContext: ExecutionContext,implicit val gmpSessionCache: GmpSessionCache
                                     ) extends GmpPageFlow(authConnector,sessionService,config,messagesControllerComponents,ac) {


  def get = authAction.async {
      implicit request => sessionService.fetchLeaving.map {
        case Some(leaving) => {
          Ok(views.html.revaluation(revaluationForm.fill(RevaluationDate(leaving, GmpDate(None, None, None)))))
        }
        case _ => Ok(views.html.revaluation(revaluationForm))
      }
  }

  def post = authAction.async {
      implicit request => {
        Logger.debug(s"[RevaluationController][post][POST] : ${request.body}")
        revaluationForm.bindFromRequest.fold(
          formWithErrors => {
            Future.successful(BadRequest(views.html.revaluation(formWithErrors)))
          },
          revaluation => {
            sessionService.cacheRevaluationDate(Some(revaluation.revaluationDate)).map {
              case Some(session) => nextPage("RevaluationController", session)
              case _ => throw new RuntimeException
            }
          }
        )
      }
  }

  def back = authAction.async {
      implicit request =>{
        sessionService.fetchGmpSession() map {
          case Some(session) => previousPage("RevaluationController", session)
          case _ => throw new RuntimeException
        }
      }
  }
}
