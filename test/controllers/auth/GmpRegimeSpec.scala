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

package controllers.auth

import java.util.UUID

import controllers.GmpController
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.{OneServerPerSuite, PlaySpec}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.domain.{Org, PsaId}
import uk.gov.hmrc.play.frontend.auth.Actions
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.frontend.auth.connectors.domain._
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.SessionKeys

import scala.concurrent.Future

class GmpRegimeSpec extends PlaySpec with OneServerPerSuite with MockitoSugar with BeforeAndAfterEach {

  "GmpRegime" must {

    "define isAuthorised" must {

      val accounts = mock[Accounts](RETURNS_DEEP_STUBS)

      "return true when gmp is defined" in {
        when(accounts.psa.isDefined || accounts.psp.isDefined).thenReturn(true)
        GmpRegime.isAuthorised(accounts) must be(true)
      }

      "return false when gmp is not defined" in {
        when(accounts.psa.isDefined || accounts.psp.isDefined).thenReturn(false)
        GmpRegime.isAuthorised(accounts) must be(false)
      }

    }

    "define the authentication type as the GMP GG" in {
      GmpRegime.authenticationType must be(GmpGovernmentGateway)
    }

    "define the unauthorised landing page as /unauthorised" in {
      GmpRegime.unauthorisedLandingPage.get must be("/guaranteed-minimum-pension/unauthorised")
    }
  }

  "when applying the GmpRegime" must {

    val mockAuthConnector = mock[AuthConnector]

    object TestController extends GmpController with Actions {
      val authConnector = mockAuthConnector

      def testRoute = AuthorisedFor(GmpRegime, pageVisibilityPredicate) {
        implicit user =>
          implicit request =>
            Ok
      }
    }

    "logged-in users" must {

      "with a psa account" must {

        "allow the user access to the page" in {
          val userId = s"user-${UUID.randomUUID}"
          val sessionId = s"session-${UUID.randomUUID}"
          lazy val request = FakeRequest().withSession(
            SessionKeys.sessionId -> sessionId,
            SessionKeys.token -> "RANDOMTOKEN",
            SessionKeys.userId -> userId)

          when(mockAuthConnector.currentAuthority(Matchers.any())) thenReturn {
            Future.successful(Some(Authority(userId, Accounts(psa = Some(PsaAccount("gmp/B1234567", PsaId("B1234567")))), None, None, CredentialStrength.None, ConfidenceLevel.L50, None, None)))
          }

          val result = TestController.testRoute.apply(request)
          status(result) must be(OK)
        }
      }

      "without a psa account" must {

        "redirect the user to the unauthorised page" in {

          val userId = s"user-${UUID.randomUUID}"
          val sessionId = s"session-${UUID.randomUUID}"
          lazy val request = FakeRequest().withSession(
            SessionKeys.sessionId -> sessionId,
            SessionKeys.token -> "RANDOMTOKEN",
            SessionKeys.userId -> userId)

          when(mockAuthConnector.currentAuthority(Matchers.any())) thenReturn {
            Future.successful(Some(Authority(userId, Accounts(org = Some(OrgAccount("org/1234", Org("1234")))), None, None, CredentialStrength.None, ConfidenceLevel.L50, None, None)))
          }
          val result = TestController.testRoute.apply(request)
          status(result) must be(SEE_OTHER)
          redirectLocation(result) must be(Some("/guaranteed-minimum-pension/unauthorised"))
        }
      }
    }

    "not logged-in users" must {

      "redirect to the gg sign-in page" in {
        val result = TestController.testRoute.apply(FakeRequest())
        status(result) must be(SEE_OTHER)
        redirectLocation(result).get must include("/gg/sign-in")
      }
    }
  }
}
