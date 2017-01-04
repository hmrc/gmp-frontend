/*
 * Copyright 2017 HM Revenue & Customs
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

import config.GmpContext
import helpers.RandomNino
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

class RevaluationRateControllerSpec extends PlaySpec with OneServerPerSuite with MockitoSugar with GmpUsers {

  val mockAuthConnector = mock[AuthConnector]
  val mockSessionService = mock[SessionService]

  object TestRevaluationRateController extends RevaluationRateController {
    val authConnector = mockAuthConnector
    override val sessionService = mockSessionService
    override val context = FakeGmpContext()
  }

  private val nino: String = RandomNino.generate

  "Revaluation Rate controller" must {

    "respond to GET /guaranteed-minimum-pension/revaluation-rate" in {
      val result = route(FakeRequest(GET, "/guaranteed-minimum-pension/revaluation-rate"))
      status(result.get) must not equal (NOT_FOUND)
    }
  }


  "Revaluation Rate controller GET " must {

    "be authorised" in {
      get() { result =>
        status(result) must equal(SEE_OTHER)
        redirectLocation(result).get must include("/gg/sign-in")
      }
    }

    "authenticated users" must {

      val gmpSession = GmpSession(MemberDetails(nino, "A", "AAA"), "S1301234T", CalculationType.REVALUATION, None, Some(""), Leaving(GmpDate(None, None, None), Some(Leaving.NO)), None)
      when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession)))
      "respond with ok" in {

        withAuthorisedUser { user =>
          get(user) { result =>
            status(result) must equal(OK)
            contentAsString(result) must include(Messages("gmp.revaluation_rate.header"))
            contentAsString(result) must include(Messages("gmp.back_to_dashboard"))
          }
        }
      }

      "throw an exception when session not fetched" in {
        when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
        withAuthorisedUser { user =>
          get(user) { result =>

            intercept[RuntimeException] {
              contentAsString(result)
            }

          }
        }
      }

      "go to failure page when session missing scon" in {
        val emptySession = GmpSession(MemberDetails("", "", ""), "", "", None, None, Leaving(GmpDate(None, None, None), Some(Leaving.NO)), None)
        when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(emptySession)))
        withAuthorisedUser { request =>
          val result = TestRevaluationRateController.get.apply(request)
          contentAsString(result)replaceAll("&#x27;", "'") must include (Messages("gmp.cannot_calculate.gmp"))
          contentAsString(result) must include (Messages("gmp.error.session_parts_missing", "/guaranteed-minimum-pension/pension-details"))
        }
      }

      "go to failure page when session missing nino" in {
        val emptySession = GmpSession(MemberDetails("", "", ""), "S1234567T", "", None, None, Leaving(GmpDate(None, None, None), None), None)
        when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(emptySession)))
        withAuthorisedUser { request =>
          val result = TestRevaluationRateController.get.apply(request)
          contentAsString(result)replaceAll("&#x27;", "'") must include (Messages("gmp.cannot_calculate.gmp"))
          contentAsString(result) must include (Messages("gmp.error.session_parts_missing", "/guaranteed-minimum-pension/member-details"))
        }
      }

      "go to failure page when session missing firstname" in {
        val emptySession = GmpSession(MemberDetails(nino, "", ""), "S1234567T", "", None, None, Leaving(GmpDate(None, None, None), None), None)
        when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(emptySession)))
        withAuthorisedUser { request =>
          val result = TestRevaluationRateController.get.apply(request)
          contentAsString(result)replaceAll("&#x27;", "'") must include (Messages("gmp.cannot_calculate.gmp"))
          contentAsString(result) must include (Messages("gmp.error.session_parts_missing", "/guaranteed-minimum-pension/member-details"))
        }
      }

      "go to failure page when session missing lastname" in {
        val emptySession = GmpSession(MemberDetails(nino, "A", ""), "S1234567T", "", None, None, Leaving(GmpDate(None, None, None), None), None)
        when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(emptySession)))
        withAuthorisedUser { request =>
          val result = TestRevaluationRateController.get.apply(request)
          contentAsString(result)replaceAll("&#x27;", "'") must include (Messages("gmp.cannot_calculate.gmp"))
          contentAsString(result) must include (Messages("gmp.error.session_parts_missing", "/guaranteed-minimum-pension/member-details"))
        }
      }

      "go to failure page when session missing scenario" in {
        val emptySession = GmpSession(MemberDetails(nino, "A", "AAA"), "S1234567T", "", None, None, Leaving(GmpDate(None, None, None), None), None)
        when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(emptySession)))
        withAuthorisedUser { request =>
          val result = TestRevaluationRateController.get.apply(request)
          contentAsString(result)replaceAll("&#x27;", "'") must include (Messages("gmp.cannot_calculate.gmp"))
          contentAsString(result) must include (Messages("gmp.error.session_parts_missing", "/guaranteed-minimum-pension/calculation-reason"))
        }
      }

      "go to failure page when session missing leaving" in {
        val emptySession = GmpSession(MemberDetails(nino, "A", "AAA"), "S1234567T", "0", None, None, Leaving(GmpDate(None, None, None), None), None)
        when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(emptySession)))
        withAuthorisedUser { request =>
          val result = TestRevaluationRateController.get.apply(request)
          contentAsString(result)replaceAll("&#x27;", "'") must include (Messages("gmp.cannot_calculate.gmp"))
          contentAsString(result) must include (Messages("gmp.error.session_parts_missing", "/guaranteed-minimum-pension/left-scheme"))
        }
      }

      "include the correct header when Payable Date scenario selected" in {
        when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession.copy(scenario = CalculationType.PAYABLE_AGE))))
        withAuthorisedUser { user =>
          get(user) { result =>
            contentAsString(result) must include(Messages("gmp.revaluation_rate_spa.header"))
          }
        }
      }

      "include the correct header when Survivor scenario selected" in {
        when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession.copy(scenario = CalculationType.SURVIVOR))))
        withAuthorisedUser { user =>
          get(user) { result =>
            contentAsString(result) must include(Messages("gmp.revaluation_rate_spa.header"))
          }
        }
      }

      "include the correct header when non Payable Date scenario selected" in {
        when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession.copy(scenario = CalculationType.DOL))))
        withAuthorisedUser { user =>
          get(user) { result =>
            contentAsString(result) must include(Messages("gmp.revaluation_rate.header"))
          }
        }
      }

      "include the correct header when non revaluation Scenario pre 06/04/2016 selected" in {
        val gmpSession = GmpSession(MemberDetails(nino, "A", "AAA"), "S1234567T", CalculationType.REVALUATION, Some(GmpDate(Some("1"), Some("1"), Some("2015"))), None, Leaving(GmpDate(None, None, None), Some(Leaving.YES_BEFORE)), None)
        when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession)))
        withAuthorisedUser { user =>
          get(user) { result =>
            contentAsString(result) must include(Messages("gmp.revaluation_rate_spa.header"))
          }
        }
      }

      "include the correct header when non revaluation Scenario after 06/04/2016 selected" in {
        val gmpSession = GmpSession(MemberDetails(nino, "A", "AAA"), "S1234567T", CalculationType.REVALUATION, Some(GmpDate(Some("1"), Some("1"), Some("2018"))), None, Leaving(GmpDate(None, None, None), Some(Leaving.YES_AFTER)), None)
        when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession)))
        withAuthorisedUser { user =>
          get(user) { result =>
            contentAsString(result) must include(Messages("gmp.revaluation_rate.header"))
          }
        }
      }

      "not show limited rate option when leaving date after 06/04/2016 selected" in {
        val gmpSession = GmpSession(MemberDetails(nino, "A", "AAA"), "S1234567T", CalculationType.SPA, None, None, Leaving(GmpDate(Some("1"), Some("5"), Some("2016")), Some(Leaving.YES_AFTER)), None)
        when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession)))
        withAuthorisedUser { user =>
          get(user) { result =>
            contentAsString(result) must not include (Messages("gmp.revaluation_rate.limited"))
          }
        }
      }

      "show limited rate option when leaving date before 06/04/2016 selected" in {
        val gmpSession = GmpSession(MemberDetails(nino, "A", "AAA"), "S1234567T", CalculationType.SPA, None, None, Leaving(GmpDate(None, None, None), Some(Leaving.YES_BEFORE)), None)
        when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession)))
        withAuthorisedUser { user =>
          get(user) { result =>
            contentAsString(result) must include (Messages("gmp.revaluation_rate.limited"))
          }
        }
      }
    }
  }


  "Revaluation controller POST" must {

    when(mockSessionService.fetchScenario()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))

    "be authorised" in {
      val result = TestRevaluationRateController.get.apply(FakeRequest())
      status(result) must equal(SEE_OTHER)
      redirectLocation(result).get must include("/gg/sign-in")
    }

    "authenticated users" must {

      "with invalid data" must {

        "respond with BAD_REQUEST" in {
          withAuthorisedUser { request =>
            val postData = Json.toJson(
              RevaluationRate(None)
            )
            val result = TestRevaluationRateController.post.apply(request.withJsonBody(postData))
            status(result) must equal(BAD_REQUEST)
          }
        }

        "display the errors" in {
          withAuthorisedUser { request =>
            val postData = Json.toJson(
              RevaluationRate(None)
            )
            val result = TestRevaluationRateController.post.apply(request.withJsonBody(postData))
            contentAsString(result) must include(Messages("gmp.error.reason.mandatory"))
          }
        }

        "respond with error when can't get session" in {
          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
          withAuthorisedUser { request =>
            val postData = Json.toJson(
              RevaluationRate(None)
            )
            intercept[RuntimeException] {
              await(TestRevaluationRateController.post.apply(request.withJsonBody(postData)))
            }
          }
        }

      }
      "with valid data" must {

        val gmpSession = GmpSession(MemberDetails("", "", ""), "S1301234T", CalculationType.REVALUATION, None, Some(""), Leaving(GmpDate(None, None, None), Some(Leaving.NO)), None)

        "redirect" in {

          when(mockSessionService.cacheRevaluationRate(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession)))
          withAuthorisedUser { request =>
            val postData = Json.toJson(
              RevaluationRate(Some("hmrc"))
            )
            val result = TestRevaluationRateController.post.apply(request.withJsonBody(postData))
            status(result) must equal(SEE_OTHER)
          }
        }


        "respond with error when rate not stored" in {
          when(mockSessionService.cacheRevaluationRate(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
          withAuthorisedUser { request =>
            val postData = Json.toJson(
              RevaluationRate(Some("hmrc"))
            )
            intercept[RuntimeException] {
              await(TestRevaluationRateController.post.apply(request.withJsonBody(postData)))
            }
          }
        }
      }
    }
  }

  "BACK" must {

    "be authorised" in {
      val result = TestRevaluationRateController.back.apply(FakeRequest())
      status(result) must equal(SEE_OTHER)
      redirectLocation(result).get must include("/gg/sign-in")
    }

    "authorised users redirect" in {

      val memberDetails = MemberDetails("", "", "")
      val session = GmpSession(memberDetails, "", CalculationType.PAYABLE_AGE, None, None, Leaving(GmpDate(None, None, None), None), None)

      withAuthorisedUser { request =>
        when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(session)))
        val result = TestRevaluationRateController.back.apply(request)
        status(result) must equal(SEE_OTHER)
      }
    }

    "throw an exception when session not fetched" in {

      withAuthorisedUser { request =>
        when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
        val result = TestRevaluationRateController.back.apply(request)
        intercept[RuntimeException] {
          status(result) must equal(SEE_OTHER)
        }
      }
    }


  }

  def get(request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest())(handler: Future[Result] => Any): Unit = {
    handler(TestRevaluationRateController.get.apply(request))
  }

}
