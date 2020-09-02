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

import config.{ApplicationConfig, GmpSessionCache}
import controllers.auth.{AuthAction, FakeAuthAction}
import forms.DateOfLeavingForm
import helpers.RandomNino
import models._
import org.joda.time.{DateTime, DateTimeUtils}
import org.mockito.Matchers
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

class DateOfLeavingControllerSpec extends PlaySpec with GuiceOneServerPerSuite with MockitoSugar {
  DateTimeUtils.setCurrentMillisFixed(new DateTime(2016, 1, 1, 1, 1).toDate.getTime)
  val mockAuthConnector = mock[AuthConnector]
  val mockSessionService = mock[SessionService]
  val mockAuthAction = mock[AuthAction]

  val baseValidDate = GmpDate(day = Some("31"), month = Some("1"), year = Some("2015"))
  implicit lazy val mcc = app.injector.instanceOf[MessagesControllerComponents]
  implicit val ec = app.injector.instanceOf[ExecutionContext]
  implicit val messagesAPI=app.injector.instanceOf[MessagesApi]
  implicit val messagesProvider=MessagesImpl(Lang("en"), messagesAPI)
  implicit val ac=app.injector.instanceOf[ApplicationConfig]
  implicit val gmpSessionCache=app.injector.instanceOf[GmpSessionCache]
  lazy val dateOfLeavingForm = new DateOfLeavingForm(mcc)
  lazy val views = app.injector.instanceOf[Views]


  object TestDateOfLeavingController extends DateOfLeavingController(FakeAuthAction, mockAuthConnector, mockSessionService,ac,FakeGmpContext,dateOfLeavingForm,mcc,ec,gmpSessionCache,views) {
    override val context = FakeGmpContext
  }

  val nino = RandomNino.generate

  "Date of Leaving controller GET " must {

    "authenticated users" must {
      val memberDetails = MemberDetails(nino, "A", "AAA")
      val session = GmpSession(memberDetails, "S1234567T", CalculationType.DOL, None, None, Leaving(GmpDate(None, None, None), None), None)
      "respond with ok" in {

        val result = TestDateOfLeavingController.get(FakeRequest())
            when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(session)))
            status(result) must equal(OK)
            contentAsString(result) must include("Did the member leave the scheme before 6 April 2016?\n - Guaranteed Minimum Pension - GOV.UK")
            contentAsString(result) must include(Messages("gmp.date.header_text"))
            contentAsString(result) must include(Messages("gmp.date.example"))
            contentAsString(result) must include(Messages("gmp.back.link"))

      }

      "throw an exception when session not fetched" in {


          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
          val result = TestDateOfLeavingController.get(FakeRequest())
          contentAsString(result) must include (Messages("gmp.error.session_parts_missing", "/guaranteed-minimum-pension/dashboard"))
      }

