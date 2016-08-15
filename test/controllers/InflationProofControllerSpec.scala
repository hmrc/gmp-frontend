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

package controllers

import models._
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.{OneServerPerSuite, PlaySpec}
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SessionService
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

import scala.concurrent.Future

class InflationProofControllerSpec extends PlaySpec with OneServerPerSuite with MockitoSugar with GmpUsers {

  val mockAuthConnector = mock[AuthConnector]
  val mockSessionService = mock[SessionService]

  object TestInflationProofController extends InflationProofController {
    val authConnector = mockAuthConnector
    override val sessionService = mockSessionService
  }

  "InflationProofController" must {

    "respond to GET /guaranteed-minimum-pension/inflation-proof" in {
      val result = route(FakeRequest(GET, "/guaranteed-minimum-pension/inflation-proof"))
      status(result.get) must not equal (NOT_FOUND)
    }

    "respond to POST /guaranteed-minimum-pension/inflation-proof" in {
      val result = route(FakeRequest(POST, "/guaranteed-minimum-pension/inflation-proof"))
      status(result.get) must not equal (NOT_FOUND)
    }

    "respond to GET /guaranteed-minimum-pension/inflation-proof/back" in {
      val result = route(FakeRequest(GET, "/guaranteed-minimum-pension/inflation-proof/back"))
      status(result.get) must not equal (NOT_FOUND)
    }

    "GET" must {

      "be authorised" in {
        val result = TestInflationProofController.get.apply(FakeRequest())
        status(result) must equal(SEE_OTHER)
        redirectLocation(result).get must include("/gg/sign-in")
      }

      "authorised users" must {

        "load the inflation proofing page" in {
          withAuthorisedUser { request =>
            val result = TestInflationProofController.get.apply(request)
            status(result) must equal(OK)
            contentAsString(result) must include(Messages("gmp.inflation_proof.question"))
            contentAsString(result) must include(Messages("gmp.back_to_dashboard"))
            contentAsString(result) must include(Messages("gmp.check_gmp.button"))
          }
        }
      }
    }

    "POST" must {

      "be authorised" in {
        val result = TestInflationProofController.post.apply(FakeRequest())
        status(result) must equal(SEE_OTHER)
        redirectLocation(result).get must include("/gg/sign-in")
      }

      "authorised users" must {

        "with valid data" must {

          val revaluationDate = GmpDate(Some("1"), Some("1"), Some("2000"))
          val inflationProof = InflationProof(revaluationDate, Some("Yes"))
          val session = GmpSession(MemberDetails("", "", ""), "", "3", Some(revaluationDate), None, Leaving(GmpDate(None, None, None), None), None, Dashboard(List()))

          "redirect to the results" in {
            withAuthorisedUser { request =>
              when(mockSessionService.cacheRevaluationDate(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(session)))
              val result = TestInflationProofController.post.apply(request.withJsonBody(Json.toJson(inflationProof)))
              status(result) must equal(SEE_OTHER)
              redirectLocation(result).get must be(routes.ResultsController.get().url)
            }
          }

          "redirect to the results when not revaluated" in {
            withAuthorisedUser { request =>
              when(mockSessionService.cacheRevaluationDate(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(session)))
              val result = TestInflationProofController.post.apply(request.withJsonBody(Json.toJson(inflationProof.copy(revaluate = Some("No")))))
              status(result) must equal(SEE_OTHER)
              redirectLocation(result).get must be(routes.ResultsController.get().url)
            }
          }

          "save revaluation date to session cache" in {
            withAuthorisedUser { request =>
              when(mockSessionService.cacheRevaluationDate(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(session)))
              val result = TestInflationProofController.post.apply(request.withJsonBody(Json.toJson(inflationProof)))
              verify(mockSessionService, atLeastOnce()).cacheRevaluationDate(Matchers.any())(Matchers.any(), Matchers.any())
            }
          }

          "respond with an exception when the session cache is unavailable" in {
            reset(mockSessionService)
            when(mockSessionService.cacheRevaluationDate(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
            withAuthorisedUser { request =>
              intercept[RuntimeException] {
                await(TestInflationProofController.post.apply(request.withJsonBody(Json.toJson(inflationProof))))
              }
            }
          }
        }

        "with invalid data" must {

          val revaluationDate = GmpDate(Some("a"), Some("b"), Some("c"))
          val inflationProof = InflationProof(revaluationDate, Some("yes"))

          "respond with BAD_REQUEST" in {
            withAuthorisedUser { request =>
              val result = TestInflationProofController.post.apply(request.withJsonBody(Json.toJson(inflationProof)))
              status(result) must equal(BAD_REQUEST)
            }
          }

          "display the errors" in {
            withAuthorisedUser { request =>
              val result = TestInflationProofController.post.apply(request.withJsonBody(Json.toJson(inflationProof)))
              contentAsString(result) must include(Messages("gmp.error.date.nonnumber"))
            }
          }
        }
      }
    }

    "back" must {

      "be authorised" in {
        val result = TestInflationProofController.back.apply(FakeRequest())
        status(result) must equal(SEE_OTHER)
        redirectLocation(result).get must include("/gg/sign-in")
      }

      "throw an exception when session not fetched" in {

        withAuthorisedUser { request =>
          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
          val result = TestInflationProofController.back.apply(request)
          intercept[RuntimeException] {
            status(result)
          }
        }
      }

      "redirect to the termination date page if the member has not left the scheme" in {
        val revaluationDate = GmpDate(Some("1"), Some("1"), Some("2000"))
        val session = GmpSession(MemberDetails("", "", ""), "", "3", Some(revaluationDate), None, Leaving(GmpDate(None, None, None), Some(Leaving.NO)), None, Dashboard(List()))
        withAuthorisedUser { request =>
          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(session)))
          val result = TestInflationProofController.back.apply(request)
          status(result) must equal(SEE_OTHER)
          redirectLocation(result).get must be(routes.DateOfLeavingController.get().url)
        }
      }

      "redirect to the revaluation rate page if the member has left the scheme before 5/4/16" in {
        val revaluationDate = GmpDate(Some("1"), Some("1"), Some("2000"))
        val session = GmpSession(MemberDetails("", "", ""), "", "3", Some(revaluationDate), None, Leaving(GmpDate(None, None, None), Some(Leaving.YES_BEFORE)), None, Dashboard(List()))
        withAuthorisedUser { request =>
          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(session)))
          val result = TestInflationProofController.back.apply(request)
          status(result) must equal(SEE_OTHER)
          redirectLocation(result).get must be(routes.RevaluationRateController.get().url)
        }
      }

      "redirect to the revaluation rate page if the member has left the scheme after 5/4/16" in {
        val revaluationDate = GmpDate(Some("1"), Some("1"), Some("2000"))
        val session = GmpSession(MemberDetails("", "", ""), "", "3", Some(revaluationDate), None, Leaving(GmpDate(None, None, None), Some(Leaving.YES_AFTER)), None, Dashboard(List()))
        withAuthorisedUser { request =>
          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(session)))
          val result = TestInflationProofController.back.apply(request)
          status(result) must equal(SEE_OTHER)
          redirectLocation(result).get must be(routes.RevaluationRateController.get().url)
        }
      }
    }
  }
}
