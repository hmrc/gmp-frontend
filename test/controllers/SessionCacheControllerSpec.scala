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

import config.{ApplicationConfig, GmpSessionCache}
import controllers.auth.{AuthAction, FakeAuthAction}
import metrics.ApplicationMetrics
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.i18n.{Lang, MessagesApi, MessagesImpl}
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SessionService
import uk.gov.hmrc.auth.core.AuthConnector

import scala.concurrent.{ExecutionContext, Future}

class SessionCacheControllerSpec extends PlaySpec with GuiceOneServerPerSuite with MockitoSugar {

  val mockAuthConnector = mock[AuthConnector]
  val mockSessionService = mock[SessionService]
  val mockAuthAction = mock[AuthAction]
  val metrics = app.injector.instanceOf[ApplicationMetrics]
  implicit val mcc = app.injector.instanceOf[MessagesControllerComponents]
  implicit val ec = app.injector.instanceOf[ExecutionContext]
  implicit val messagesAPI=app.injector.instanceOf[MessagesApi]
  implicit val messagesProvider=MessagesImpl(Lang("en"), messagesAPI)
  implicit val ac=app.injector.instanceOf[ApplicationConfig]
  implicit val gmpSessionCache=app.injector.instanceOf[GmpSessionCache]


  object TestSessionCacheController extends SessionCacheController(FakeAuthAction, mockAuthConnector,ac,mockSessionService,FakeGmpContext,mcc,ec,gmpSessionCache) {
 /*   override val sessionService = mockSessionService
    override val context = FakeGmpContext
*/  }

  "new-calculation" must {

    "reset the cached calculation parameters except for scon" in {
        when(mockSessionService.resetGmpSessionWithScon()(Matchers.any())).thenReturn(Future.successful(Some(new SessionService(metrics, gmpSessionCache).cleanSession)))
        await(TestSessionCacheController.newCalculation(FakeRequest()))
        verify(mockSessionService, atLeastOnce()).resetGmpSessionWithScon()(Matchers.any())
    }

    "redirect to the pension details page" in {
        when(mockSessionService.resetGmpSessionWithScon()(Matchers.any())).thenReturn(Future.successful(Some(new SessionService(metrics, gmpSessionCache).cleanSession)))
        val result = TestSessionCacheController.newCalculation(FakeRequest())
        status(result) must be(SEE_OTHER)
        redirectLocation(result).get must be("/guaranteed-minimum-pension/pension-details")
    }

    "raise an error when the session service is unreachable" in {

        when(mockSessionService.resetGmpSessionWithScon()(Matchers.any())).thenReturn(Future.successful(None))
        intercept[RuntimeException]{
          await(TestSessionCacheController.newCalculation(FakeRequest()))
      }
    }
  }

  "new-bulk-calculation" must {

    "reset the cached calculation parameters" in {

        when(mockSessionService.resetGmpBulkSession()(Matchers.any())).thenReturn(Future.successful(Some(new SessionService(metrics, gmpSessionCache).cleanBulkSession)))
        await(TestSessionCacheController.newBulkCalculation(FakeRequest()))
        verify(mockSessionService, atLeastOnce()).resetGmpBulkSession()(Matchers.any())
    }

    "redirect to the upload csv page" in {

        when(mockSessionService.resetGmpBulkSession()(Matchers.any())).thenReturn(Future.successful(Some(new SessionService(metrics, gmpSessionCache).cleanBulkSession)))
        val result = TestSessionCacheController.newBulkCalculation(FakeRequest())
        status(result) must be(SEE_OTHER)
        redirectLocation(result).get must be("/guaranteed-minimum-pension/upload-csv")
    }

    "raise an error when the session service is unreachable" in {

        when(mockSessionService.resetGmpBulkSession()(Matchers.any())).thenReturn(Future.successful(None))
        intercept[RuntimeException]{
          await(TestSessionCacheController.newBulkCalculation(FakeRequest()))
      }
    }
  }
}
