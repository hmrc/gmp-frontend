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
import play.api.mvc.{Result, AnyContentAsEmpty}
import play.api.test.FakeRequest
import play.api.test.Helpers._

import services.SessionService
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

import scala.concurrent.Future

class EqualiseControllerSpec extends PlaySpec with OneServerPerSuite with MockitoSugar with GmpUsers {

  val mockAuthConnector = mock[AuthConnector]
  val mockSessionService = mock[SessionService]

  object TestEqualiseController extends EqualiseController {
    override val authConnector = mockAuthConnector
    override val sessionService = mockSessionService
  }

  "EqualiseController GET" must {

    "respond to GET /guaranteed-minimum-pension/equalise" in {
      val result = route(FakeRequest(GET, "/guaranteed-minimum-pension/equalise"))
      status(result.get) must not equal (NOT_FOUND)
    }

    "be authorised" in {
      getEqualise() { result =>
        status(result) must equal(SEE_OTHER)
        redirectLocation(result).get must include("/account/sign-in")
      }
    }

    "respond with ok" in {

      withAuthorisedUser { user =>
        getEqualise(user) { result =>
          status(result) must equal(OK)
          contentAsString(result) must include(Messages("gmp.equalise.title"))
        }
      }
    }

    "present the equalise page" in {
      when(mockSessionService.fetchMemberDetails()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
      withAuthorisedUser { request =>
        val result = TestEqualiseController.get.apply(request)
        contentAsString(result) must include(Messages("gmp.equalise_header"))
        contentAsString(result) must include(Messages("gmp.equalise_subheader"))
        contentAsString(result) must include(Messages("gmp.back_to_dashboard"))
      }
    }


  }

  "BACK" must {

    "be authorised" in {
      val result = TestEqualiseController.back.apply(FakeRequest())
      status(result) must equal(SEE_OTHER)
      redirectLocation(result).get must include("/account/sign-in")
    }


    "authorised users redirect" in {

      val memberDetails = MemberDetails("", "", "")
      val session = GmpSession(memberDetails, "", CalculationType.REVALUATION, None, None, Leaving(GmpDate(None, None, None), None), None, Dashboard(List()))

      withAuthorisedUser { request =>
        when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(session)))
        val result = TestEqualiseController.back.apply(request)
        status(result) must equal(SEE_OTHER)
      }
    }

    "throw an exception when session not fetched" in {

      withAuthorisedUser { request =>
        when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
        val result = TestEqualiseController.back.apply(request)
        intercept[RuntimeException] {
          status(result)
        }
      }
    }

  }

  val gmpSession = GmpSession(MemberDetails("", "", ""), "S1301234T", "", None, Some(""), Leaving(GmpDate(None, None, None), None), equalise = Some(1), Dashboard(List()))
  "EqualiseController POST" must {

    "with invalid data" must {

      "be authorised" in {
        val result = TestEqualiseController.post.apply(FakeRequest())
        status(result) must equal(SEE_OTHER)
        redirectLocation(result).get must include("/account/sign-in")
      }

      "authenticated users" must {

        "with invalid data" must {

          "respond with bad request must choose option" in {
            withAuthorisedUser {
              request =>
                val result = TestEqualiseController.post()(request)

                status(result) must equal(BAD_REQUEST)
                contentAsString(result) must include(Messages("gmp.error.reason.mandatory"))
            }
          }
        }

        "with valid data" must {

          val gmpSession = GmpSession(MemberDetails("", "", ""), "S1301234T", CalculationType.REVALUATION, None, Some(""), Leaving(GmpDate(None, None, None), None), None, Dashboard(List()))

          "redirect" in {

            when(mockSessionService.cacheEqualise(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession)))
            withAuthorisedUser { request =>
              val postData = Json.toJson(
                Equalise(Some(1))
              )
              val result = TestEqualiseController.post.apply(request.withJsonBody(postData))
              status(result) must equal(SEE_OTHER)
            }
          }

          "respond with error when rate not stored" in {
            when(mockSessionService.cacheEqualise(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
            withAuthorisedUser { request =>
              val postData = Json.toJson(
                Equalise(Some(1))
              )
              intercept[RuntimeException] {
                await(TestEqualiseController.post.apply(request.withJsonBody(postData)))
              }
            }
          }
        }

      }
    }
  }

  def getEqualise(request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest())(handler: Future[Result] => Any): Unit = {
    handler(TestEqualiseController.get.apply(request))
  }

}
