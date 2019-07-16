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

import java.util.UUID

import connectors.GmpConnector
import controllers.auth.{AuthAction, FakeAuthAction, GmpAuthConnector}
import metrics.Metrics
import models._
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.{OneServerPerSuite, PlaySpec}
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SessionService
import uk.gov.hmrc.domain.PsaId
import uk.gov.hmrc.play.frontend.auth.AuthContext
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.frontend.auth.connectors.domain._
import play.api.i18n.Messages.Implicits._

import scala.concurrent.Future
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.logging.SessionId

class PensionDetailsControllerSpec extends PlaySpec with OneServerPerSuite with MockitoSugar with GmpUsers {

  val mockAuthConnector = mock[GmpAuthConnector]
  val mockSessionService = mock[SessionService]
  val mockGmpConnector = mock[GmpConnector]
  val mockAuthAction = mock[AuthAction]


  implicit val user = AuthContext(authority = Authority("1234", Accounts(psa = Some(PsaAccount("link", PsaId("B1234567")))), None, None, CredentialStrength.None, ConfidenceLevel.L50, None, None, None, ""))
  implicit val hc = new HeaderCarrier(sessionId = Some(SessionId(s"session-${UUID.randomUUID}")))

  object TestPensionDetailsController extends PensionDetailsController(FakeAuthAction, mockAuthConnector, mockGmpConnector, Metrics) {
    override val sessionService = mockSessionService
    override val context = FakeGmpContext
  }

  "pension details GET " must {

    "authenticated users" must {

      "respond with ok" in {
        when(mockSessionService.fetchPensionDetails()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
        val result = TestPensionDetailsController.get.apply(FakeRequest())
            status(result) must equal(OK)
            contentAsString(result) must include(Messages("gmp.pension_details.header"))
            contentAsString(result) must include(Messages("gmp.scon"))
            contentAsString(result) must include(Messages("gmp.signout"))
            contentAsString(result) must include(Messages("gmp.back.link"))
            contentAsString(result) must include(Messages("gmp.scon.message"))
      }

      "get page containing scon when retrieved" in {
        when(mockSessionService.fetchPensionDetails()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some("S1301234T")))
        val result = TestPensionDetailsController.get.apply(FakeRequest())
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