      "be shown correct title for DOL" in {

          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(session)))
          val result = TestDateOfLeavingController.get(FakeRequest())
          status(result) must equal(OK)
          contentAsString(result) must include(Messages("gmp.leaving.dol.question"))

      }

      "be shown correct title for SPA" in {

          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(session.copy(scenario = CalculationType.SPA))))
          val result = TestDateOfLeavingController.get(FakeRequest())
          status(result) must equal(OK)
          contentAsString(result) must include(Messages("gmp.other.dol.left.question"))

      }

      "be shown correct title for PAYABLE_AGE" in {

          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(session.copy(scenario = CalculationType.PAYABLE_AGE))))
          val result = TestDateOfLeavingController.get(FakeRequest())
          status(result) must equal(OK)
          contentAsString(result) must include(Messages("gmp.other.dol.left.question"))

      }

      "be shown correct title for REVALUATION" in {

          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(session.copy(scenario = CalculationType.REVALUATION))))
          val result = TestDateOfLeavingController.get(FakeRequest())
          status(result) must equal(OK)
          contentAsString(result) must include(Messages("gmp.other.dol.left.question"))

      }

      "be shown correct title for SURVIVOR" in {

          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(session.copy(scenario = CalculationType.SURVIVOR))))
          val result = TestDateOfLeavingController.get(FakeRequest())
          status(result) must equal(OK)
          contentAsString(result) must include(Messages("gmp.survivor.dol.question"))

      }

      "be shown correct options for DOL" in {

          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(session.copy(scenario = CalculationType.DOL))))
          val result = TestDateOfLeavingController.get(FakeRequest())
          status(result) must equal(OK)
          contentAsString(result) must include(Messages("gmp.generic.yes"))
          contentAsString(result) must include(Messages("gmp.generic.no"))

      }

      "be shown correct options for Anything Else" in {

          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(session.copy(scenario = CalculationType.SPA))))
          val result = TestDateOfLeavingController.get(FakeRequest())
          status(result) must equal(OK)
          contentAsString(result) must include(Messages("gmp.dol.threequestions.before2016"))
          contentAsString(result) must include(Messages("gmp.dol.threequestions.after2016"))
          contentAsString(result) must include(Messages("gmp.dol.threequestions.no"))

      }

      "go to failure page when session missing scon" in {
        val emptySession = GmpSession(MemberDetails("", "", ""), "", "", None, None, Leaving(GmpDate(None, None, None), None), None)
        when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(emptySession)))

          val result = TestDateOfLeavingController.get(FakeRequest())
          contentAsString(result)replaceAll("&#x27;", "'") must include (Messages("gmp.cannot_calculate.gmp"))
          contentAsString(result) must include (Messages("gmp.error.session_parts_missing", "/guaranteed-minimum-pension/pension-details"))
      }

      "go to failure page when session missing nino" in {
        val emptySession = GmpSession(MemberDetails("", "", ""), "S1234567T", "", None, None, Leaving(GmpDate(None, None, None), None), None)
        when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(emptySession)))

          val result = TestDateOfLeavingController.get(FakeRequest())
          contentAsString(result)replaceAll("&#x27;", "'") must include (Messages("gmp.cannot_calculate.gmp"))
          contentAsString(result) must include (Messages("gmp.error.session_parts_missing", "/guaranteed-minimum-pension/member-details"))
      }

      "go to failure page when session missing firstname" in {
        val emptySession = GmpSession(MemberDetails(nino, "", ""), "S1234567T", "", None, None, Leaving(GmpDate(None, None, None), None), None)

          val result = TestDateOfLeavingController.get(FakeRequest())
          contentAsString(result)replaceAll("&#x27;", "'") must include (Messages("gmp.cannot_calculate.gmp"))
          contentAsString(result) must include (Messages("gmp.error.session_parts_missing", "/guaranteed-minimum-pension/member-details"))
      }

      "go to failure page when session missing lastname" in {
        val emptySession = GmpSession(MemberDetails(nino, "A", ""), "S1234567T", "", None, None, Leaving(GmpDate(None, None, None), None), None)
        when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(emptySession)))

          val result = TestDateOfLeavingController.get(FakeRequest())
          contentAsString(result)replaceAll("&#x27;", "'") must include (Messages("gmp.cannot_calculate.gmp"))
          contentAsString(result) must include (Messages("gmp.error.session_parts_missing", "/guaranteed-minimum-pension/member-details"))
      }

      "go to failure page when session missing scenario" in {
        val emptySession = GmpSession(MemberDetails(nino, "A", "AAA"), "S1234567T", "", None, None, Leaving(GmpDate(None, None, None), None), None)
        when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(emptySession)))

          val result = TestDateOfLeavingController.get(FakeRequest())
          contentAsString(result)replaceAll("&#x27;", "'") must include (Messages("gmp.cannot_calculate.gmp"))
          contentAsString(result) must include (Messages("gmp.error.session_parts_missing", "/guaranteed-minimum-pension/calculation-reason"))
      }
    }
  }

  "BACK" must {

    "authorised users redirect" in {

      val memberDetails = MemberDetails(nino, "A", "AAA")
      val session = GmpSession(memberDetails, "S1234567T", CalculationType.DOL, None, None, Leaving(GmpDate(None, None, None), None), None)

        when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(session)))
        val result = TestDateOfLeavingController.back(FakeRequest())
        status(result) must equal(SEE_OTHER)
    }

    "throw an exception when session not fetched" in {

        when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
        val result = TestDateOfLeavingController.back(FakeRequest())
        intercept[RuntimeException] {
          status(result) must equal(SEE_OTHER)
      }
    }

  }

  "Date of Leaving controller POST" must {

    "authenticated users" must {

      "with invalid data" must {
        val gmpSession = GmpSession(MemberDetails(nino, "A", "AAA"), "S1234567T", CalculationType.DOL, None, None, Leaving(GmpDate(None, None, None), None), None)
        when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession)))

        "respond with BAD_REQUEST" in {

            val postData = Json.toJson(
              Leaving(baseValidDate.copy(day = Some("31"), month = Some("2"), year = Some("2015")), Some("Yes"))
            )
            when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession)))
            val result = TestDateOfLeavingController.post(FakeRequest().withJsonBody(postData))
            status(result) must equal(BAD_REQUEST)
        }

        "display the errors" in {
            val postData = Json.toJson(
              Leaving(baseValidDate.copy(day = Some("31"), month = Some("2"), year = Some("2015")), Some(Leaving.YES_AFTER))
            )
            when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession)))
            val result = TestDateOfLeavingController.post(FakeRequest().withJsonBody(postData))
            contentAsString(result) must include(Messages("gmp.error.date.leaving.invalid"))
        }

        "throw an exception when session not fetched" in {

            val postData = Json.toJson(
              Leaving(baseValidDate.copy(day = Some("31"), month = Some("2"), year = Some("2015")), Some(Leaving.YES_AFTER))
            )
            when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
            val result = TestDateOfLeavingController.post(FakeRequest().withJsonBody(postData))
            intercept[RuntimeException] {
              contentAsString(result) must include(Messages("gmp.error.date.invalid"))
          }
        }
      }

      "with valid data" must {
        val gmpSession = GmpSession(MemberDetails(nino, "A", "AAA"), "S1234567T", CalculationType.DOL, None, None, Leaving(GmpDate(None, None, None), None), None)
        "redirect" in {

          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession)))

          when(mockSessionService.cacheLeaving(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession)))

          val leaving = Json.toJson(Leaving(GmpDate(Some("06"), Some("04"), Some("2016")), Some("Y")))

            val result = TestDateOfLeavingController.post()(FakeRequest().withJsonBody(leaving))
            status(result) must equal(SEE_OTHER)
        }

        "throw exception when can't cache session" in {


          when(mockSessionService.cacheLeaving(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))

          val leaving = Json.toJson(Leaving(GmpDate(Some("06"), Some("04"), Some("2016")), Some("Y")))

            val result = TestDateOfLeavingController.post()(FakeRequest().withJsonBody(leaving))
            intercept[RuntimeException] {
              status(result) must equal(SEE_OTHER)
          }
        }

        "redirect to revaluation when revaluation scenario is selected" in {
          when(mockSessionService.cacheLeaving(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession.copy(scenario = CalculationType.REVALUATION))))
          val validReason = Json.toJson(Leaving(baseValidDate.copy(day = Some("06"), month = Some("4"), year = Some("2016")), Some(Leaving.YES_AFTER)))

            val result = TestDateOfLeavingController.post()(FakeRequest().withJsonBody(validReason))
            status(result) must equal(SEE_OTHER)
            redirectLocation(result).get must include("/relevant-date")
        }

        "redirect to revaluation rate when spa and has left" in {
          when(mockSessionService.cacheLeaving(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(
            gmpSession.copy(scenario = CalculationType.SPA, leaving = Leaving(GmpDate(Some("06"), Some("4"), Some("2016")), leaving = Some(Leaving.YES_AFTER))))))
          val validReason = Json.toJson(Leaving(baseValidDate.copy(day = Some("06"), month = Some("4"), year = Some("2016")), Some(Leaving.YES_AFTER)))

            val result = TestDateOfLeavingController.post()(FakeRequest().withJsonBody(validReason))
            status(result) must equal(SEE_OTHER)
            redirectLocation(result).get must include("/revaluation-rate")
        }

        "redirect to equalisation when payable age and has not left" in {
          when(mockSessionService.cacheLeaving(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession.copy(scenario = CalculationType.PAYABLE_AGE))))
          val validReason = Json.toJson(Leaving(baseValidDate.copy(day = None, month = None, year = None), Some(Leaving.YES_BEFORE)))

            val result = TestDateOfLeavingController.post()(FakeRequest().withJsonBody(validReason))
            status(result) must equal(SEE_OTHER)

            redirectLocation(result).get must include("/equalise")

        }

        "redirect to equalise when dol" in {
          when(mockSessionService.cacheLeaving(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession.copy(leaving = Leaving(GmpDate(Some(""), Some(""), Some("")), leaving = None)))))

            val postData = Json.toJson(
              Leaving(baseValidDate.copy(day = Some("06"), month = Some("4"), year = Some("2016")), Some(Leaving.YES_AFTER))
            )
            val result = TestDateOfLeavingController.post(FakeRequest().withJsonBody(postData))
            status(result) must equal(SEE_OTHER)
            redirectLocation(result).get must include("/equalise")
        }

        "redirect to the revaluation rate page when survivor has left before 6/4/16" in {
          when(mockSessionService.cacheLeaving(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession.copy(scenario = CalculationType.SURVIVOR, leaving = Leaving(GmpDate(Some(""), Some(""), Some("")), Some(Leaving.YES_BEFORE))))))

            val postData = Json.toJson(
              Leaving(baseValidDate.copy(day = Some("20"), month = Some("4"), year = Some("2015")), Some(Leaving.YES_BEFORE))
            )
            val result = TestDateOfLeavingController.post(FakeRequest().withJsonBody(postData))
            status(result) must equal(SEE_OTHER)
            redirectLocation(result).get must be(routes.RevaluationRateController.get.url)
        }

        "redirect to the revaluation rate page when survivor has left on or after 6/4/16" in {
          when(mockSessionService.cacheLeaving(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession.copy(scenario = CalculationType.SURVIVOR, leaving = Leaving(GmpDate(Some(""), Some(""), Some("")), Some(Leaving.YES_AFTER))))))

            val postData = Json.toJson(
              Leaving(baseValidDate.copy(day = Some("6"), month = Some("4"), year = Some("2016")), Some(Leaving.YES_AFTER))
            )
            val result = TestDateOfLeavingController.post(FakeRequest().withJsonBody(postData))
            status(result) must equal(SEE_OTHER)
            redirectLocation(result).get must be(routes.RevaluationRateController.get.url)
        }


        "redirect to inflation proof page when survivor has not left" in {
          when(mockSessionService.cacheLeaving(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession.copy(scenario = CalculationType.SURVIVOR, leaving = Leaving(GmpDate(Some(""), Some(""), Some("")), Some(Leaving.NO))))))

            val postData = Json.toJson(
              Leaving(baseValidDate.copy(day = Some("6"), month = Some("4"), year = Some("2016")), Some(Leaving.YES_AFTER))
            )
            val result = TestDateOfLeavingController.post(FakeRequest().withJsonBody(postData))
            status(result) must equal(SEE_OTHER)
            redirectLocation(result).get must be(routes.InflationProofController.get.url)
        }
      }
    }
  }
}
