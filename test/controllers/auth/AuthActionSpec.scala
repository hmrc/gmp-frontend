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

package controllers.auth

import akka.util.Timeout
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status._
import play.api.mvc.{Action, AnyContent, Controller}
import play.api.test.FakeRequest
import play.api.test.Helpers.{redirectLocation, status}
import uk.gov.hmrc.auth.core._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

class AuthActionSpec extends PlaySpec with GuiceOneAppPerSuite with MockitoSugar with ScalaFutures {

  class Harness(authAction: AuthAction) extends Controller {
    def onPageLoad(): Action[AnyContent] = authAction { request => Ok.withHeaders("link" -> request.linkId) }
  }

  implicit val timeout: Timeout = 5 seconds

  "Auth Action" when {
    "the user is not logged in" must {
      "redirect the user to log in" in {

        val mockAuthConnector = mock[AuthConnector]

        when(mockAuthConnector.authorise(any(),any())(any(), any()))
          .thenReturn(Future.failed(new MissingBearerToken))

        val authAction = new AuthActionImpl(mockAuthConnector,app.configuration)
        val controller = new Harness(authAction)

        val result = controller.onPageLoad()(FakeRequest("", ""))
        status(result) mustBe SEE_OTHER
        redirectLocation(result).get must include(ExternalUrls.signIn)
      }
    }

    "the user has a confidence level too low" must {
      "redirect the user to the unauthorised page" in {

        val mockAuthConnector = mock[AuthConnector]

        when(mockAuthConnector.authorise(any(),any())(any(), any()))
          .thenReturn(Future.failed(new InsufficientConfidenceLevel))

        val authAction = new AuthActionImpl(mockAuthConnector,app.configuration)
        val controller = new Harness(authAction)

        val result = controller.onPageLoad()(FakeRequest("", ""))
        status(result) mustBe SEE_OTHER
        redirectLocation(result).get must be("/guaranteed-minimum-pension/unauthorised")
      }
    }

    "the user has no psa/psp account " must {
      "redirect the user to the unauthorised page" in {
        val mockAuthConnector = mock[AuthConnector]

        when(mockAuthConnector.authorise[Enrolments](any(), any())(any(), any()))
          .thenReturn(Future.failed(new InsufficientEnrolments))

        val authAction = new AuthActionImpl(mockAuthConnector, app.configuration)
        val controller = new Harness(authAction)

        val result = controller.onPageLoad()(FakeRequest("", ""))
        status(result) mustBe SEE_OTHER
        redirectLocation(result) must be(Some("/guaranteed-minimum-pension/unauthorised"))

      }
    }

    "the user is authorised with psa " must {
      "create an authenticated link for psa" in {
        val mockAuthConnector = mock[AuthConnector]

        val retrievalResult: Future[Enrolments] =
          Future.successful(Enrolments(Set(Enrolment("HMRC-PSA-ORG", Seq(EnrolmentIdentifier("PSAID", "someID")), ""))))

        when(mockAuthConnector.authorise[Enrolments](any(), any())(any(), any()))
          .thenReturn(retrievalResult)

        val authAction = new AuthActionImpl(mockAuthConnector, app.configuration)
        val controller = new Harness(authAction)

        val result = controller.onPageLoad()(FakeRequest("", ""))
        status(result) mustBe OK
        whenReady(result) {
          _.header.headers("link") mustBe "someID"
        }
      }
    }
      "the user is authorised with psp " must {
        "create an authenticated link for psp" in {
          val mockAuthConnector = mock[AuthConnector]

          val retrievalResult: Future[Enrolments] =
            Future.successful(Enrolments(Set(Enrolment("HMRC-PP-ORG", Seq(EnrolmentIdentifier("PPID", "someID")), ""))))

          when(mockAuthConnector.authorise[Enrolments](any(), any())(any(), any()))
            .thenReturn(retrievalResult)

          val authAction = new AuthActionImpl(mockAuthConnector, app.configuration)
          val controller = new Harness(authAction)

          val result = controller.onPageLoad()(FakeRequest("", ""))
          status(result) mustBe OK
          whenReady(result) {
            _.header.headers("link") mustBe "someID"
          }
        }
      }

    "the user is authorised with both psa and psp" must {
      "create an authenticated link for psa" in {
        val mockAuthConnector = mock[AuthConnector]

        val retrievalResult: Future[Enrolments] =
          Future.successful(
            Enrolments(
              Set(
                Enrolment("HMRC-PSA-ORG", Seq(EnrolmentIdentifier("PSAID", "somePsaID")), ""),
                Enrolment("HMRC-PP-ORG", Seq(EnrolmentIdentifier("PPID", "somePspID")), "")
              )
            )
          )

        when(mockAuthConnector.authorise[Enrolments](any(), any())(any(), any()))
          .thenReturn(retrievalResult)

        val authAction = new AuthActionImpl(mockAuthConnector, app.configuration)
        val controller = new Harness(authAction)

        val result = controller.onPageLoad()(FakeRequest("", ""))
        status(result) mustBe OK
        whenReady(result) {
          _.header.headers("link") mustBe "somePsaID"
        }
      }
    }

  }
}