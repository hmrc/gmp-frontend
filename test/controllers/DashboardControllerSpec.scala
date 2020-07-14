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

import config.{ApplicationConfig, GmpSessionCache}
import connectors.GmpBulkConnector
import controllers.auth.{AuthAction, FakeAuthAction}
import models._
import org.joda.time.LocalDateTime
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.{OneServerPerSuite, PlaySpec}
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.api.libs.json.Json
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SessionService
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.Upstream5xxResponse
import views.Views

import scala.concurrent.{ExecutionContext, Future}

class DashboardControllerSpec extends PlaySpec with OneServerPerSuite with MockitoSugar {

  val mockAuthConnector = mock[AuthConnector]
  val mockSessionService = mock[SessionService]
  val mockGmpBulkConnector = mock[GmpBulkConnector]
  val mockAuthAction = mock[AuthAction]

  implicit val mcc = app.injector.instanceOf[MessagesControllerComponents]
  implicit val ec = app.injector.instanceOf[ExecutionContext]
  implicit val messagesAPI=app.injector.instanceOf[MessagesApi]
  implicit val messagesProvider=MessagesImpl(Lang("en"), messagesAPI)
  implicit val ac=app.injector.instanceOf[ApplicationConfig]
  implicit val gmpSessionCache=app.injector.instanceOf[GmpSessionCache]
  lazy val views = app.injector.instanceOf[Views]


  object TestDashboardController extends DashboardController(FakeAuthAction, mockAuthConnector, mockGmpBulkConnector,
          ac,mockSessionService,FakeGmpContext,mcc,ec,gmpSessionCache,views) {
   }

  "DashboardController" must {

    "Contain Ur banner" in {

      val dashboard = new Dashboard(Nil)

        val result = TestDashboardController.get(FakeRequest())
        contentAsString(result) must include(Messages("urbanner.message.text"))
        contentAsString(result) must include(Messages("urbanner.message.open.new.window"))
        contentAsString(result) must include(Messages("urbanner.message.reject"))
   }
  }

  val recentBulkCalculations = List(new BulkPreviousRequest("1234","abcd",LocalDateTime.now(),LocalDateTime.now()), new BulkPreviousRequest("5678","efgh", LocalDateTime.now(),LocalDateTime.now()))

  when(mockGmpBulkConnector.getPreviousBulkRequests(Matchers.any())(Matchers.any())).thenReturn(Future.successful(recentBulkCalculations))

  "dashboard GET " must {

    "authenticated users" must {

      "respond with ok" in {
        val result = TestDashboardController.get(FakeRequest())
            status(result) must equal(OK)
            contentAsString(result) must include(Messages("gmp.dashboard_header"))
            contentAsString(result) must include(Messages("gmp.signout"))
            contentAsString(result) must not include(Messages("gmp.back_to_dashboard"))

      }

      "contain required links to single/bulk calculation, the template file download link and more bulk calculations link" in {

          val result = TestDashboardController.get(FakeRequest())
            status(result) must equal(OK)
            contentAsString(result) must include(Messages("gmp.dashboard_header"))
            contentAsString(result) must include(Messages("gmp.dashboard.choose_calculation_type"))
            contentAsString(result) must include(Messages("gmp.single_calculation_link"))
            contentAsString(result) must include(Messages("gmp.bulk_calculation_link"))
            contentAsString(result) must include(Messages("gmp.download_templates_link"))
            contentAsString(result) must include(Messages("gmp.single_calculation_text").replace("’", "&#x27;"))
            contentAsString(result) must include(Messages("gmp.bulk_calculation_text"))
            contentAsString(result) must include(Messages("gmp.previous_calculations_text"))
      }

      "load the dashboard from the bulk service if present but empty" in {
        val dashboard = new Dashboard(Nil)
          val result = TestDashboardController.get(FakeRequest())
          contentAsString(result) must include(Messages("gmp.previous_calculations"))
      }

      "load the dashboard if the bulk service throws an exception" in {

        val brokenGmpBulkConnector = mock[GmpBulkConnector]

        object BrokenDashboardController extends DashboardController(FakeAuthAction, mockAuthConnector, brokenGmpBulkConnector,
                    ac,mockSessionService,FakeGmpContext,mcc,ec,gmpSessionCache,views) {
         }

        when(brokenGmpBulkConnector.getPreviousBulkRequests(Matchers.any())(Matchers.any())).thenReturn(Future.failed(new Upstream5xxResponse("failed",503,503)))
        val dashboard = new Dashboard(Nil)

          val result = BrokenDashboardController.get(FakeRequest())
          contentAsString(result) must include(Messages("gmp.previous_calculations"))
      }

      "load the dashboard from the bulk service if present and complete" in {

          val result = TestDashboardController.get(FakeRequest())
          contentAsString(result) must include(Messages("gmp.previous_calculations"))
          contentAsString(result) must include("1234")
          contentAsString(result) must include("5678")
      }

      "handle timestamp conversion" in {
        val localDateTime = new LocalDateTime(2016,5,18,17,50,55,511)

        val bpr = new BulkPreviousRequest("","",localDateTime,localDateTime)
        val bprJson = Json.parse(
          """
            {
              "uploadReference":"",
              "reference":"",
              "timestamp":"2016-05-18T17:50:55.511",
              "processedDateTime":"2016-05-18T17:50:55.511"
            }
          """
        )

        val bcr = BulkCalculationRequest("","","",List(),"",localDateTime)
        val bcrJson = Json.parse(
          """
            {
              "uploadReference":"",
              "email":"",
              "reference":"",
              "calculationRequests":[],
              "userId":"",
              "timestamp":"2016-05-18T17:50:55.511"
            }
          """
        )

        bprJson.as[BulkPreviousRequest] must equal(bpr)
        Json.toJson(bpr) must equal(bprJson)

        bcrJson.as[BulkCalculationRequest] must equal(bcr)
        Json.toJson(bcr) must equal(bcrJson)

      }

    }
  }

}
