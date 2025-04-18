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
import forms.RevaluationRateForm
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
import services.{GMPSessionService, SessionService}
import uk.gov.hmrc.auth.core.AuthConnector
import views.Views

import scala.concurrent.{ExecutionContext, Future}

class RevaluationRateControllerSpec extends PlaySpec with GuiceOneServerPerSuite with MockitoSugar {

  val mockAuthConnector = mock[AuthConnector]
  val mockSessionService = mock[GMPSessionService]
  val mockAuthAction = mock[AuthAction]

  implicit val mcc: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]
  implicit val ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]
  implicit val messagesAPI: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit val messagesProvider: MessagesImpl = MessagesImpl(Lang("en"), messagesAPI)
  implicit val ac: ApplicationConfig = app.injector.instanceOf[ApplicationConfig]
  implicit val gmpSessionCache: GmpSessionCache = app.injector.instanceOf[GmpSessionCache]
  lazy val revaluationRateForm = new RevaluationRateForm(mcc)
  lazy val views = app.injector.instanceOf[Views]

  object TestRevaluationRateController extends RevaluationRateController(FakeAuthAction,
    mockAuthConnector,ac,mockSessionService,FakeGmpContext,mcc,revaluationRateForm,ec,gmpSessionCache,views)


  private val nino: String = RandomNino.generate


  "Revaluation Rate controller GET " must {

    "authenticated users" must {

      val gmpSession = GmpSession(MemberDetails(nino, "A", "AAA"), "S1301234T", CalculationType.REVALUATION, None,
        Some(""), Leaving(GmpDate(None, None, None), Some(Leaving.NO)), None)
      when(mockSessionService.fetchGmpSession()(any())).thenReturn(Future.successful(Some(gmpSession)))
      "respond with ok" in {

        val result = TestRevaluationRateController.get(FakeRequest())
            status(result) must equal(OK)
            contentAsString(result) must include(Messages("gmp.revaluation_rate.header"))
            contentAsString(result) must include(Messages("gmp.back.link"))
      }

      "throw an exception when session not fetched" in {
        when(mockSessionService.fetchGmpSession()(any())).thenReturn(Future.successful(None))
        val result = TestRevaluationRateController.get(FakeRequest())
            intercept[RuntimeException] {
              contentAsString(result)
            }
      }

      "go to failure page when session missing scon" in {
        val emptySession = GmpSession(MemberDetails("", "", ""), "", "", None, None, Leaving(GmpDate(None, None, None),
          Some(Leaving.NO)), None)
        when(mockSessionService.fetchGmpSession()(any())).thenReturn(Future.successful(Some(emptySession)))
          val result = TestRevaluationRateController.get(FakeRequest())
          contentAsString(result)replaceAll("&#x27;", "'") must include (Messages("gmp.cannot_calculate.gmp"))
          contentAsString(result) must include (Messages("gmp.error.session_parts_missing",
            "/guaranteed-minimum-pension/pension-details"))
      }

      "go to failure page when session missing nino" in {
        val emptySession = GmpSession(MemberDetails("", "", ""), "S1234567T", "", None, None,
          Leaving(GmpDate(None, None, None), None), None)
        when(mockSessionService.fetchGmpSession()(any())).thenReturn(Future.successful(Some(emptySession)))

          val result = TestRevaluationRateController.get(FakeRequest())
          contentAsString(result)replaceAll("&#x27;", "'") must include (Messages("gmp.cannot_calculate.gmp"))
          contentAsString(result) must include (Messages("gmp.error.session_parts_missing",
            "/guaranteed-minimum-pension/member-details"))
      }

      "go to failure page when session missing firstname" in {

        val emptySession = GmpSession(MemberDetails(nino, "", ""), "S1234567T", "", None, None,
          Leaving(GmpDate(None, None, None), None), None)

        when(mockSessionService.fetchGmpSession()(any())).thenReturn(Future.successful(Some(emptySession)))

          val result = TestRevaluationRateController.get(FakeRequest())
          contentAsString(result)replaceAll("&#x27;", "'") must include (Messages("gmp.cannot_calculate.gmp"))
          contentAsString(result) must include (Messages("gmp.error.session_parts_missing",
            "/guaranteed-minimum-pension/member-details"))
      }


      "go to failure page when session missing lastname" in {
        val emptySession = GmpSession(MemberDetails(nino, "A", ""), "S1234567T", "", None, None, Leaving(GmpDate(None,
          None, None), None), None)
        when(mockSessionService.fetchGmpSession()(any())).thenReturn(Future.successful(Some(emptySession)))

          val result = TestRevaluationRateController.get(FakeRequest())
          contentAsString(result)replaceAll("&#x27;", "'") must include (Messages("gmp.cannot_calculate.gmp"))
          contentAsString(result) must include (Messages("gmp.error.session_parts_missing",
            "/guaranteed-minimum-pension/member-details"))
      }

      "go to failure page when session missing scenario" in {
        val emptySession = GmpSession(MemberDetails(nino, "A", "AAA"), "S1234567T", "", None, None,
          Leaving(GmpDate(None, None, None), None), None)
        when(mockSessionService.fetchGmpSession()(any())).thenReturn(Future.successful(Some(emptySession)))

          val result = TestRevaluationRateController.get(FakeRequest())
          contentAsString(result)replaceAll("&#x27;", "'") must include (Messages("gmp.cannot_calculate.gmp"))
          contentAsString(result) must include (Messages("gmp.error.session_parts_missing",
            "/guaranteed-minimum-pension/calculation-reason"))
      }

      "go to failure page when session missing leaving" in {
        val emptySession = GmpSession(MemberDetails(nino, "A", "AAA"), "S1234567T", "0", None, None,
          Leaving(GmpDate(None, None, None), None), None)
        when(mockSessionService.fetchGmpSession()(any())).thenReturn(Future.successful(Some(emptySession)))

          val result = TestRevaluationRateController.get(FakeRequest())
          contentAsString(result)replaceAll("&#x27;", "'") must include (Messages("gmp.cannot_calculate.gmp"))
          contentAsString(result) must include (Messages("gmp.error.session_parts_missing",
            "/guaranteed-minimum-pension/left-scheme"))
      }

      "include the correct header when Payable Date scenario selected" in {
        when(mockSessionService.fetchGmpSession()(any())).thenReturn(Future.successful(Some(gmpSession
          .copy(scenario = CalculationType.PAYABLE_AGE))))
        val result = TestRevaluationRateController.get(FakeRequest())
            contentAsString(result) must include(Messages("gmp.revaluation_rate.header"))
      }

      "include the correct header when Survivor scenario selected" in {
        when(mockSessionService.fetchGmpSession()(any())).thenReturn(Future.successful(Some(gmpSession
          .copy(scenario = CalculationType.SURVIVOR))))
        val result = TestRevaluationRateController.get(FakeRequest())
            contentAsString(result) must include(Messages("gmp.revaluation_rate.header"))
      }

      "include the correct header when non Payable Date scenario selected" in {

        when(mockSessionService.fetchGmpSession()(any())).thenReturn(Future.successful(Some(gmpSession
          .copy(scenario = CalculationType.DOL))))

        val result = TestRevaluationRateController.get(FakeRequest())
            contentAsString(result) must include(Messages("gmp.revaluation_rate.header"))
      }

      "include the correct header when non revaluation Scenario pre 06/04/2016 selected" in {

        val gmpSession = GmpSession(MemberDetails(nino, "A", "AAA"), "S1234567T", CalculationType.REVALUATION,
          Some(GmpDate(Some("1"), Some("1"), Some("2015"))), None, Leaving(GmpDate(None, None, None),
            Some(Leaving.YES_BEFORE)), None)
        when(mockSessionService.fetchGmpSession()(any())).thenReturn(Future.successful(Some(gmpSession)))
        val result = TestRevaluationRateController.get(FakeRequest())
            contentAsString(result) must include(Messages("gmp.revaluation_rate.header"))
      }

      "include the correct header when non revaluation Scenario after 06/04/2016 selected" in {

        val gmpSession = GmpSession(MemberDetails(nino, "A", "AAA"), "S1234567T", CalculationType.REVALUATION,
          Some(GmpDate(Some("1"), Some("1"), Some("2018"))), None, Leaving(GmpDate(None, None, None),
            Some(Leaving.YES_AFTER)), None)
        when(mockSessionService.fetchGmpSession()(any())).thenReturn(Future.successful(Some(gmpSession)))
        val result = TestRevaluationRateController.get(FakeRequest())
            contentAsString(result) must include(Messages("gmp.revaluation_rate.header"))
      }

      "not show limited rate option when leaving date after 06/04/2016 selected" in {

        val gmpSession = GmpSession(MemberDetails(nino, "A", "AAA"), "S1234567T", CalculationType.SPA, None, None,
          Leaving(GmpDate(Some("1"), Some("5"), Some("2016")), Some(Leaving.YES_AFTER)), None)

        when(mockSessionService.fetchGmpSession()(any())).thenReturn(Future.successful(Some(gmpSession)))
        val result = TestRevaluationRateController.get(FakeRequest())
            contentAsString(result) must not include (Messages("gmp.revaluation_rate.limited"))
      }

      "show limited rate option when leaving date before 06/04/2016 selected" in {

        val gmpSession = GmpSession(MemberDetails(nino, "A", "AAA"), "S1234567T", CalculationType.SPA, None, None,
          Leaving(GmpDate(None, None, None), Some(Leaving.YES_BEFORE)), None)

        when(mockSessionService.fetchGmpSession()(any())).thenReturn(Future.successful(Some(gmpSession)))
        val result = TestRevaluationRateController.get(FakeRequest())
            contentAsString(result) must include (Messages("gmp.revaluation_rate.limited"))
      }
    }
  }


  "Revaluation controller POST" must {

    when(mockSessionService.fetchScenario()(any())).thenReturn(Future.successful(None))

    "authenticated users" must {

      "with invalid data" must {

        "respond with BAD_REQUEST" in {

            val result = TestRevaluationRateController.post(FakeRequest().withMethod("POST")
              .withFormUrlEncodedBody("rateType" -> ""))
            status(result) must equal(BAD_REQUEST)
        }

        "display the errors" in {
            val result = TestRevaluationRateController.post(FakeRequest().withMethod("POST")
              .withFormUrlEncodedBody("rateType" -> ""))
            contentAsString(result) must include(Messages("gmp.error.revaluation.rate.error"))
        }

        "respond with error when can't get session" in {
          when(mockSessionService.fetchGmpSession()(any())).thenReturn(Future.successful(None))

            intercept[RuntimeException] {
              await(TestRevaluationRateController.post(FakeRequest().withMethod("POST")
                .withFormUrlEncodedBody("rateType" -> "")))
          }
        }

      }
      "with valid data" must {

        val gmpSession = GmpSession(MemberDetails("", "", ""), "S1301234T", CalculationType.REVALUATION, None, Some(""),
          Leaving(GmpDate(None, None, None), Some(Leaving.NO)), None)

        "redirect" in {

          when(mockSessionService.cacheRevaluationRate(any())(any())).thenReturn(Future.successful(Some(gmpSession)))
            val result = TestRevaluationRateController.post(FakeRequest().withMethod("POST")
              .withFormUrlEncodedBody("rateType" -> "hmrc"))
            status(result) must equal(SEE_OTHER)
        }


        "respond with error when rate not stored" in {
          when(mockSessionService.cacheRevaluationRate(any())(any())).thenReturn(Future.successful(None))
            intercept[RuntimeException] {
              await(TestRevaluationRateController.post(FakeRequest().withMethod("POST")
                .withFormUrlEncodedBody("rateType" -> "hmrc")))
          }
        }
      }
    }
  }

  "BACK" must {

    "authorised users redirect" in {

      val memberDetails = MemberDetails("", "", "")
      val session = GmpSession(memberDetails, "", CalculationType.PAYABLE_AGE, None, None,
        Leaving(GmpDate(None, None, None), None), None)


        when(mockSessionService.fetchGmpSession()(any())).thenReturn(Future.successful(Some(session)))
        val result = TestRevaluationRateController.back(FakeRequest())
        status(result) must equal(SEE_OTHER)
    }

    "throw an exception when session not fetched" in {

        when(mockSessionService.fetchGmpSession()(any())).thenReturn(Future.successful(None))
        val result = TestRevaluationRateController.back(FakeRequest())
        intercept[RuntimeException] {
          status(result) must equal(SEE_OTHER)
      }
    }

  }
}
