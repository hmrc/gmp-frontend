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

//package controllers.auth
//
//import akka.util.Timeout
//import org.mockito.Matchers.any
//import org.mockito.Mockito.when
//import org.scalatest.mockito.MockitoSugar
//import org.scalatestplus.play.PlaySpec
//import org.scalatestplus.play.guice.GuiceOneAppPerSuite
//import play.api.mvc.{Action, AnyContent, Controller}
//import play.api.test.FakeRequest
//import uk.gov.hmrc.auth.core.AuthConnector
//
//import scala.concurrent.ExecutionContext.Implicits.global
//import scala.concurrent.Future
//import scala.concurrent.duration._
//
//class AuthActionSpec extends PlaySpec with GuiceOneAppPerSuite with MockitoSugar {
//
//  class Harness(authAction: AuthAction) extends Controller {
//    def onPageLoad(): Action[AnyContent] = authAction { request => Ok }
//  }
//
//  implicit val timeout: Timeout = 5 seconds
//
//  "Auth Action" when {
////    "the user is not logged in" must {
////      "redirect the user to log in" in {
////        val authAction = new AuthActionImpl(
////          new BrokenAuthConnector(new MissingBearerToken,
////            mock[WSHttp],
////            app.injector.instanceOf[Configuration]
////          ), app.configuration)
////        val controller = new Harness(authAction)
////        val result = controller.onPageLoad()(FakeRequest("", ""))
////        status(result) mustBe SEE_OTHER
////        redirectLocation(result).get must endWith("/auth-login-stub/gg-sign-in")
////
////      }
////    }
////    "the user has a confidence level too low " must {
////      "redirect the user to a page to enroll" in {
////        val authAction = new AuthActionImpl(
////          new BrokenAuthConnector(new InsufficientConfidenceLevel,
////            mock[WSHttp],
////            app.injector.instanceOf[Configuration]
////          ), app.configuration)
////        val controller = new Harness(authAction)
////        val result = controller.onPageLoad()(FakeRequest("", ""))
////        status(result) mustBe SEE_OTHER
////        redirectLocation(result) mustBe Some(FrontendAppConfig.saUrl)
////
////      }
////    }
//
//    "the user is authorised with psa " must {
//      "create an authenticated link for psa" in {
//        val mockAuthConnector = mock[GmpAuthConnector]
//
//        when(mockAuthConnector.authorise[Unit](any(),any())(any(), any()))
//          .thenReturn(Future.successful(()))
//
//        val authAction = new AuthActionImpl(mockAuthConnector,app.configuration)
//        val controller = new Harness(authAction)
//
//        val result = controller.onPageLoad()(FakeRequest("", ""))
//
//      }
//    }
//  }
//}
//
////class BrokenAuthConnector @Inject()(exception: Throwable, httpClient:WSHttp, configuration: Configuration) extends C2NIAuthConnector(
////  httpClient,
////  configuration) {
////  override val serviceUrl: String = ""
////
////  override def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[A] =
////    Future.failed(exception)
////}
//
