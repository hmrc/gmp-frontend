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
import forms.EqualiseForm._
import play.api.Logger
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import services.SessionService
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

import scala.concurrent.Future

@Singleton
class EqualiseController @Inject()(val authConnector: AuthConnector,
                                   sessionService: SessionService) extends GmpPageFlow {

  def get = AuthorisedFor(GmpRegime, pageVisibilityPredicate).async {
    implicit user => implicit request => Future.successful(Ok(views.html.equalise(equaliseForm)))
  }

  def post = AuthorisedFor(GmpRegime, pageVisibilityPredicate).async {

    implicit user =>
      implicit request => {

        Logger.debug(s"[EqualiseController][POST] : ${request.body}")

        equaliseForm.bindFromRequest().fold(

          formWithErrors => {Future.successful(BadRequest(views.html.equalise(formWithErrors)))},

          equalise => {
            sessionService.cacheEqualise(equalise.equalise) map {
              case Some(session) => nextPage("EqualiseController", session)
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
          case Some(session) => previousPage("EqualiseController", session)
          case _ => throw new RuntimeException
        }
      }
  }

}
