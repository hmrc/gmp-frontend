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

import java.util.UUID
import config.{ApplicationConfig, GmpSessionCache}
import connectors.GmpConnector
import controllers.auth.{AuthAction, FakeAuthAction}
import forms.PensionDetails_no_longer_used_Form
import metrics.ApplicationMetrics
import models._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import org.scalatestplus.play.PlaySpec
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.api.libs.json.Json
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{GMPSessionService, SessionService}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.SessionId
import views.Views

import scala.concurrent.{ExecutionContext, Future}

class PensionDetailsSconControllerSpec extends PlaySpec with GuiceOneServerPerSuite with MockitoSugar {

  val mockAuthConnector = mock[AuthConnector]
  val mockGMPSessionService = mock[GMPSessionService]
  val mockGmpConnector = mock[GmpConnector]
  val mockAuthAction = mock[AuthAction]
  val metrics = app.injector.instanceOf[ApplicationMetrics]
  implicit val mcc: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]
  implicit val ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]
  implicit val messagesAPI: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit val messagesProvider: MessagesImpl = MessagesImpl(Lang("en"), messagesAPI)
  implicit val ac: ApplicationConfig = app.injector.instanceOf[ApplicationConfig]
  implicit val gmpSessionCache: GmpSessionCache = app.injector.instanceOf[GmpSessionCache]
  lazy val pensionDetailsForm = new PensionDetails_no_longer_used_Form(mcc)
  lazy val views = app.injector.instanceOf[Views]

  implicit val hc: HeaderCarrier = new HeaderCarrier(sessionId = Some(SessionId(s"session-${UUID.randomUUID}")))

  object TestPensionDetailsController extends PensionDetailsController(FakeAuthAction, mockAuthConnector, mockGmpConnector,mockGMPSessionService,FakeGmpContext, metrics,ac,pensionDetailsForm,mcc,ec,gmpSessionCache,views) {
  }

  "pension details GET " must {

    "authenticated users" must {

      "respond with ok" in {
        when(mockGMPSessionService.fetchPensionDetails()(any())).thenReturn(Future.successful(None))
        val result = TestPensionDetailsController.get(FakeRequest())
            status(result) must equal(OK)
            contentAsString(result) must include(Messages("gmp.pension_details.header"))
            contentAsString(result) must include(Messages("gmp.scon"))
            contentAsString(result) must include(Messages("gmp.signout"))
            contentAsString(result) must include(Messages("gmp.back.link"))
      }

      "get page containing scon when retrieved" in {
        when(mockGMPSessionService.fetchPensionDetails()(any())).thenReturn(Future.successful(Some("S1301234T")))
        val result = TestPensionDetailsController.get(FakeRequest())
            contentAsString(result) must include("S1301234T")
      }
    }
  }

  "pension details POST " must {

    "authenticated users" must {

      val gmpSession = GmpSession(MemberDetails("", "", ""), "S1301234T", "", None, None, Leaving(GmpDate(None, None, None), None), None)

      "validate scon and store scon and redirect" in {
        when(mockGMPSessionService.cachePensionDetails(any())(any())).thenReturn(Future.successful(Some(gmpSession)))
        when(mockGmpConnector.validateScon(any(),any())(any())).thenReturn(Future.successful(ValidateSconResponse(true)))
          val result = TestPensionDetailsController.post(FakeRequest().withMethod("POST")
            .withFormUrlEncodedBody("scon" -> "S1301234T"))
          status(result) mustBe SEE_OTHER
      }

      "respond with bad request missing SCON" in {
          val result = TestPensionDetailsController.post(FakeRequest().withMethod("POST")
            .withFormUrlEncodedBody("scon" -> ""))
          status(result) mustBe BAD_REQUEST
          contentAsString(result) must include(Messages("scon.error.required"))
      }

      "respond with bad request when scon not validated" in {
        when(mockGmpConnector.validateScon(any(),any())(any())).thenReturn(Future.successful(ValidateSconResponse(false)))
          val result = TestPensionDetailsController.post(FakeRequest().withMethod("POST")
            .withFormUrlEncodedBody("scon" -> "S1301234T"))
          status(result) mustBe BAD_REQUEST
          contentAsString(result) must include(Messages("scon.error.notRecognised"))
          contentAsString(result) must include(Messages("gmp.scon.message"))
      }

      "respond with exception when scon service throws exception" in {
        when(mockGMPSessionService.cachePensionDetails(any())(any())).thenReturn(Future.successful(Some(gmpSession)))
        when(mockGmpConnector.validateScon(any(),any())(any())).thenReturn(Future.successful(null))
          intercept[RuntimeException]{
            await(TestPensionDetailsController.post(FakeRequest().withMethod("POST")
              .withFormUrlEncodedBody("scon" -> "S1301234T")))
        }
      }

      "respond with exception when cache service throws exception" in {
        when(mockGMPSessionService.cachePensionDetails(any())(any())).thenReturn(Future.successful(None))
        when(mockGmpConnector.validateScon(any(),any())(any())).thenReturn(Future.successful(ValidateSconResponse(true)))
          intercept[RuntimeException]{
            await(TestPensionDetailsController.post(FakeRequest().withMethod("POST")
              .withFormUrlEncodedBody("scon" -> "S1301234T")))
        }
      }

    }
  }

}
