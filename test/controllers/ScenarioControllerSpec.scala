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
import helpers.RandomNino
import models._
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.{OneServerPerSuite, PlaySpec}
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SessionService

import scala.concurrent.Future

class ScenarioControllerSpec extends PlaySpec with OneServerPerSuite with MockitoSugar {

  val mockAuthConnector = mock[GmpAuthConnector]
  val mockSessionService = mock[SessionService]
  val mockAuthAction = mock[AuthAction]

  object TestScenarioController extends ScenarioController(FakeAuthAction, mockAuthConnector) {
    override val sessionService = mockSessionService
    override val context = FakeGmpContext
  }

  private val nino: String = RandomNino.generate
  val gmpSession = GmpSession(MemberDetails(nino, "A", "AAA"), "S1301234T", CalculationType.REVALUATION, None, None, Leaving(GmpDate(None, None, None), None), None)
  val emptySession = GmpSession(MemberDetails("", "", ""), "", "", None, None, Leaving(GmpDate(None, None, None), None), None)

  "ScenarioController GET" must {

    "respond with ok" in {
      when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession)))
      val result = TestScenarioController.get.apply(FakeRequest())
          status(result) must equal(OK)
          contentAsString(result) must include(Messages("gmp.scenarios.title"))
    }

    "return the correct scenarios" in {
      when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession)))
      val result = TestScenarioController.get.apply(FakeRequest())
          status(result) must equal(OK)
          contentAsString(result) must include(Messages("gmp.scenarios.payable_age"))
          contentAsString(result) must include(Messages("gmp.scenarios.spa"))
          contentAsString(result) must include(Messages("gmp.scenarios.survivor"))
          contentAsString(result) must include(Messages("gmp.scenarios.leaving"))
          contentAsString(result) must include(Messages("gmp.scenarios.specific_date"))
          contentAsString(result) must include(Messages("gmp.back.link"))
    }

    "go to failure page when session missing scon" in {
      when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(emptySession)))
        val result = TestScenarioController.get.apply(FakeRequest())
        contentAsString(result)replaceAll("&#x27;", "'") must include (Messages("gmp.cannot_calculate.gmp"))
        contentAsString(result) must include (Messages("gmp.error.session_parts_missing", "/guaranteed-minimum-pension/pension-details"))
    }

    "go to failure page when session missing member details" in {
      when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(emptySession.copy(scon="S1234567T"))))

        val result = TestScenarioController.get.apply(FakeRequest())
        contentAsString(result)replaceAll("&#x27;", "'") must include (Messages("gmp.cannot_calculate.gmp"))
        contentAsString(result) must include (Messages("gmp.error.session_parts_missing", "/guaranteed-minimum-pension/member-details"))
    }

    "go to failure page when no session" in {
      when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))

        val result = TestScenarioController.get.apply(FakeRequest())
        contentAsString(result)replaceAll("&#x27;", "'") must include (Messages("gmp.cannot_calculate.gmp"))
        contentAsString(result) must include (Messages("gmp.error.session_parts_missing", "/guaranteed-minimum-pension/dashboard"))
    }

  }

  "ScenarioController back" must {

    "redirect when authorised" in {
      val memberDetails = MemberDetails("", "", "")
      val session = GmpSession(memberDetails, "", "", None, None, Leaving(GmpDate(None, None, None), None), None)

        when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(session)))
        val result = TestScenarioController.back.apply(FakeRequest())
        status(result) must equal(SEE_OTHER)
    }

    "throw an exception when session not fetched" in {

        when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
        val result = TestScenarioController.back.apply(FakeRequest())
        intercept[RuntimeException] {
          status(result)
      }
    }


  }

  "ScenarioController POST" must {

    "respond with bad request must choose option" in {

          val result = TestScenarioController.post()(FakeRequest())

          status(result) must equal(BAD_REQUEST)
          contentAsString(result) must include(Messages("gmp.error.scenario.mandatory"))
    }

    "redirect when a choice is made" in {
      val gmpSession = GmpSession(MemberDetails("", "", ""), "", CalculationType.DOL, None, None, Leaving(GmpDate(None, None, None), None), None)

      when(mockSessionService.cacheScenario(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession)))

      val validReason = Json.toJson(CalculationType(Some(CalculationType.DOL)))

        val result = TestScenarioController.post()(FakeRequest().withJsonBody(validReason))
        status(result) must equal(SEE_OTHER)
    }

    "respond with exception when session cache fails" in {
      when(mockSessionService.cacheScenario(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
      val validReason = Json.toJson(CalculationType(Some(CalculationType.DOL)))
        intercept[RuntimeException]{
          await(TestScenarioController.post()(FakeRequest().withJsonBody(validReason)))

      }
    }
  }

  def postCalculationReason(request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest())(handler: Future[Result] => Any): Unit = {
    handler(TestScenarioController.post.apply(request))
  }

}
