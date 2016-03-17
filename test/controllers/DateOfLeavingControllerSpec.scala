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
import org.joda.time.{DateTime, DateTimeUtils}
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
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

import scala.concurrent.Future

class DateOfLeavingControllerSpec extends PlaySpec with OneServerPerSuite with MockitoSugar with GmpUsers {
  DateTimeUtils.setCurrentMillisFixed(new DateTime(2016, 1, 1, 1, 1).toDate.getTime)
  val mockAuthConnector = mock[AuthConnector]
  val mockSessionService = mock[SessionService]

  val baseValidDate = GmpDate(day = Some("31"), month = Some("1"), year = Some("2015"))

  object TestDateOfLeavingController extends DateOfLeavingController {
    val authConnector = mockAuthConnector
    override val sessionService = mockSessionService
  }

  "Date of Leaving controller" must {

    "respond to GET /guaranteed-minimum-pension/left-scheme" in {
      val result = route(FakeRequest(GET, "/guaranteed-minimum-pension/left-scheme"))
      status(result.get) must not equal (NOT_FOUND)
    }

    "respond to back /guaranteed-minimum-pension/left-scheme/back" in {
      val result = route(FakeRequest(GET, "/guaranteed-minimum-pension/left-scheme/back"))
      status(result.get) must not equal (NOT_FOUND)
    }
  }


  "Date of Leaving controller GET " must {

    "be authorised" in {
      get() { result =>
        status(result) must equal(SEE_OTHER)
        redirectLocation(result).get must include("/account/sign-in")
      }
    }

    "authenticated users" must {
      val memberDetails = MemberDetails("", "", "")
      val session = GmpSession(memberDetails, "", CalculationType.DOL, None, None, Leaving(GmpDate(None, None, None), None), None)
      "respond with ok" in {

        withAuthorisedUser { user =>
          get(user) { result =>
            when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(session)))
            status(result) must equal(OK)
            contentAsString(result) must include(Messages("gmp.leaving.title"))
            contentAsString(result) must include(Messages("gmp.date.header_text"))
            contentAsString(result) must include(Messages("gmp.date.example"))
          }
        }
      }

      "throw an exception when session not fetched" in {

        withAuthorisedUser { user =>
          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
          intercept[RuntimeException] {
            await(TestDateOfLeavingController.get.apply(user))
          }
        }
      }

