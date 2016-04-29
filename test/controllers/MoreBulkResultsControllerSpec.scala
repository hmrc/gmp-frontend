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
import models.{BulkPreviousRequest, Dashboard}
import org.joda.time.LocalDate
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.{OneServerPerSuite, PlaySpec}
import play.api.i18n.Messages
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SessionService
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

import scala.concurrent.Future

class MoreBulkResultsControllerSpec extends PlaySpec with OneServerPerSuite with MockitoSugar with GmpUsers {

  val mockAuthConnector = mock[AuthConnector]
  val mockSessionService = mock[SessionService]
  val mockGmpBulkConnector = mock[GmpBulkConnector]

  object TestMoreBulkResultsController extends MoreBulkResultsController {
    val authConnector = mockAuthConnector
    override val sessionService = mockSessionService
    override val gmpBulkConnector = mockGmpBulkConnector
  }

  "MoreBulkResultsController" must {

    "respond to GET /guaranteed-minimum-pension/more-bulk-results" in {
      val result = route(FakeRequest(GET, "/guaranteed-minimum-pension/more-bulk-results"))
      status(result.get) must not equal (NOT_FOUND)
    }
  }

  val recentBulkCalculations = List(new BulkPreviousRequest("1234","abcd",LocalDate.now()), new BulkPreviousRequest("5678","efgh", LocalDate.now()))

  when(mockGmpBulkConnector.getPreviousBulkRequests()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(recentBulkCalculations))

  "more bulk results GET " must {

    "be authorised" in {
      getMoreBulkResults() { result =>
        status(result) must equal(SEE_OTHER)
        redirectLocation(result).get must include("/account/sign-in")
      }
    }

    "authenticated users" must {

      "respond with ok" in {
        withAuthorisedUser { user =>
          getMoreBulkResults(user) { result =>
            status(result) must equal(OK)
            contentAsString(result) must include(Messages("gmp.more_bulk_results.title"))
            contentAsString(result) must include(Messages("gmp.signout"))
          }
        }
      }

      "display table with more recent bulk calculation links" in {
        withAuthorisedUser { user =>
          getMoreBulkResults(user) { result =>
            status(result) must equal(OK)
            contentAsString(result) must include(Messages("gmp.more_bulk_results.header"))
          }
        }
      }

//      "load the dashboard from the bulk service if present but empty" in {
//        val dashboard = new Dashboard(Nil)
//        withAuthorisedUser { request =>
//          val result = TestDashboardController.get.apply(request)
//          contentAsString(result) must include(Messages("gmp.previous_calculations"))
//        }
//      }
//
//      "load the dashboard from the bulk service if present and complete" in {
//
//        withAuthorisedUser { request =>
//          val result = TestDashboardController.get.apply(request)
//          contentAsString(result) must include(Messages("gmp.previous_calculations"))
//          contentAsString(result) must include("1234")
//          contentAsString(result) must include("5678")
//        }
//      }
    }
  }


  def getMoreBulkResults(request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest())(handler: Future[Result] => Any): Unit = {
    handler(TestMoreBulkResultsController.retrieveMoreBulkResults.apply(request))
  }

}
