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
import forms.RevaluationForm
import models.{GmpDate, GmpSession, RevaluationDate}
import play.api.Logging

import play.api.mvc.MessagesControllerComponents
import services.GMPSessionService
import uk.gov.hmrc.auth.core.AuthConnector
import views.Views

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RevaluationController @Inject()( authAction: AuthAction,
                                       override val authConnector: AuthConnector,
                                       GMPSessionService: GMPSessionService,
                                       implicit val config:GmpContext,
                                       override val messagesControllerComponents: MessagesControllerComponents,
                                       ac:ApplicationConfig,
                                       rvform: RevaluationForm,
                                       implicit val executionContext: ExecutionContext,
                                       implicit val gmpSessionCache: GmpSessionCache,
                                       views: Views
                                     ) extends GmpPageFlow(authConnector,GMPSessionService,config,messagesControllerComponents,ac) with Logging {

  def revalForm(session: GmpSession) = {
    val revalDate = session.revaluationDate.fold(GmpDate(None, None, None))(identity)
    rvform.revaluationForm(session).fill(RevaluationDate(session.leaving, revalDate))
  }

  def get = authAction.async { implicit request =>
    GMPSessionService.fetchGmpSession().map {
      case Some(session) => {
        val revalDate = session.revaluationDate.fold(GmpDate(None, None, None))(identity)
        Ok(views.revaluation(revalForm(session).fill(RevaluationDate(session.leaving, revalDate))))
      }
      case _ => sys.error(" Session not present")
    }
  }


  def post = authAction.async {
    implicit request => {
      logger.debug(s"[RevaluationController][post][POST] : ${request.body}")
      val form = GMPSessionService.fetchGmpSession().map {
        _ match {
          case Some(session) => revalForm(session)
          case None => throw new RuntimeException("No session found in order to retrieve scenario")
        }
      }

      form.flatMap { f =>
        f.bindFromRequest().fold(
          formWithErrors => {
            Future.successful(BadRequest(views.revaluation(formWithErrors)))
          },
          revaluation => {
            GMPSessionService.cacheRevaluationDate(Some(revaluation.revaluationDate)).map {
              case Some(session) => nextPage("RevaluationController", session)
              case _ => throw new RuntimeException
            }
          }
        )

      }
    }
  }

  def back = authAction.async {
    implicit request =>{
      GMPSessionService.fetchGmpSession() map {
        case Some(session) => previousPage("RevaluationController", session)
        case _ => throw new RuntimeException
      }
    }
  }
}