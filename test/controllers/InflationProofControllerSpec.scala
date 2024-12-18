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

import config.{ApplicationConfig, GmpSessionCache}
import controllers.auth.{AuthAction, FakeAuthAction}
import forms.InflationProofForm
import models._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.api.libs.json.Json
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{GMPSessionService, SessionService}
import uk.gov.hmrc.auth.core.AuthConnector
import views.Views

import scala.concurrent.{ExecutionContext, Future}

class InflationProofControllerSpec extends PlaySpec with GuiceOneServerPerSuite with MockitoSugar {

  val mockAuthConnector = mock[AuthConnector]
  val mockGMPSessionService = mock[GMPSessionService]
  val mockAuthAction = mock[AuthAction]
  implicit val mcc: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]
  implicit val ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]
  implicit val messagesAPI: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit val messagesProvider: MessagesImpl = MessagesImpl(Lang("en"), messagesAPI)
  implicit val ac: ApplicationConfig = app.injector.instanceOf[ApplicationConfig]
  implicit val gmpSessionCache: GmpSessionCache = app.injector.instanceOf[GmpSessionCache]
  lazy val inflationProofForm = new InflationProofForm(mcc)
  lazy val views = app.injector.instanceOf[Views]

  object TestInflationProofController extends InflationProofController(FakeAuthAction, mockAuthConnector,mockGMPSessionService,
    FakeGmpContext,mcc,inflationProofForm,ac,ec,gmpSessionCache,views)


  "InflationProofController" must {

    "GET" must {

      "authorised users" must {

        "load the inflation proofing page" in {
            val result = TestInflationProofController.get(FakeRequest())
            status(result) must equal(OK)
            contentAsString(result) must include(Messages("gmp.inflation_proof.question"))
            contentAsString(result) must include(Messages("gmp.back.link"))
            contentAsString(result) must include(Messages("gmp.continue.button"))
        }
      }
    }

    "POST" must {

      "authorised users" must {

        "with valid data" must {

          val revaluationDate = GmpDate(Some("1"), Some("1"), Some("2000"))
          val session = GmpSession(MemberDetails("", "", ""), "", "3", Some(revaluationDate), None, Leaving(GmpDate(None, None, None), None), None)

          "redirect to the results" in {

              when(mockGMPSessionService.cacheRevaluationDate(any())(any())).thenReturn(Future.successful(Some(session)))
              val result = TestInflationProofController.post(FakeRequest().withMethod("POST")
                .withFormUrlEncodedBody("gmpDate" -> "inflationProof.revaluationDate", "revaluate" -> "inflationProof.revaluate"))
              status(result) mustBe SEE_OTHER
              redirectLocation(result).get must be(routes.ResultsController.get.url)

          }

          "redirect to the results when not revaluated" in {

              when(mockGMPSessionService.cacheRevaluationDate(any())(any())).thenReturn(Future.successful(Some(session)))
              val result = TestInflationProofController.post(FakeRequest().withMethod("POST")
                .withFormUrlEncodedBody("gmpDate" -> "inflationProof.revaluationDate", "revaluate" -> "Some('No')"))
              status(result) mustBe(SEE_OTHER)
              redirectLocation(result).get must be(routes.ResultsController.get.url)

          }

          "save revaluation date to session cache" in {

              when(mockGMPSessionService.cacheRevaluationDate(any())(any())).thenReturn(Future.successful(Some(session)))
              TestInflationProofController.post(FakeRequest().withMethod("POST")
                .withFormUrlEncodedBody("gmpDate" -> "inflationProof.revaluationDate", "revaluate" -> "inflationProof.revaluate"))
              verify(mockGMPSessionService, atLeastOnce()).cacheRevaluationDate(any())(any())

          }

          "respond with an exception when the session cache is unavailable" in {
            reset(mockGMPSessionService)
            when(mockGMPSessionService.cacheRevaluationDate(any())(any())).thenReturn(Future.successful(None))
              intercept[RuntimeException] {
                await(TestInflationProofController.post(FakeRequest().withMethod("POST")
                  .withFormUrlEncodedBody("gmpDate" -> "inflationProof.revaluationDate", "revaluate" -> "inflationProof.revaluate")))
            }
          }
        }

        "with invalid data" must {

          val badGmpDate = "a,b,c"
          "respond with BAD_REQUEST" in {

              val result = TestInflationProofController.post(FakeRequest().withMethod("POST")
                .withFormUrlEncodedBody("revaluationDate" -> badGmpDate, "revaluate" -> "Yes"))
            status(result) mustBe(BAD_REQUEST)
          }

          "display the errors" in {
              val result = TestInflationProofController.post(FakeRequest().withMethod("POST")
                .withFormUrlEncodedBody("revaluationDate.day" -> "a", "revaluationDate.month" -> "b",
                  "revaluationDate.year" -> "c", "revaluate" -> "Yes"))
              contentAsString(result) must include(Messages("revaluationDate.day"))
          }
        }
      }
    }

    "back" must {

      "throw an exception when session not fetched" in {

          when(mockGMPSessionService.fetchGmpSession()(any())).thenReturn(Future.successful(None))
          val result = TestInflationProofController.back(FakeRequest())
          intercept[RuntimeException] {
            status(result)
        }
      }

      "redirect to the termination date page if the member has not left the scheme" in {
        val revaluationDate = GmpDate(Some("1"), Some("1"), Some("2000"))
        val session = GmpSession(MemberDetails("", "", ""), "", "3", Some(revaluationDate), None, Leaving(GmpDate(None, None, None), Some(Leaving.NO)), None)
          when(mockGMPSessionService.fetchGmpSession()(any())).thenReturn(Future.successful(Some(session)))
          val result = TestInflationProofController.back(FakeRequest())
          status(result) must equal(SEE_OTHER)
          redirectLocation(result).get must be(routes.DateOfLeavingController.get.url)
      }

      "redirect to the revaluation rate page if the member has left the scheme before 5/4/16" in {
        val revaluationDate = GmpDate(Some("1"), Some("1"), Some("2000"))
        val session = GmpSession(MemberDetails("", "", ""), "", "3", Some(revaluationDate), None, Leaving(GmpDate(None, None, None), Some(Leaving.YES_BEFORE)), None)
          when(mockGMPSessionService.fetchGmpSession()(any())).thenReturn(Future.successful(Some(session)))
          val result = TestInflationProofController.back(FakeRequest())
          status(result) must equal(SEE_OTHER)
          redirectLocation(result).get must be(routes.RevaluationRateController.get.url)
      }

      "redirect to the revaluation rate page if the member has left the scheme after 5/4/16" in {
        val revaluationDate = GmpDate(Some("1"), Some("1"), Some("2000"))
        val session = GmpSession(MemberDetails("", "", ""), "", "3", Some(revaluationDate), None, Leaving(GmpDate(None, None, None), Some(Leaving.YES_AFTER)), None)
          when(mockGMPSessionService.fetchGmpSession()(any())).thenReturn(Future.successful(Some(session)))
          val result = TestInflationProofController.back(FakeRequest())
          status(result) must equal(SEE_OTHER)
          redirectLocation(result).get must be(routes.RevaluationRateController.get.url)
      }
    }
  }
}
