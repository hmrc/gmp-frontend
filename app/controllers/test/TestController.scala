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

package controllers.test

import com.google.inject.Inject
import config.{ApplicationConfig, GmpContext}
import controllers.GmpPageFlow
import controllers.auth.AuthAction
import models.upscan.{ErrorDetails, UploadedFailed}
import play.api.mvc.MessagesControllerComponents
import services.SessionService
import uk.gov.hmrc.auth.core.AuthConnector
import views.Views
import scala.concurrent.{ExecutionContext, Future}

class TestController @Inject()( authAction: AuthAction,
                                              override val authConnector: AuthConnector,
                                              sessionService: SessionService,implicit val config:GmpContext,
                                              override val messagesControllerComponents: MessagesControllerComponents,ac:ApplicationConfig,
                                              implicit val executionContext: ExecutionContext,
                                              views: Views
                                            ) extends GmpPageFlow(authConnector,sessionService,config,messagesControllerComponents,ac){

  def testError(code: String) = authAction.async {
    implicit request => {
      val status1 = UploadedFailed("ref1", ErrorDetails("UNKNOWN", "Not known"))
      val status2 = UploadedFailed("ref2", ErrorDetails("REJECTED", "Empty"))
      val status3 = UploadedFailed("ref3", ErrorDetails("QUARANTINE", "Virus"))

      code match {
        case "1" => Future.successful(Ok(views.uploadResult(status1)))
        case "2" => Future.successful(Ok(views.uploadResult(status2)))
        case _   => Future.successful(Ok(views.uploadResult(status3)))
      }

    }
  }

}
