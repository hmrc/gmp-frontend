/*
 * Copyright 2022 HM Revenue & Customs
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
import forms.MemberDetailsForm
import helpers.RandomNino
import models._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import org.scalatestplus.play.PlaySpec
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.api.libs.json.Json
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SessionService
import uk.gov.hmrc.auth.core.AuthConnector
import views.Views

import scala.concurrent.{ExecutionContext, Future}

class MemberDetailsControllerSpec extends PlaySpec with GuiceOneServerPerSuite with MockitoSugar {

  val mockAuthConnector = mock[AuthConnector]
  val mockSessionService = mock[SessionService]
  val mockAuthAction = mock[AuthAction]
  implicit lazy val mcc = app.injector.instanceOf[MessagesControllerComponents]
  implicit val ec = app.injector.instanceOf[ExecutionContext]
  implicit val messagesAPI=app.injector.instanceOf[MessagesApi]
  implicit val messagesProvider=MessagesImpl(Lang("en"), messagesAPI)
  implicit val ac=app.injector.instanceOf[ApplicationConfig]
  implicit val gmpSessionCache=app.injector.instanceOf[GmpSessionCache]
  lazy val form = new MemberDetailsForm(mcc)
  lazy val views = app.injector.instanceOf[Views]

  object TestMemberDetailsController extends MemberDetailsController(FakeAuthAction, mockAuthConnector,mockSessionService,
                      FakeGmpContext,mcc,ac,form,ec,gmpSessionCache,views)
  "GET" must {

    "authenticated users" must {

      "respond with ok" in {
        when(mockSessionService.fetchMemberDetails()(any())).thenReturn(Future.successful(None))
          val result = TestMemberDetailsController.get(FakeRequest())
          status(result) must equal(OK)
      }

      "present the member's details page" in {
        when(mockSessionService.fetchMemberDetails()(any())).thenReturn(Future.successful(None))
          val result = TestMemberDetailsController.get(FakeRequest())
          contentAsString(result) must include(Messages("gmp.member_details.header"))
          contentAsString(result) must include(Messages("gmp.nino"))
          contentAsString(result) must include(Messages("gmp.firstname"))
          contentAsString(result) must include(Messages("gmp.lastname"))
          contentAsString(result) must include(Messages("gmp.back.link"))
      }

      "load the details from the session storage if present" in {
        val nino = RandomNino.generate
        when(mockSessionService.fetchMemberDetails()(any())).thenReturn(Future.successful(Some
        (MemberDetails("Bob", "Jones", nino))))
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

        when(mockSessionService.fetchGmpSession()(any())).thenReturn(Future.successful(Some(session)))
        val result = TestMemberDetailsController.back(FakeRequest())
        status(result) must equal(SEE_OTHER)
    }

  }

  "POST" must {

    "authenticated users" must {

      "with valid data" must {

        val memberDetails = MemberDetails("Bob", "Jones", RandomNino.generate)
        val session = GmpSession(memberDetails, "SCON1234", "", None, None, Leaving(GmpDate(None, None, None), None), None)

        "redirect" in {
          when(mockSessionService.cacheMemberDetails(any())(any())).thenReturn(Future.successful(Some(session)))
            val result = TestMemberDetailsController.post(FakeRequest().withMethod("POST")
              .withFormUrlEncodedBody("firstForename" -> "Bob", "surname" -> "Jones",
                "nino" -> memberDetails.nino))
            status(result) mustBe SEE_OTHER
        }

        "save details to keystore" in {

          when(mockSessionService.cacheMemberDetails(any())(any())).thenReturn(Future.successful(Some(session)))
            TestMemberDetailsController.post(FakeRequest().withMethod("POST")
              .withFormUrlEncodedBody("firstForename" -> "Bob", "surname" -> "Jones",
                "nino" -> memberDetails.nino))
            verify(mockSessionService, atLeastOnce()).cacheMemberDetails(any())(any())
        }

        "respond with an exception when the session cache is unavailable" in {
          reset(mockSessionService)
          when(mockSessionService.cacheMemberDetails(any())(any())).thenReturn(Future.successful(None))
            intercept[RuntimeException]{
              await(TestMemberDetailsController.post(FakeRequest().withMethod("POST")
              .withFormUrlEncodedBody("firstForename" -> "Bob", "surname" -> "Jones",
                "nino" -> RandomNino.generate)))
          }
        }

      }

      "with invalid data" must {
        val memberDetails = MemberDetails("Bob", "Jones", RandomNino.generate)

        "respond with BAD_REQUEST" in {
            val result = TestMemberDetailsController.post(FakeRequest().withMethod("POST")
              .withFormUrlEncodedBody("firstForename" -> "", "surname" -> "Jones",
                "nino" -> memberDetails.nino))
            status(result) must equal(BAD_REQUEST)
        }

        "display the errors" in {
            val result = TestMemberDetailsController.post(FakeRequest().withMethod("POST")
              .withFormUrlEncodedBody("firstForename" -> "Bob", "surname" -> "",
                "nino" -> memberDetails.nino))
          contentAsString(result) must include(Messages("gmp.error.member.lastname.mandatory"))
        }

        "throw an exception when session not fetched" in {

            when(mockSessionService.fetchGmpSession()(any())).thenReturn(Future.successful(None))
            val result = TestMemberDetailsController.back(FakeRequest())
            intercept[RuntimeException] {
              status(result)
          }
        }
      }
    }
  }
}
