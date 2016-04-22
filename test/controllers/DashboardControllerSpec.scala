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

class DashboardControllerSpec extends PlaySpec with OneServerPerSuite with MockitoSugar with GmpUsers {

  val mockAuthConnector = mock[AuthConnector]
  val mockSessionService = mock[SessionService]


  object TestDashboardController extends DashboardController {
    val authConnector = mockAuthConnector
    override val sessionService = mockSessionService
  }

  "DashboardController" must {

    "respond to GET /guaranteed-minimum-pension/dashboard" in {
      val result = route(FakeRequest(GET, "/guaranteed-minimum-pension/dashboard"))
      status(result.get) must not equal (NOT_FOUND)
    }
  }

  "dashboard GET " must {

    "be authorised" in {
      getDashboard() { result =>
        status(result) must equal(SEE_OTHER)
        redirectLocation(result).get must include("/account/sign-in")
      }
    }

    "authenticated users" must {

      "respond with ok" in {
        when(mockSessionService.fetchDashboard()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
        withAuthorisedUser { user =>
          getDashboard(user) { result =>
            status(result) must equal(OK)
            contentAsString(result) must include(Messages("gmp.dashboard.title"))
            contentAsString(result) must include(Messages("gmp.signout"))
          }
        }
      }
    }
  }

  def getDashboard(request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest())(handler: Future[Result] => Any): Unit = {
    handler(TestDashboardController.get.apply(request))
  }

}
