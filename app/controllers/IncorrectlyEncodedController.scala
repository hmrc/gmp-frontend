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

import com.google.inject.Inject
import config.{ApplicationConfig, GmpContext}
import controllers.auth.AuthAction
import play.api.i18n.Messages
import play.api.mvc.MessagesControllerComponents
import services.{GMPSessionService, SessionService}
import uk.gov.hmrc.auth.core.AuthConnector
import views.Views

import scala.concurrent.{ExecutionContext, Future}

class IncorrectlyEncodedController @Inject()( authAction: AuthAction,
                                              override val authConnector: AuthConnector,
                                              GMPSessionService: GMPSessionService,
                                              implicit val config:GmpContext,
                                              override val messagesControllerComponents: MessagesControllerComponents,
                                              ac:ApplicationConfig,
                                              implicit val executionContext: ExecutionContext,
                                              views: Views
                                            ) extends GmpPageFlow(authConnector,GMPSessionService,config,messagesControllerComponents,ac){

  def get = authAction.async {
    implicit request => {
      Future.successful(
        InternalServerError(views.incorrectlyEncoded(
          Messages("gmp.bulk.incorrectlyEncoded"),
          Messages("gmp.bulk.incorrectlyEncoded.header"))))
    }
  }

}
