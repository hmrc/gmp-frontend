/*
 * Copyright 2016 HM Revenue & Customs
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

import connectors.GmpBulkConnector
import models._
import org.joda.time.{Chronology, LocalDateTime, LocalDate}
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.{OneServerPerSuite, PlaySpec}
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SessionService
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.http.Upstream5xxResponse

import scala.concurrent.Future

class DashboardControllerSpec extends PlaySpec with OneServerPerSuite with MockitoSugar with GmpUsers {

  val mockAuthConnector = mock[AuthConnector]
  val mockSessionService = mock[SessionService]
  val mockGmpBulkConnector = mock[GmpBulkConnector]

  object TestDashboardController extends DashboardController {
    val authConnector = mockAuthConnector
    override val sessionService = mockSessionService
    override val gmpBulkConnector = mockGmpBulkConnector
  }

  "DashboardController" must {

    "respond to GET /guaranteed-minimum-pension/dashboard" in {
      val result = route(FakeRequest(GET, "/guaranteed-minimum-pension/dashboard"))
      status(result.get) must not equal (NOT_FOUND)
    }

    "respond to GET /guaranteed-minimum-pension" in {
      val result = route(FakeRequest(GET, "/guaranteed-minimum-pension"))
      status(result.get) must not equal (NOT_FOUND)
    }
  }

  val recentBulkCalculations = List(new BulkPreviousRequest("1234","abcd",LocalDateTime.now(),LocalDateTime.now()), new BulkPreviousRequest("5678","efgh", LocalDateTime.now(),LocalDateTime.now()))

  when(mockGmpBulkConnector.getPreviousBulkRequests()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(recentBulkCalculations))

  "dashboard GET " must {

    "be authorised" in {
      getDashboard() { result =>
        status(result) must equal(SEE_OTHER)
        redirectLocation(result).get must include("/gg/sign-in")
      }
    }

    "authenticated users" must {

      "respond with ok" in {
        withAuthorisedUser { user =>
          getDashboard(user) { result =>
            status(result) must equal(OK)
            contentAsString(result) must include(Messages("gmp.dashboard.title"))
            contentAsString(result) must include(Messages("gmp.signout"))
            contentAsString(result) must not include(Messages("gmp.back_to_dashboard"))
          }
        }
      }

      "contain required links to single/bulk calculation, the template file download link and more bulk calculations link" in {

        withAuthorisedUser { user =>
          getDashboard(user) { result =>
            status(result) must equal(OK)
            contentAsString(result) must include(Messages("gmp.dashboard_header"))
            contentAsString(result) must include(Messages("gmp.dashboard.choose_calculation_type"))
            contentAsString(result) must include(Messages("gmp.single_calculation_link"))
            contentAsString(result) must include(Messages("gmp.bulk_calculation_link"))
            contentAsString(result) must include(Messages("gmp.download_templates_link"))
            contentAsString(result) must include(Messages("gmp.single_calculation_text").replace("'", "&#x27;"))
            contentAsString(result) must include(Messages("gmp.bulk_calculation_text"))
            contentAsString(result) must include(Messages("gmp.previous_calculations_text"))

          }
        }
      }

      "load the dashboard from the bulk service if present but empty" in {
        val dashboard = new Dashboard(Nil)
        withAuthorisedUser { request =>
          val result = TestDashboardController.get.apply(request)
          contentAsString(result) must include(Messages("gmp.previous_calculations"))
        }
      }

      "load the dashboard if the bulk service throws an exception" in {

        val brokenGmpBulkConnector = mock[GmpBulkConnector]

        object BrokenDashboardController extends DashboardController {
          val authConnector = mockAuthConnector
          override val sessionService = mockSessionService
          override val gmpBulkConnector = brokenGmpBulkConnector
        }

        when(brokenGmpBulkConnector.getPreviousBulkRequests()(Matchers.any(), Matchers.any())).thenReturn(Future.failed(new Upstream5xxResponse("failed",503,503)))
        val dashboard = new Dashboard(Nil)
        withAuthorisedUser { request =>
          val result = BrokenDashboardController.get.apply(request)
          contentAsString(result) must include(Messages("gmp.previous_calculations"))
        }
      }

      "load the dashboard from the bulk service if present and complete" in {

        withAuthorisedUser { request =>
          val result = TestDashboardController.get.apply(request)
          contentAsString(result) must include(Messages("gmp.previous_calculations"))
          contentAsString(result) must include("1234")
          contentAsString(result) must include("5678")
        }
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


  def getDashboard(request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest())(handler: Future[Result] => Any): Unit = {
    handler(TestDashboardController.get.apply(request))
  }

}
