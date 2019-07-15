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
import com.google.inject.Inject
import config.WSHttp
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.{Configuration, Environment}
import play.api.mvc.{Action, AnyContent, Controller}
import play.api.test.FakeRequest
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.auth.core.{AuthConnector, ConfidenceLevel, Enrolment, EnrolmentIdentifier, Enrolments, InsufficientConfidenceLevel, MissingBearerToken}
import uk.gov.hmrc.http.HeaderCarrier
import play.api.test.Helpers.{redirectLocation, status}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import play.api.Configuration
import play.api.http.Status._

class AuthActionSpec extends PlaySpec with GuiceOneAppPerSuite with MockitoSugar {

  class Harness(authAction: AuthAction) extends Controller {
    def onPageLoad(): Action[AnyContent] = authAction { request => Ok }
  }

  implicit val timeout: Timeout = 5 seconds

  "Auth Action" when {
    "the user is not logged in" must {
      "redirect the user to log in" in {
        val authAction = new AuthActionImpl(
          new BrokenAuthConnector(new MissingBearerToken,
            mock[WSHttp],
            app.injector.instanceOf[Configuration],
            app.injector.instanceOf[Environment]
          ), app.configuration)
        val controller = new Harness(authAction)
        val result = controller.onPageLoad()(FakeRequest("", ""))
        status(result) mustBe SEE_OTHER
        redirectLocation(result).get must endWith("sign-in")

      }
    }
//    "the user has a confidence level too low " must {
//      "redirect the user to a page to enroll" in {
//        val authAction = new AuthActionImpl(
//          new BrokenAuthConnector(new InsufficientConfidenceLevel,
//            mock[WSHttp],
//            app.injector.instanceOf[Configuration],
//            app.injector.instanceOf[Environment]
//          ), app.configuration)
//        val controller = new Harness(authAction)
//        val result = controller.onPageLoad()(FakeRequest("", ""))
//        status(result) mustBe SEE_OTHER
//        redirectLocation(result) mustBe Some(FrontendAppConfig.saUrl)
//
//      }
//    }

    "the user is authorised with psa " must {
      "create an authenticated link for psa" in {
        val mockAuthConnector = mock[GmpAuthConnector]

        val retrievalResult: Future[Enrolments] =
          Future.successful(Enrolments(Set(Enrolment("HMRC-PSA-ORG",Seq(EnrolmentIdentifier("PSAID", "someID")),""))))

        when(mockAuthConnector.authorise[Enrolments](any(),any())(any(), any()))
          .thenReturn(retrievalResult)



        val authAction = new AuthActionImpl(mockAuthConnector,app.configuration)
        val controller = new Harness(authAction)

        val result = controller.onPageLoad()(FakeRequest("", ""))
        status(result) mustBe OK
      }
    }
  }
}

class BrokenAuthConnector @Inject()(exception: Throwable, httpClient:WSHttp, configuration: Configuration, environment: Environment) extends GmpAuthConnector(
  httpClient,
  environment,
  configuration) {
  override val serviceUrl: String = ""

  override def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[A] =
    Future.failed(exception)
}

