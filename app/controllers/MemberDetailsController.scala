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

import config.GmpFrontendAuthConnector
import controllers.auth.GmpRegime
import forms.MemberDetailsForm._
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.Logger

import scala.concurrent.Future

object MemberDetailsController extends MemberDetailsController {
  val authConnector = GmpFrontendAuthConnector
}

trait MemberDetailsController extends GmpPageFlow {

  def get = AuthorisedFor(GmpRegime, pageVisibilityPredicate).async {
    implicit user =>
      implicit request => {
        sessionService.fetchMemberDetails() map {
          case Some(memberDetails) => Ok(views.html.member_details(form.fill(memberDetails)))
          case _ => Ok(views.html.member_details(form))
        }
      }
  }

  def post = AuthorisedFor(GmpRegime, pageVisibilityPredicate).async {
    implicit user =>
      implicit request => {

        Logger.debug(s"[MemberDetailsController][POST] : ${request.body}")

        form.bindFromRequest.fold(
          formWithErrors => {
            Future.successful(BadRequest(views.html.member_details(formWithErrors)))
          },
          memberDetails => {
            sessionService.cacheMemberDetails(memberDetails) map {
              case Some(session) => nextPage("MemberDetailsController", session)
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
          case Some(session) => previousPage("MemberDetailsController", session)
          case _ => throw new RuntimeException
        }
      }
  }

}