      "be shown correct title for DOL" in {

        withAuthorisedUser { request =>

          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(session)))
          val result = TestDateOfLeavingController.get.apply(request)
          status(result) must equal(OK)
          contentAsString(result) must include(Messages("gmp.leaving.dol.question"))

        }
      }

      "be shown correct title for SPA" in {

        withAuthorisedUser { request =>

          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(session.copy(scenario = CalculationType.SPA))))
          val result = TestDateOfLeavingController.get.apply(request)
          status(result) must equal(OK)
          contentAsString(result) must include(Messages("gmp.other.dol.left.question"))

        }
      }

      "be shown correct title for PAYABLE_AGE" in {

        withAuthorisedUser { request =>

          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(session.copy(scenario = CalculationType.PAYABLE_AGE))))
          val result = TestDateOfLeavingController.get.apply(request)
          status(result) must equal(OK)
          contentAsString(result) must include(Messages("gmp.other.dol.left.question"))

        }
      }

      "be shown correct title for REVALUATION" in {

        withAuthorisedUser { request =>

          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(session.copy(scenario = CalculationType.REVALUATION))))
          val result = TestDateOfLeavingController.get.apply(request)
          status(result) must equal(OK)
          contentAsString(result) must include(Messages("gmp.other.dol.left.question"))

        }
      }

      "be shown correct title for SURVIVOR" in {

        withAuthorisedUser { request =>

          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(session.copy(scenario = CalculationType.SURVIVOR))))
          val result = TestDateOfLeavingController.get.apply(request)
          status(result) must equal(OK)
          contentAsString(result) must include(Messages("gmp.survivor.dol.question"))

        }
      }

      "be shown correct options for DOL" in {
        withAuthorisedUser { request =>

          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(session.copy(scenario = CalculationType.DOL))))
          val result = TestDateOfLeavingController.get.apply(request)
          status(result) must equal(OK)
          contentAsString(result) must include(Messages("gmp.generic.yes"))
          contentAsString(result) must include(Messages("gmp.generic.no"))

        }
      }

      "be shown correct options for Anything Else" in {
        withAuthorisedUser { request =>

          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(session.copy(scenario = CalculationType.SPA))))
          val result = TestDateOfLeavingController.get.apply(request)
          status(result) must equal(OK)
          contentAsString(result) must include(Messages("gmp.dol.threequestions.before2016"))
          contentAsString(result) must include(Messages("gmp.dol.threequestions.after2016"))
          contentAsString(result) must include(Messages("gmp.dol.threequestions.no"))

        }
      }
    }
  }

  "BACK" must {

    "be authorised" in {
      val result = TestDateOfLeavingController.back.apply(FakeRequest())
      status(result) must equal(SEE_OTHER)
      redirectLocation(result).get must include("/account/sign-in")
    }

    "authorised users redirect" in {

      val memberDetails = MemberDetails("", "", "")
      val session = GmpSession(memberDetails, "", "", None, None, Leaving(GmpDate(None, None, None), None), None)

      withAuthorisedUser { request =>
        when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(session)))
        val result = TestDateOfLeavingController.back.apply(request)
        status(result) must equal(SEE_OTHER)
      }
    }

    "throw an exception when session not fetched" in {

      withAuthorisedUser { request =>
        when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
        val result = TestDateOfLeavingController.back.apply(request)
        intercept[RuntimeException] {
          status(result) must equal(SEE_OTHER)
        }
      }
    }

  }

  "Date of Leaving controller POST" must {
    "be authorised" in {
      val result = TestDateOfLeavingController.get.apply(FakeRequest())
      status(result) must equal(SEE_OTHER)
      redirectLocation(result).get must include("/account/sign-in")
    }

    "authenticated users" must {

      "with invalid data" must {
        val gmpSession = GmpSession(MemberDetails("", "", ""), "", CalculationType.DOL, None, None, Leaving(GmpDate(None, None, None), None), None)
        when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession)))

        "respond with BAD_REQUEST" in {
          withAuthorisedUser { request =>
            val postData = Json.toJson(
              Leaving(baseValidDate.copy(day = Some("31"), month = Some("2"), year = Some("2015")), Some("Yes"))
            )
            when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession)))
            val result = TestDateOfLeavingController.post.apply(request.withJsonBody(postData))
            status(result) must equal(BAD_REQUEST)
          }
        }

        "display the errors" in {
          withAuthorisedUser { request =>
            val postData = Json.toJson(
              Leaving(baseValidDate.copy(day = Some("31"), month = Some("2"), year = Some("2015")), Some(Leaving.YES_AFTER))
            )
            when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession)))
            val result = TestDateOfLeavingController.post.apply(request.withJsonBody(postData))
            contentAsString(result) must include(Messages("gmp.error.date.invalid"))
          }
        }

        "throw an exception when session not fetched" in {
          withAuthorisedUser { request =>
            val postData = Json.toJson(
              Leaving(baseValidDate.copy(day = Some("31"), month = Some("2"), year = Some("2015")), Some(Leaving.YES_AFTER))
            )
            when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
            val result = TestDateOfLeavingController.post.apply(request.withJsonBody(postData))
            intercept[RuntimeException] {
              contentAsString(result) must include(Messages("gmp.error.date.invalid"))
            }
          }
        }
      }

      "with valid data" must {
        val gmpSession = GmpSession(MemberDetails("", "", ""), "", CalculationType.DOL, None, None, Leaving(GmpDate(None, None, None), None), None)
        "redirect" in {


          when(mockSessionService.cacheLeaving(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession)))

          val leaving = Json.toJson(Leaving(GmpDate(Some("06"), Some("04"), Some("2016")), Some("Y")))
          withAuthorisedUser { request =>
            val result = TestDateOfLeavingController.post()(request.withJsonBody(leaving))
            status(result) must equal(SEE_OTHER)
          }
        }

        "throw exception when can't cache session" in {


          when(mockSessionService.cacheLeaving(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))

          val leaving = Json.toJson(Leaving(GmpDate(Some("06"), Some("04"), Some("2016")), Some("Y")))
          withAuthorisedUser { request =>
            val result = TestDateOfLeavingController.post()(request.withJsonBody(leaving))
            intercept[RuntimeException] {
              status(result) must equal(SEE_OTHER)
            }
          }
        }

        "redirect to revaluation when revaluation scenario is selected" in {
          when(mockSessionService.cacheLeaving(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession.copy(scenario = CalculationType.REVALUATION))))
          val validReason = Json.toJson(Leaving(baseValidDate.copy(day = Some("06"), month = Some("4"), year = Some("2016")), Some(Leaving.YES_AFTER)))
          withAuthorisedUser { request =>
            val result = TestDateOfLeavingController.post()(request.withJsonBody(validReason))
            status(result) must equal(SEE_OTHER)
            redirectLocation(result).get must include("/relevant-date")
          }
        }

        "redirect to revaluation rate when spa and has left" in {
          when(mockSessionService.cacheLeaving(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(
            gmpSession.copy(scenario = CalculationType.SPA, leaving = Leaving(GmpDate(Some("06"), Some("4"), Some("2016")), leaving = Some(Leaving.YES_AFTER))))))
          val validReason = Json.toJson(Leaving(baseValidDate.copy(day = Some("06"), month = Some("4"), year = Some("2016")), Some(Leaving.YES_AFTER)))
          withAuthorisedUser { request =>
            val result = TestDateOfLeavingController.post()(request.withJsonBody(validReason))
            status(result) must equal(SEE_OTHER)
            redirectLocation(result).get must include("/revaluation-rate")
          }
        }

        "redirect to equalisation when payable age and has not left" in {
          when(mockSessionService.cacheLeaving(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession.copy(scenario = CalculationType.PAYABLE_AGE))))
          val validReason = Json.toJson(Leaving(baseValidDate.copy(day = None, month = None, year = None), Some(Leaving.YES_BEFORE)))
          withAuthorisedUser { request =>
            val result = TestDateOfLeavingController.post()(request.withJsonBody(validReason))
            println(contentAsString(result))
            status(result) must equal(SEE_OTHER)

            redirectLocation(result).get must include("/equalise")
          }
        }

        "redirect to equalise when dol" in {
          when(mockSessionService.cacheLeaving(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession.copy(leaving = Leaving(GmpDate(Some(""), Some(""), Some("")), leaving = None)))))
          withAuthorisedUser { request =>
            val postData = Json.toJson(
              Leaving(baseValidDate.copy(day = Some("06"), month = Some("4"), year = Some("2016")), Some(Leaving.YES_AFTER))
            )
            val result = TestDateOfLeavingController.post.apply(request.withJsonBody(postData))
            status(result) must equal(SEE_OTHER)
            redirectLocation(result).get must include("/equalise")
          }
        }

        "redirect to the revaluation rate page when survivor has left before 6/4/16" in {
          when(mockSessionService.cacheLeaving(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession.copy(scenario = CalculationType.SURVIVOR, leaving = Leaving(GmpDate(Some(""), Some(""), Some("")), Some(Leaving.YES_BEFORE))))))
          withAuthorisedUser { request =>
            val postData = Json.toJson(
              Leaving(baseValidDate.copy(day = Some("20"), month = Some("4"), year = Some("2015")), Some(Leaving.YES_BEFORE))
            )
            val result = TestDateOfLeavingController.post.apply(request.withJsonBody(postData))
            status(result) must equal(SEE_OTHER)
            redirectLocation(result).get must be(routes.RevaluationRateController.get.url)
          }
        }

        "redirect to the revaluation rate page when survivor has left on or after 6/4/16" in {
          when(mockSessionService.cacheLeaving(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession.copy(scenario = CalculationType.SURVIVOR, leaving = Leaving(GmpDate(Some(""), Some(""), Some("")), Some(Leaving.YES_AFTER))))))
          withAuthorisedUser { request =>
            val postData = Json.toJson(
              Leaving(baseValidDate.copy(day = Some("6"), month = Some("4"), year = Some("2016")), Some(Leaving.YES_AFTER))
            )
            val result = TestDateOfLeavingController.post.apply(request.withJsonBody(postData))
            status(result) must equal(SEE_OTHER)
            redirectLocation(result).get must be(routes.RevaluationRateController.get.url)
          }
        }


        "redirect to inflation proof page when survivor has not left" in {
          when(mockSessionService.cacheLeaving(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession.copy(scenario = CalculationType.SURVIVOR, leaving = Leaving(GmpDate(Some(""), Some(""), Some("")), Some(Leaving.NO))))))
          withAuthorisedUser { request =>
            val postData = Json.toJson(
              Leaving(baseValidDate.copy(day = Some("6"), month = Some("4"), year = Some("2016")), Some(Leaving.YES_AFTER))
            )
            val result = TestDateOfLeavingController.post.apply(request.withJsonBody(postData))
            status(result) must equal(SEE_OTHER)
            redirectLocation(result).get must be(routes.InflationProofController.get.url)
          }
        }
      }
    }
  }

  def get(request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest())(handler: Future[Result] => Any): Unit = {
    handler(TestDateOfLeavingController.get.apply(request))
  }

}
