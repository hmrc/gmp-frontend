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

import java.util.UUID

import config.{ApplicationConfig, GmpSessionCache}
import connectors.GmpConnector
import controllers.auth.{AuthAction, FakeAuthAction}
import forms.PensionDetailsForm
import metrics.ApplicationMetrics
import models._
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
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.logging.SessionId

import scala.concurrent.{ExecutionContext, Future}

class PensionDetailsControllerSpec extends PlaySpec with OneServerPerSuite with MockitoSugar {

  val mockAuthConnector = mock[AuthConnector]
  val mockSessionService = mock[SessionService]
  val mockGmpConnector = mock[GmpConnector]
  val mockAuthAction = mock[AuthAction]
  val metrics = app.injector.instanceOf[ApplicationMetrics]
  implicit lazy val mcc = app.injector.instanceOf[MessagesControllerComponents]
  implicit val ec = app.injector.instanceOf[ExecutionContext]
  implicit val messagesAPI=app.injector.instanceOf[MessagesApi]
  implicit val messagesProvider=MessagesImpl(Lang("en"), messagesAPI)
  implicit val ac=app.injector.instanceOf[ApplicationConfig]
  implicit val gmpSessionCache=app.injector.instanceOf[GmpSessionCache]
  lazy val pensionDetailsForm = new PensionDetailsForm(mcc)


  implicit val hc = new HeaderCarrier(sessionId = Some(SessionId(s"session-${UUID.randomUUID}")))

  object TestPensionDetailsController extends PensionDetailsController(FakeAuthAction, mockAuthConnector, mockGmpConnector,mockSessionService,FakeGmpContext, metrics,ac,pensionDetailsForm,mcc,ec,gmpSessionCache) {
   /* override val sessionService = mockSessionService
    override val context = FakeGmpContext*/
  }

  "pension details GET " must {

    "authenticated users" must {

      "respond with ok" in {
        when(mockSessionService.fetchPensionDetails()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
        val result = TestPensionDetailsController.get(FakeRequest())
            status(result) must equal(OK)
            contentAsString(result) must include(Messages("gmp.pension_details.header"))
            contentAsString(result) must include(Messages("gmp.scon"))
            contentAsString(result) must include(Messages("gmp.signout"))
            contentAsString(result) must include(Messages("gmp.back.link"))
            contentAsString(result) must include(Messages("gmp.scon.message"))
      }

      "get page containing scon when retrieved" in {
        when(mockSessionService.fetchPensionDetails()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some("S1301234T")))
        val result = TestPensionDetailsController.get(FakeRequest())
            contentAsString(result) must include("S1301234T")
      }
    }
  }

  "pension details POST " must {

    "authenticated users" must {

      val validGmpRequest = PensionDetails("S1301234T")
      val emptySconGmpRequest = PensionDetails("")
      val gmpSession = GmpSession(MemberDetails("", "", ""), "S1301234T", "", None, None, Leaving(GmpDate(None, None, None), None), None)

      "validate scon and store scon and redirect" in {
        when(mockSessionService.cachePensionDetails(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession)))
        when(mockGmpConnector.validateScon(Matchers.any(),Matchers.any())(Matchers.any())).thenReturn(Future.successful(ValidateSconResponse(true)))
          val result = TestPensionDetailsController.post()(FakeRequest().withJsonBody(Json.toJson(validGmpRequest)))
          status(result) must equal(SEE_OTHER)
      }

      "respond with bad request missing SCON" in {
          val result = TestPensionDetailsController.post()(FakeRequest().withJsonBody(Json.toJson(emptySconGmpRequest)))
          status(result) must equal(BAD_REQUEST)
          contentAsString(result) must include(Messages("gmp.error.mandatory.new", Messages("gmp.scon")))
      }

      "respond with bad request when scon not validated" in {
        when(mockGmpConnector.validateScon(Matchers.any(),Matchers.any())(Matchers.any())).thenReturn(Future.successful(ValidateSconResponse(false)))
          val result = TestPensionDetailsController.post()(FakeRequest().withJsonBody(Json.toJson(validGmpRequest)))
          status(result) must equal(BAD_REQUEST)
          contentAsString(result) must include(Messages("gmp.error.scon.nps_invalid", Messages("gmp.scon")))
      }

      "respond with exception when scon service throws exception" in {
        when(mockSessionService.cachePensionDetails(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession)))
        when(mockGmpConnector.validateScon(Matchers.any(),Matchers.any())(Matchers.any())).thenReturn(Future.successful(null))
          intercept[RuntimeException]{
            await(TestPensionDetailsController.post()(FakeRequest().withJsonBody(Json.toJson(validGmpRequest))))
        }
      }

      "respond with exception when cache service throws exception" in {
        when(mockSessionService.cachePensionDetails(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
        when(mockGmpConnector.validateScon(Matchers.any(),Matchers.any())(Matchers.any())).thenReturn(Future.successful(ValidateSconResponse(true)))
          intercept[RuntimeException]{
            await(TestPensionDetailsController.post()(FakeRequest().withJsonBody(Json.toJson(validGmpRequest))))
        }
      }

    }
  }

}
