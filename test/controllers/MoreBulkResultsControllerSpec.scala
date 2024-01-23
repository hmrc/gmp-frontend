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

import config.ApplicationConfig
import connectors.GmpBulkConnector
import controllers.auth.{AuthAction, FakeAuthAction}
import models._
import org.joda.time.LocalDateTime
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import org.scalatestplus.play.PlaySpec
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SessionService
import uk.gov.hmrc.auth.core.AuthConnector
import views.Views

import scala.concurrent.{ExecutionContext, Future}

class MoreBulkResultsControllerSpec extends PlaySpec with GuiceOneServerPerSuite with MockitoSugar {

  val mockAuthConnector = mock[AuthConnector]
  val mockSessionService = mock[SessionService]
  val mockGmpBulkConnector = mock[GmpBulkConnector]
  val mockAuthAction = mock[AuthAction]
  implicit val mcc = app.injector.instanceOf[MessagesControllerComponents]
  implicit val ec = app.injector.instanceOf[ExecutionContext]
  implicit val messagesAPI=app.injector.instanceOf[MessagesApi]
  implicit val messagesProvider=MessagesImpl(Lang("en"), messagesAPI)
  implicit val ac=app.injector.instanceOf[ApplicationConfig]
  lazy val views = app.injector.instanceOf[Views]

  object TestMoreBulkResultsController extends MoreBulkResultsController(FakeAuthAction, mockAuthConnector, mockSessionService,
                  FakeGmpContext,mockGmpBulkConnector,ac,mcc,ec,views)


  val recentBulkCalculations = List(new BulkPreviousRequest("1234","abcd",LocalDateTime.now(),LocalDateTime.now()), new BulkPreviousRequest("5678","efgh", LocalDateTime.now(),LocalDateTime.now()))

  when(mockGmpBulkConnector.getPreviousBulkRequests(any())(any())).thenReturn(Future.successful(recentBulkCalculations))

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
