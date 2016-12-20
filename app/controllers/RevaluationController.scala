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
import forms.RevaluationForm._
import models.{GmpDate, RevaluationDate}
import play.api.Logger
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import scala.concurrent.Future

object RevaluationController extends RevaluationController {
  val authConnector = GmpFrontendAuthConnector

}

trait RevaluationController extends GmpPageFlow {

  def get = AuthorisedFor(GmpRegime, pageVisibilityPredicate).async {
    implicit user =>
      implicit request => sessionService.fetchLeaving.map {
        case Some(leaving) => {
          Ok(views.html.revaluation(revaluationForm.fill(RevaluationDate(GmpDate(None, None, None), leaving))))
        }
        case _ => Ok(views.html.revaluation(revaluationForm))
      }
  }

  def post = AuthorisedFor(GmpRegime, pageVisibilityPredicate).async {
    implicit user =>
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

  def back = AuthorisedFor(GmpRegime, pageVisibilityPredicate).async {

    implicit user =>
      implicit request =>{
        sessionService.fetchGmpSession() map {
          case Some(session) => previousPage("RevaluationController", session)
          case _ => throw new RuntimeException
        }
      }
  }
}
