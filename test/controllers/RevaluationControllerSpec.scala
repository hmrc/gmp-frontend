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

import controllers.auth.{AuthAction, FakeAuthAction, GmpAuthConnector}
import models._
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.{OneServerPerSuite, PlaySpec}
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SessionService
import uk.gov.hmrc.auth.core.AuthConnector

import scala.concurrent.Future

class RevaluationControllerSpec extends PlaySpec with OneServerPerSuite with MockitoSugar {

  val mockAuthConnector = mock[AuthConnector]
  val mockSessionService = mock[SessionService]
  val mockAuthAction = mock[AuthAction]

  val baseValidDate = GmpDate(day = Some("31"), month = Some("1"), year = Some("2015"))

  object TestRevaluationController extends RevaluationController(FakeAuthAction, mockAuthConnector) {
    override val sessionService = mockSessionService
    override val context = FakeGmpContext
  }

  "Revaluation controller" must {

    "authenticated users" must {

      "respond with ok" in {

          when(mockSessionService.fetchLeaving()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(Leaving(GmpDate(None, None, None), None))))
          val result = TestRevaluationController.get(FakeRequest())
            status(result) must equal(OK)
            contentAsString(result) must include("When would you like the calculation made to?")
            contentAsString(result) must include(Messages("gmp.revaluation.question"))
            contentAsString(result) must include(Messages("gmp.back.link"))
      }

      "respond with ok when no leaving" in {
        when(mockSessionService.fetchLeaving()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
        val result = TestRevaluationController.get(FakeRequest())
            status(result) must equal(OK)
            contentAsString(result) must include("When would you like the calculation made to?")
            contentAsString(result) must include(Messages("gmp.revaluation.question"))
      }
    }
  }

  "RevaluationController back" must {

    "authenticated users" must {

      val memberDetails = MemberDetails("", "", "")
      val session = GmpSession(memberDetails, "", CalculationType.REVALUATION, None, None, Leaving(GmpDate(None, None, None), None), None)

      "redirect to the date of leaving page" in {

          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(session)))
          val result = TestRevaluationController.back(FakeRequest())
          status(result) must equal(SEE_OTHER)
          redirectLocation(result).get must include("/left-scheme")
      }
    }

    "throw an exception when session not fetched" in {

        when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
        val result = TestRevaluationController.back(FakeRequest())
        intercept[RuntimeException] {
          status(result)
      }
    }

  }

  "Revaluation controller POST" must {

    "authenticated users" must {

      "with invalid data" must {

        "respond with BAD_REQUEST" in {
            val postData = Json.toJson(
              RevaluationDate(Leaving(GmpDate(None, None, None), None), baseValidDate.copy(day = Some("31"), month = Some("2"), year = Some("2015")))
            )
            val result = TestRevaluationController.post(FakeRequest().withJsonBody(postData))
            status(result) must equal(BAD_REQUEST)
        }

        "display the errors" in {

            val postData = Json.toJson(
              RevaluationDate(Leaving(GmpDate(None, None, None), None), baseValidDate.copy(day = Some("31"), month = Some("2"), year = Some("2015")))
            )
            val result = TestRevaluationController.post(FakeRequest().withJsonBody(postData))
            contentAsString(result) must include(Messages("gmp.error.date.invalid"))
        }
      }

      "with valid data" must {

        val gmpSession = GmpSession(MemberDetails("", "", ""), "S1301234T", CalculationType.REVALUATION, None, Some(""), Leaving(GmpDate(None, None, None), None), None)

        "redirect" in {

          when(mockSessionService.cacheRevaluationDate(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession)))
            val postData = Json.toJson(
              RevaluationDate(Leaving(GmpDate(None, None, None), None), baseValidDate.copy(day = Some("31"), month = Some("3"), year = Some("2015")))
            )
            val result = TestRevaluationController.post(FakeRequest().withJsonBody(postData))
            status(result) must equal(SEE_OTHER)
        }


        "respond with error when rate not stored" in {
          when(mockSessionService.cacheRevaluationDate(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
            val postData = Json.toJson(
              RevaluationDate(Leaving(GmpDate(None, None, None), None), baseValidDate.copy(day = Some("31"), month = Some("3"), year = Some("2015")))
            )
            intercept[RuntimeException] {
              await(TestRevaluationController.post(FakeRequest().withJsonBody(postData)))
          }
        }
      }
    }
  }
}
