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
import forms.RevaluationForm
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
import services.{GMPSessionService, SessionService}
import uk.gov.hmrc.auth.core.AuthConnector
import views.Views

import scala.concurrent.{ExecutionContext, Future}

class RevaluationControllerSpec extends PlaySpec with GuiceOneServerPerSuite with MockitoSugar {

  val mockAuthConnector = mock[AuthConnector]
  val mockGMPSessionService = mock[GMPSessionService]
  val mockAuthAction = mock[AuthAction]
  implicit val mcc: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]
  implicit val ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]
  implicit val messagesAPI: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit val messagesProvider: MessagesImpl = MessagesImpl(Lang("en"), messagesAPI)
  implicit val ac: ApplicationConfig = app.injector.instanceOf[ApplicationConfig]
  implicit val gmpSessionCache: GmpSessionCache = app.injector.instanceOf[GmpSessionCache]
  lazy val revaluationForm = new RevaluationForm(mcc)
  lazy val views = app.injector.instanceOf[Views]

  val gmpSession = GmpSession(
    MemberDetails("firstForename", "surname", "AB1234567C"),
    scon = "S2730000B",
    scenario = "02",
    revaluationDate = None,
    rate = None,
    leaving = Leaving(GmpDate(None, None, None), None),
    equalise = None
  )

  val baseValidDate = GmpDate(day = Some("31"), month = Some("1"), year = Some("2015"))

  object TestRevaluationController extends RevaluationController(FakeAuthAction, mockAuthConnector, mockGMPSessionService,
    FakeGmpContext, mcc, ac, revaluationForm, ec, gmpSessionCache, views)

  "Revaluation controller" must {

    "authenticated users" must {

      "respond with ok" in {

        when(mockGMPSessionService.fetchGmpSession()(any())).thenReturn(Future.successful
        (Some(gmpSession)))
        val result = TestRevaluationController.get(FakeRequest())
        status(result) must equal(OK)
        contentAsString(result) must include("When would you like the calculation made to?")
        contentAsString(result) must include(Messages("gmp.revaluation.question"))
        contentAsString(result) must include(Messages("gmp.back.link"))
      }

      "respond with ok when no leaving" in {
        when(mockGMPSessionService.fetchLeaving()(any())).thenReturn(Future.successful(None))
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
      val session = GmpSession(memberDetails, "", CalculationType.REVALUATION, None, None,
        Leaving(GmpDate(None, None, None), None), None)

      "redirect to the date of leaving page" in {

        when(mockGMPSessionService.fetchGmpSession()(any())).thenReturn(Future.successful(Some(session)))
        val result = TestRevaluationController.back(FakeRequest())
        status(result) must equal(SEE_OTHER)
        redirectLocation(result).get must include("/left-scheme")
      }
    }

    "throw an exception when session not fetched" in {

      when(mockGMPSessionService.fetchGmpSession()(any())).thenReturn(Future.successful(None))
      val result = TestRevaluationController.back(FakeRequest())
      intercept[RuntimeException] {
        status(result)
      }
    }

  }

  "Revaluation controller POST" must {

    "authenticated users" must {

      "with invalid data" must {
        val gmpSession = GmpSession(MemberDetails("", "", ""), "S1301234T", CalculationType.REVALUATION, None, Some(""),
          Leaving(GmpDate(None, None, None), None), None)

        "respond with BAD_REQUEST" in {

          when(mockGMPSessionService.fetchGmpSession()(any())).thenReturn(Future.successful(Some(gmpSession)))
          val result = TestRevaluationController.post(FakeRequest().withMethod("POST")
            .withFormUrlEncodedBody("RevaluationDate.Leaving.leavingDate.GmpDate" -> "",
              "RevaluationDate.revaluationDate" -> "31, 2, 2015"))
          status(result) mustBe (BAD_REQUEST)
        }

        "display the errors" in {
          when(mockGMPSessionService.fetchGmpSession()(any())).thenReturn(Future.successful(Some(gmpSession)))
          val result = TestRevaluationController.post(FakeRequest().withMethod("POST")
            .withFormUrlEncodedBody("leaving.leavingDate.day" -> "", "leaving.leavingDate.month" -> "",
              "leaving.leavingDate.year" -> "", "leaving.leaving" -> "", "revaluationDate.day" -> "31",
              "revaluationDate.month" -> "2", "revaluationDate.year" -> "2015"))

          contentAsString(result) must include(Messages("gmp.error.date.invalid"))
        }
      }

      "with valid data" must {

        val gmpSession = GmpSession(MemberDetails("", "", ""), "S1301234T", CalculationType.REVALUATION, None, Some(""),
          Leaving(GmpDate(None, None, None), None), None)

        "redirect" in {
          when(mockGMPSessionService.fetchGmpSession()(any())).thenReturn(Future.successful(Some(gmpSession)))
          when(mockGMPSessionService.cacheRevaluationDate(any())(any())).thenReturn(Future.successful(Some(gmpSession)))

          val result = TestRevaluationController.post(FakeRequest().withMethod("POST")
            .withFormUrlEncodedBody("leaving.leavingDate.day" -> "", "leaving.leavingDate.month" -> "",
              "leaving.leavingDate.year" -> "", "leaving.leaving" -> "", "revaluationDate.day" -> "31",
              "revaluationDate.month" -> "3", "revaluationDate.year" -> "2015"))
          status(result) must equal(SEE_OTHER)
        }


        "respond with error when rate not stored" in {
          when(mockGMPSessionService.cacheRevaluationDate(any())(any())).thenReturn(Future.successful(None))

          intercept[RuntimeException] {
            await(TestRevaluationController.post(FakeRequest().withMethod("POST")
              .withFormUrlEncodedBody("leaving.leavingDate.day" -> "", "leaving.leavingDate.month" -> "",
                "leaving.leavingDate.year" -> "", "leaving.leaving" -> "", "revaluationDate.day" -> "31",
                "revaluationDate.month" -> "3", "revaluationDate.year" -> "2015")))
          }
        }
      }
    }
  }

}