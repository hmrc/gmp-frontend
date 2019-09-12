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

import controllers.auth.{AuthAction, FakeAuthAction}
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

class EqualiseControllerSpec extends PlaySpec with OneServerPerSuite with MockitoSugar {

  val mockAuthConnector = mock[AuthConnector]
  val mockSessionService = mock[SessionService]
  val mockAuthAction = mock[AuthAction]

  object TestEqualiseController extends EqualiseController(FakeAuthAction, mockAuthConnector, mockSessionService) {
    override val context = FakeGmpContext
  }

  "EqualiseController GET" must {

    "respond with ok" in {

      val result = TestEqualiseController.get(FakeRequest())
      status(result) must equal(OK)
      contentAsString(result) must include(Messages("gmp.equalise_header"))
    }

    "present the equalise page" in {
      when(mockSessionService.fetchMemberDetails()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))

        val result = TestEqualiseController.get(FakeRequest())
        contentAsString(result) must include(Messages("gmp.equalise_header"))
        contentAsString(result) must include(Messages("gmp.back.link"))
        contentAsString(result) must include(Messages("gmp.continue.button"))
    }

  }

  "BACK" must {

    "authorised users redirect" in {

      val memberDetails = MemberDetails("", "", "")
      val session = GmpSession(memberDetails, "", CalculationType.REVALUATION, None, None, Leaving(GmpDate(None, None, None), None), None)

        when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(session)))
        val result = TestEqualiseController.back(FakeRequest())
        status(result) must equal(SEE_OTHER)
    }

    "throw an exception when session not fetched" in {

        when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
        val result = TestEqualiseController.back(FakeRequest())
        intercept[RuntimeException] {
          status(result)
      }
    }

  }

  val gmpSession = GmpSession(MemberDetails("", "", ""), "S1301234T", "", None, Some(""), Leaving(GmpDate(None, None, None), None), equalise = Some(1))
  "EqualiseController POST" must {

    "with invalid data" must {

      "authenticated users" must {

        "with invalid data" must {

          "respond with bad request must choose option" in {

                val result = TestEqualiseController.post()(FakeRequest())

                status(result) must equal(BAD_REQUEST)
                contentAsString(result) must include(Messages("gmp.error.equalise.error_message"))
          }
        }

        "with valid data" must {

          val gmpSession = GmpSession(MemberDetails("", "", ""), "S1301234T", CalculationType.REVALUATION, None, Some(""), Leaving(GmpDate(None, None, None), None), None)

          "redirect" in {

            when(mockSessionService.cacheEqualise(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession)))

              val postData = Json.toJson(
                Equalise(Some(1))
              )
              val result = TestEqualiseController.post(FakeRequest().withJsonBody(postData))
              status(result) must equal(SEE_OTHER)
          }

          "respond with error when rate not stored" in {
            when(mockSessionService.cacheEqualise(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
              val postData = Json.toJson(
                Equalise(Some(1))
              )
              intercept[RuntimeException] {
                await(TestEqualiseController.post(FakeRequest().withJsonBody(postData)))
            }
          }
        }

      }
    }
  }
}
