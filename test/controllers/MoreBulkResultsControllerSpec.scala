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

import config.ApplicationConfig
import connectors.GmpBulkConnector
import controllers.auth.{AuthAction, FakeAuthAction}
import models._
import org.joda.time.LocalDateTime
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.{OneServerPerSuite, PlaySpec}
import play.api.i18n.{Messages, MessagesProvider}
import play.api.i18n.Messages.Implicits._
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SessionService
import uk.gov.hmrc.auth.core.AuthConnector

import scala.concurrent.{ExecutionContext, Future}

class MoreBulkResultsControllerSpec extends PlaySpec with OneServerPerSuite with MockitoSugar {

  val mockAuthConnector = mock[AuthConnector]
  val mockSessionService = mock[SessionService]
  val mockGmpBulkConnector = mock[GmpBulkConnector]
  val mockAuthAction = mock[AuthAction]
  implicit val mcc = app.injector.instanceOf[MessagesControllerComponents]
  implicit val ec = app.injector.instanceOf[ExecutionContext]
  implicit val messagesProvider=app.injector.instanceOf[MessagesProvider]
  implicit val ac=app.injector.instanceOf[ApplicationConfig]


  object TestMoreBulkResultsController extends MoreBulkResultsController(FakeAuthAction, mockAuthConnector, mockGmpBulkConnector,ac,mcc,ec) {
    override val sessionService = mockSessionService
    override val context = FakeGmpContext
  }

  val recentBulkCalculations = List(new BulkPreviousRequest("1234","abcd",LocalDateTime.now(),LocalDateTime.now()), new BulkPreviousRequest("5678","efgh", LocalDateTime.now(),LocalDateTime.now()))

  when(mockGmpBulkConnector.getPreviousBulkRequests(Matchers.any())(Matchers.any())).thenReturn(Future.successful(recentBulkCalculations))

  "more bulk results GET " must {

    "authenticated users" must {

      "respond with ok" in {
        val result = TestMoreBulkResultsController.retrieveMoreBulkResults(FakeRequest())
            status(result) must equal(OK)
            contentAsString(result) must include(Messages("gmp.more_bulk_results.header"))
            contentAsString(result) must include(Messages("gmp.signout"))
            contentAsString(result) must include(Messages("gmp.back.link"))

      }

      "display table with more recent bulk calculation links" in {
        val result = TestMoreBulkResultsController.retrieveMoreBulkResults(FakeRequest())
            status(result) must equal(OK)
            contentAsString(result) must include(Messages("gmp.more_bulk_results.header"))
      }
    }
  }

}
