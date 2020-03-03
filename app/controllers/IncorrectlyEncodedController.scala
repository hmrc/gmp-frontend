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

import com.google.inject.Inject
import config.{ApplicationConfig, GmpContext, GmpSessionCache}
import controllers.auth.AuthAction
import play.api.i18n.Messages
import play.api.mvc.MessagesControllerComponents
import services.SessionService
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.play.partials.FormPartialRetriever

import scala.concurrent.{ExecutionContext, Future}

class IncorrectlyEncodedController @Inject()(authAction: AuthAction, override val authConnector: AuthConnector, sessionService: SessionService, override val messagesControllerComponents: MessagesControllerComponents, ac: ApplicationConfig, formPartialRetriever: FormPartialRetriever)
                                            (implicit val executionContext: ExecutionContext, val config: GmpContext) extends GmpPageFlow(authConnector,sessionService,config,messagesControllerComponents,ac) {



  def get = authAction.async {
    implicit request => {
      Future.successful(
        InternalServerError(views.html.incorrectlyEncoded(
          Messages("gmp.bulk.incorrectlyEncoded"),
          Messages("gmp.bulk.incorrectlyEncoded.header"), formPartialRetriever)))
    }
  }

}