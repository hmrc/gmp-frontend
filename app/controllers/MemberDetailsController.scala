/*
 * Copyright 2022 HM Revenue & Customs
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
import forms.MemberDetailsForm
import play.api.Logging
import play.api.mvc.MessagesControllerComponents
import services.SessionService
import uk.gov.hmrc.auth.core.AuthConnector
import views.Views

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MemberDetailsController @Inject()(authAction: AuthAction,
                                        override val authConnector: AuthConnector,
                                        sessionService: SessionService,
                                        implicit val config:GmpContext,
                                        override val messagesControllerComponents: MessagesControllerComponents,
                                        ac:ApplicationConfig,mdf:MemberDetailsForm,
                                        implicit val executionContext: ExecutionContext,
                                        implicit val gmpSessionCache: GmpSessionCache,
                                        views: Views) extends GmpPageFlow(authConnector,sessionService,config,messagesControllerComponents,ac) with Logging{


  lazy val form=mdf.form()

  def get = authAction.async {
      implicit request => {
        sessionService.fetchMemberDetails() map {
          case Some(memberDetails) => Ok(views.memberDetails(form.fill(memberDetails)))
          case _ => Ok(views.memberDetails(form))
        }
      }
  }

  def post = authAction.async {
      implicit request => {

        logger.debug(s"[MemberDetailsController][POST] : ${request.body}")

        form.bindFromRequest.fold(
          formWithErrors => {
            Future.successful(BadRequest(views.memberDetails(formWithErrors)))
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

  def back = authAction.async {
      implicit request => {
        sessionService.fetchGmpSession() map {
          case Some(session) => previousPage("MemberDetailsController", session)
          case _ => throw new RuntimeException
        }
      }
  }

}
