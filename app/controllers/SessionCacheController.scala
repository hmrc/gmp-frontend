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
import controllers.auth.AuthAction
import play.api.Logging
import play.api.mvc.MessagesControllerComponents
import services.SessionService
import uk.gov.hmrc.auth.core.AuthConnector
import scala.concurrent.ExecutionContext

@Singleton
class SessionCacheController @Inject()(authAction: AuthAction,
                                       override val authConnector: AuthConnector,
                                       ac:ApplicationConfig,
                                       sessionService: SessionService,
                                       implicit val config:GmpContext,
                                       override val messagesControllerComponents: MessagesControllerComponents,
                                       implicit val executionContext: ExecutionContext,
                                       implicit val gmpSessionCache: GmpSessionCache
                                      ) extends GmpPageFlow(authConnector,sessionService,config,messagesControllerComponents,ac) with Logging {

  def newCalculation = authAction.async {
      implicit request => {

        logger.debug(s"[SessionCacheController][newCalculation][GET] : $request")

        sessionService.resetGmpSessionWithScon map {
          case Some(x) => Redirect(controllers.routes.PensionDetailsController.get)
          case None => throw new RuntimeException
        }
      }
  }

  def newBulkCalculation = authAction.async {
      implicit request => {

        logger.debug(s"[SessionCacheController][newBulkCalculation][GET] : $request")

        sessionService.resetGmpBulkSession() map {
          case Some(x) => Redirect(controllers.routes.FileUploadController.get)
          case None => throw new RuntimeException
        }
      }
  }
}

