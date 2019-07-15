/*
 * Copyright 2019 HM Revenue & Customs
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

import controllers.auth.{AuthAction, GmpAuthConnector}
import metrics.Metrics
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.{OneServerPerSuite, PlaySpec}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SessionService
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

import scala.concurrent.Future

class SessionCacheControllerSpec extends PlaySpec with OneServerPerSuite with MockitoSugar with GmpUsers {

  val mockAuthConnector = mock[GmpAuthConnector]
  val mockSessionService = mock[SessionService]
  val mockAuthAction = mock[AuthAction]

  object TestSessionCacheController extends SessionCacheController(mockAuthAction, mockAuthConnector) {
    override val sessionService = mockSessionService
    override val context = FakeGmpContext
  }

  "SessionCacheController" must {

    "respond to GET /guaranteed-minimum-pension/new-calculation" in {
      val result = route(FakeRequest(GET, "/guaranteed-minimum-pension/new-calculation"))
      status(result.get) must not equal (NOT_FOUND)
    }
  }

  "new-calculation" must {

    "be an authorised route" in {
      val result = TestSessionCacheController.newCalculation.apply(FakeRequest())
      status(result) must equal(SEE_OTHER)
      redirectLocation(result).get must include("/gg/sign-in")
    }

    "reset the cached calculation parameters except for scon" in {
      withAuthorisedUser { request =>
        when(mockSessionService.resetGmpSessionWithScon()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(new SessionService(Metrics).cleanSession)))
        await(TestSessionCacheController.newCalculation.apply(request))
        verify(mockSessionService, atLeastOnce()).resetGmpSessionWithScon()(Matchers.any(), Matchers.any())
      }
    }

    "redirect to the pension details page" in {
      withAuthorisedUser { request =>
        when(mockSessionService.resetGmpSessionWithScon()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(new SessionService(Metrics).cleanSession)))
        val result = TestSessionCacheController.newCalculation.apply(request)
        status(result) must be(SEE_OTHER)
        redirectLocation(result).get must be("/guaranteed-minimum-pension/pension-details")
      }
    }

    "raise an error when the session service is unreachable" in {
      withAuthorisedUser { request =>
        when(mockSessionService.resetGmpSessionWithScon()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
        intercept[RuntimeException]{
          await(TestSessionCacheController.newCalculation.apply(request))
        }
      }
    }
  }

  "new-bulk-calculation" must {

    "be an authorised route" in {
      val result = TestSessionCacheController.newBulkCalculation.apply(FakeRequest())
      status(result) must equal(SEE_OTHER)
      redirectLocation(result).get must include("/gg/sign-in")
    }

    "reset the cached calculation parameters" in {
      withAuthorisedUser { request =>
        when(mockSessionService.resetGmpBulkSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(new SessionService(Metrics).cleanBulkSession)))
        await(TestSessionCacheController.newBulkCalculation.apply(request))
        verify(mockSessionService, atLeastOnce()).resetGmpBulkSession()(Matchers.any(), Matchers.any())
      }
    }

    "redirect to the upload csv page" in {
      withAuthorisedUser { request =>
        when(mockSessionService.resetGmpBulkSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(new SessionService(Metrics).cleanBulkSession)))
        val result = TestSessionCacheController.newBulkCalculation.apply(request)
        status(result) must be(SEE_OTHER)
        redirectLocation(result).get must be("/guaranteed-minimum-pension/upload-csv")
      }
    }

    "raise an error when the session service is unreachable" in {
      withAuthorisedUser { request =>
        when(mockSessionService.resetGmpBulkSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
        intercept[RuntimeException]{
          await(TestSessionCacheController.newBulkCalculation.apply(request))
        }
      }
    }
  }

}
