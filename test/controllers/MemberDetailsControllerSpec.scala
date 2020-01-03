/*
 * Copyright 2020 HM Revenue & Customs
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
import helpers.RandomNino
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

class MemberDetailsControllerSpec extends PlaySpec with OneServerPerSuite with MockitoSugar {

  val mockAuthConnector = mock[AuthConnector]
  val mockSessionService = mock[SessionService]
  val mockAuthAction = mock[AuthAction]

  object TestMemberDetailsController extends MemberDetailsController(FakeAuthAction, mockAuthConnector) {
    override val sessionService = mockSessionService
    override val context = FakeGmpContext
  }

  "GET" must {

    "authenticated users" must {

      "respond with ok" in {
        when(mockSessionService.fetchMemberDetails()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
          val result = TestMemberDetailsController.get(FakeRequest())
          status(result) must equal(OK)
      }

      "present the member's details page" in {
        when(mockSessionService.fetchMemberDetails()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
          val result = TestMemberDetailsController.get(FakeRequest())
          contentAsString(result) must include(Messages("gmp.member_details.header"))
          contentAsString(result) must include(Messages("gmp.nino"))
          contentAsString(result) must include(Messages("gmp.firstname"))
          contentAsString(result) must include(Messages("gmp.lastname"))
          contentAsString(result) must include(Messages("gmp.back.link"))
      }

      "load the details from the session storage if present" in {
        val nino = RandomNino.generate
        when(mockSessionService.fetchMemberDetails()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some
        (MemberDetails(nino, "Bob", "Jones"))))
          val result = TestMemberDetailsController.get(FakeRequest())
          contentAsString(result) must include(nino)
          contentAsString(result) must include("Bob")
          contentAsString(result) must include("Jones")
      }
    }
  }

  "BACK" must {

    "authorised users redirect" in {

      val memberDetails = MemberDetails("", "", "")
      val session = GmpSession(memberDetails, "", "", None, None, Leaving(GmpDate(None, None, None), None), None)

        when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(session)))
        val result = TestMemberDetailsController.back(FakeRequest())
        status(result) must equal(SEE_OTHER)
    }

  }

  "POST" must {

    "authenticated users" must {

      "with valid data" must {

        val memberDetails = MemberDetails(RandomNino.generate, "Bob", "Jones")
        val session = GmpSession(memberDetails, "SCON1234", "", None, None, Leaving(GmpDate(None, None, None), None), None)

        "redirect" in {
          when(mockSessionService.cacheMemberDetails(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(session)))
            val postData = Json.obj(
              "nino" -> RandomNino.generate,
              "firstForename" -> "Bob",
              "surname" -> "Jones"
            )
            val result = TestMemberDetailsController.post(FakeRequest().withJsonBody(Json.toJson(memberDetails)))
            status(result) must equal(SEE_OTHER)
        }

        "save details to keystore" in {
          when(mockSessionService.cacheMemberDetails(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(session)))
            TestMemberDetailsController.post(FakeRequest().withJsonBody(Json.toJson(memberDetails)))
            verify(mockSessionService, atLeastOnce()).cacheMemberDetails(Matchers.any())(Matchers.any(), Matchers.any())
        }

        "respond with an exception when the session cache is unavailable" in {
          reset(mockSessionService)
          when(mockSessionService.cacheMemberDetails(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
            val postData = Json.obj(
              "nino" -> RandomNino.generate,
              "firstForename" -> "Bob",
              "surname" -> "Jones"
            )
            intercept[RuntimeException]{
              await(TestMemberDetailsController.post(FakeRequest().withJsonBody(postData)))
          }
        }

      }

      "with invalid data" must {

        "respond with BAD_REQUEST" in {
            val postData = Json.obj(
              "nino" -> RandomNino.generate,
              "firstForename" -> "",
              "surname" -> "Jones"
            )
            val result = TestMemberDetailsController.post(FakeRequest().withJsonBody(postData))
            status(result) must equal(BAD_REQUEST)
        }

        "display the errors" in {
            val postData = Json.obj(
              "nino" -> RandomNino.generate,
              "firstForename" -> "Bob",
              "surname" -> ""
            )
            val result = TestMemberDetailsController.post(FakeRequest().withJsonBody(postData))
            contentAsString(result) must include(Messages("gmp.error.member.lastname.mandatory"))
        }

        "throw an exception when session not fetched" in {

            when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
            val result = TestMemberDetailsController.back(FakeRequest())
            intercept[RuntimeException] {
              status(result)
          }
        }
      }
    }
  }
}
