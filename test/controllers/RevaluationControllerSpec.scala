/*
 * Copyright 2018 HM Revenue & Customs
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
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SessionService
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import play.api.i18n.Messages.Implicits._
import scala.concurrent.Future

class RevaluationControllerSpec extends PlaySpec with OneServerPerSuite with MockitoSugar with GmpUsers {

  val mockAuthConnector = mock[AuthConnector]
  val mockSessionService = mock[SessionService]

  val baseValidDate = GmpDate(day = Some("31"), month = Some("1"), year = Some("2015"))

  object TestRevaluationController extends RevaluationController {
    val authConnector = mockAuthConnector
    override val sessionService = mockSessionService
    override val context = FakeGmpContext()
  }

  "Revaluation controller" must {

    "respond to GET /guaranteed-minimum-pension/relevant-date" in {
      val result = route(FakeRequest(GET, "/guaranteed-minimum-pension/relevant-date"))
      status(result.get) must not equal (NOT_FOUND)
    }

    "respond to POST /guaranteed-minimum-pension/relevant-date" in {
      val result = route(FakeRequest(POST, "/guaranteed-minimum-pension/relevant-date"))
      status(result.get) must not equal (NOT_FOUND)
    }

    "respond to GET /guaranteed-minimum-pension/relevant-date/back" in {
      val result = route(FakeRequest(GET, "/guaranteed-minimum-pension/relevant-date/back"))
      status(result.get) must not equal (NOT_FOUND)
    }
  }

  "Revaluation controller GET " must {

    "be authorised" in {
      get() { result =>
        status(result) must equal(SEE_OTHER)
        redirectLocation(result).get must include("/gg/sign-in")
      }
    }

    "authenticated users" must {

      "respond with ok" in {
        withAuthorisedUser { user =>
          when(mockSessionService.fetchLeaving()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(Leaving(GmpDate(None, None, None), None))))
          get(user) { result =>
            status(result) must equal(OK)
            contentAsString(result) must include("At which date would you like the calculation to apply? - Guaranteed Minimum Pension - GOV.UK")
            contentAsString(result) must include(Messages("gmp.revaluation.question"))
            contentAsString(result) must include(Messages("gmp.back.link"))
          }
        }
      }

      "respond with ok when no leaving" in {
        when(mockSessionService.fetchLeaving()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
        withAuthorisedUser { user =>
          get(user) { result =>
            status(result) must equal(OK)
            contentAsString(result) must include("At which date would you like the calculation to apply? - Guaranteed Minimum Pension - GOV.UK")
            contentAsString(result) must include(Messages("gmp.revaluation.question"))

          }
        }
      }
    }
  }

  "RevaluationController back" must {

    "be authorised" in {
      val result = TestRevaluationController.get.apply(FakeRequest())
      status(result) must equal(SEE_OTHER)
      redirectLocation(result).get must include("/gg/sign-in")
    }

    "authenticated users" must {

      val memberDetails = MemberDetails("", "", "")
      val session = GmpSession(memberDetails, "", CalculationType.REVALUATION, None, None, Leaving(GmpDate(None, None, None), None), None)

      "redirect to the date of leaving page" in {
        withAuthorisedUser { request =>
          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(session)))
          val result = TestRevaluationController.back.apply(request)
          status(result) must equal(SEE_OTHER)
          redirectLocation(result).get must include("/left-scheme")
        }
      }
    }

    "throw an exception when session not fetched" in {

      withAuthorisedUser { request =>
        when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
        val result = TestRevaluationController.back.apply(request)
        intercept[RuntimeException] {
          status(result)
        }
      }
    }

  }

  "Revaluation controller POST" must {
    "be authorised" in {
      val result = TestRevaluationController.get.apply(FakeRequest())
      status(result) must equal(SEE_OTHER)
      redirectLocation(result).get must include("/gg/sign-in")
    }

    "authenticated users" must {

      "with invalid data" must {

        "respond with BAD_REQUEST" in {
          withAuthorisedUser { request =>
            val postData = Json.toJson(
              RevaluationDate(baseValidDate.copy(day = Some("31"), month = Some("2"), year = Some("2015")), Leaving(GmpDate(None, None, None), None))
            )
            val result = TestRevaluationController.post.apply(request.withJsonBody(postData))
            status(result) must equal(BAD_REQUEST)
          }
        }

        "display the errors" in {
          withAuthorisedUser { request =>
            val postData = Json.toJson(
              RevaluationDate(baseValidDate.copy(day = Some("31"), month = Some("2"), year = Some("2015")), Leaving(GmpDate(None, None, None), None))
            )
            val result = TestRevaluationController.post.apply(request.withJsonBody(postData))
            contentAsString(result) must include(Messages("gmp.error.date.invalid"))
          }
        }
      }

      "with valid data" must {

        val gmpSession = GmpSession(MemberDetails("", "", ""), "S1301234T", CalculationType.REVALUATION, None, Some(""), Leaving(GmpDate(None, None, None), None), None)

        "redirect" in {

          when(mockSessionService.cacheRevaluationDate(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession)))
          withAuthorisedUser { request =>
            val postData = Json.toJson(
              RevaluationDate(baseValidDate.copy(day = Some("31"), month = Some("3"), year = Some("2015")), Leaving(GmpDate(None, None, None), None))
            )
            val result = TestRevaluationController.post.apply(request.withJsonBody(postData))
            status(result) must equal(SEE_OTHER)
          }
        }


        "respond with error when rate not stored" in {
          when(mockSessionService.cacheRevaluationDate(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
          withAuthorisedUser { request =>
            val postData = Json.toJson(
              RevaluationDate(baseValidDate.copy(day = Some("31"), month = Some("3"), year = Some("2015")), Leaving(GmpDate(None, None, None), None))
            )
            intercept[RuntimeException] {
              await(TestRevaluationController.post.apply(request.withJsonBody(postData)))
            }
          }
        }
      }
    }
  }

  def get(request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest())(handler: Future[Result] => Any): Unit = {
    handler(TestRevaluationController.get.apply(request))
  }

}
