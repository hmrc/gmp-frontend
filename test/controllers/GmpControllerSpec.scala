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

import controllers.auth.{AuthAction, GmpAuthConnector}
import models._
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.{OneServerPerSuite, PlaySpec}
import play.api.test.Helpers._
import services.SessionService


class GmpControllerSpec extends PlaySpec with OneServerPerSuite with MockitoSugar {

  val mockAuthConnector = mock[GmpAuthConnector]
  val mockSessionService = mock[SessionService]
  val mockAuthAction = mock[AuthAction]

  object TestGmpController extends GmpPageFlow(mockAuthConnector) {
    override val sessionService = mockSessionService
    override val context = FakeGmpContext
  }

  val gmpSession = GmpSession(MemberDetails("", "", ""), "", "", None, None, Leaving(GmpDate(None, None, None), None), None)

  "GmpControllerSpec" must {

    "next page" must {

      "return a not found when you send it a controller doesn't exist in the map" in {
        val gmpSession = GmpSession(MemberDetails("", "", ""), "", "", None, None, Leaving(GmpDate(None, None, None), None), None)
        val result = TestGmpController.nextPage("BadController", gmpSession)
        result.header.status must be(NOT_FOUND)
      }

      "coming from the pension details page (PensionDetailsController[POST])" must {

        "redirect to the member details page" in {
          val result = TestGmpController.nextPage(PageType.PENSION_DETAILS, gmpSession)
          result.header.status must be(SEE_OTHER)
          result.header.headers.get("LOCATION").get must be(routes.MemberDetailsController.get().url)
        }
      }

      "coming from the member details page (MemberDetailsController[POST])" must {

        "redirect to the scenario page" in {
          val result = TestGmpController.nextPage(PageType.MEMBER_DETAILS, gmpSession)
          result.header.status must be(SEE_OTHER)
          result.header.headers.get("LOCATION").get must be(routes.ScenarioController.get().url)
        }
      }

      "coming from the scenario page (ScenarioController[POST])" must {

        "redirect to the date of leaving page" in {

          val session = GmpSession(MemberDetails("", "", ""), "", "", None, None, Leaving(GmpDate(None, None, None), None), None)
          val result = TestGmpController.nextPage(PageType.SCENARIO, session)

          result.header.status must be(SEE_OTHER)
          result.header.headers.get("LOCATION").get must be(routes.DateOfLeavingController.get().url)
        }
      }

      "coming from the revaluation controller (RevaluationController[POST])" must {

        "redirect to the equalise page when member still in scheme" in {
          val revalSession = GmpSession(MemberDetails("", "", ""), "", CalculationType.REVALUATION, Some(GmpDate(Some("1"), Some("1"), Some("1990"))), None, Leaving(GmpDate(None, None, None), Some(Leaving.NO)), None)
          val result = TestGmpController.nextPage(PageType.REVALUATION, revalSession)
          result.header.status must be(SEE_OTHER)
          result.header.headers.get("LOCATION").get must be(routes.EqualiseController.get().url)
        }

        "redirect to the equalise page member not in scheme, left before 2016" in {
          val revalSession = GmpSession(MemberDetails("", "", ""), "", CalculationType.REVALUATION, Some(GmpDate(Some("1"), Some("1"), Some("2017"))), None, Leaving(GmpDate(Some("1"), Some("1"), Some("1990")), Some(Leaving.YES_BEFORE)), None)
          val result = TestGmpController.nextPage(PageType.REVALUATION, revalSession)
          result.header.status must be(SEE_OTHER)
          result.header.headers.get("LOCATION").get must be(routes.RevaluationRateController.get().url)
        }

        "redirect to the equalise page member not in scheme, left after 2016, in same tax year" in {
          val revalSession = GmpSession(MemberDetails("", "", ""), "", CalculationType.REVALUATION, Some(GmpDate(Some("1"), Some("1"), Some("2017"))), None, Leaving(GmpDate(Some("1"), Some("1"), Some("2017")), Some(Leaving.YES_AFTER)), None)
          val result = TestGmpController.nextPage(PageType.REVALUATION, revalSession)
          result.header.status must be(SEE_OTHER)
          result.header.headers.get("LOCATION").get must be(routes.EqualiseController.get().url)
        }

        "redirect to the equalise page member not in scheme, left after 2016, not in same tax year" in {
          val revalSession = GmpSession(MemberDetails("", "", ""), "", CalculationType.REVALUATION, Some(GmpDate(Some("1"), Some("1"), Some("1990"))), None, Leaving(GmpDate(Some("1"), Some("1"), Some("2017")), Some(Leaving.YES_AFTER)), None)
          val result = TestGmpController.nextPage(PageType.REVALUATION, revalSession)
          result.header.status must be(SEE_OTHER)
          result.header.headers.get("LOCATION").get must be(routes.RevaluationRateController.get().url)
        }
      }

      "coming from the rate page (RevaluationRatePage[POST]" must {

        "redirect to the equalise page" in {
          val revalSession = GmpSession(MemberDetails("", "", ""), "", CalculationType.REVALUATION, Some(GmpDate(Some("1"), Some("1"), Some("1990"))), Some("1"), Leaving(GmpDate(None, None, None), None), None)
          val result = TestGmpController.nextPage(PageType.REVALUATION_RATE, revalSession)
          result.header.status must be(SEE_OTHER)
          result.header.headers.get("LOCATION").get must be(routes.EqualiseController.get().url)
        }

        "redirect to the inflation proof page" in {
          val session = gmpSession.copy(scenario = CalculationType.SURVIVOR)
          val result = TestGmpController.nextPage(PageType.REVALUATION_RATE, session)
          result.header.status must be(SEE_OTHER)
          result.header.headers.get("LOCATION").get must be(routes.InflationProofController.get().url)
        }
      }

      "coming from the equalise page (EqualisePage[POST]" must {

        "redirect to the results page" in {
          val result = TestGmpController.nextPage(PageType.EQUALISE, gmpSession)
          result.header.status must be(SEE_OTHER)
          result.header.headers.get("LOCATION").get must be(routes.ResultsController.get().url)
        }
      }
    }

    "previous page" must {

      "return a not found when you send it a controller doesn't exist in the map" in {
        val result = TestGmpController.previousPage("BadController", gmpSession)
        result.header.status must be(NOT_FOUND)
      }

      "coming from the member details page" must {

        "redirect to the pension details page" in {
          val result = TestGmpController.previousPage(PageType.MEMBER_DETAILS, gmpSession)
          result.header.status must be(SEE_OTHER)
          result.header.headers.get("LOCATION").get must be(routes.PensionDetailsController.get().url)
        }
      }

      "coming from the scenario page" must {

        "redirect to the member details page" in {
          val result = TestGmpController.previousPage(PageType.SCENARIO, gmpSession)
          result.header.status must be(SEE_OTHER)
          result.header.headers.get("LOCATION").get must be(routes.MemberDetailsController.get().url)
        }
      }

      "coming from the revaluation page" must {

        "redirect to the scenario page" in {
          val result = TestGmpController.previousPage(PageType.REVALUATION, gmpSession)
          result.header.status must be(SEE_OTHER)
          result.header.headers.get("LOCATION").get must be(routes.DateOfLeavingController.get().url)
        }
      }

      "coming from the equalise page" must {

        "when revaluation calculation and same tax year" must {

          val revaluationSession = GmpSession(MemberDetails("", "", ""), "", CalculationType.REVALUATION,
            revaluationDate = Some(GmpDate(Some("06"), Some("04"), Some("2010"))),
            None, leaving = Leaving(GmpDate(Some("05"), Some("04"), Some("2011")), leaving = Some("Yes")), None)

          "redirect to the revaluation date page" in {

            val result = TestGmpController.previousPage(PageType.EQUALISE, revaluationSession)
            result.header.status must be(SEE_OTHER)
            result.header.headers.get("LOCATION").get must be(routes.RevaluationController.get().url)
          }
        }

        "when revaluation calculation and NOT same tax year" must {

          val revaluationSession = GmpSession(MemberDetails("", "", ""), "", CalculationType.REVALUATION,
            revaluationDate = Some(GmpDate(Some("06"), Some("04"), Some("2010"))), None,
            leaving = Leaving(GmpDate(Some("06"), Some("04"), Some("2011")), leaving = Some("Yes")), None)

          "redirect to the revaluation rate page" in {

            val result = TestGmpController.previousPage(PageType.EQUALISE, revaluationSession)
            result.header.status must be(SEE_OTHER)
            result.header.headers.get("LOCATION").get must be(routes.RevaluationRateController.get().url)
          }
        }

        "when revaluation calculation and no leaving date" must {

          val revaluationSession = GmpSession(MemberDetails("", "", ""), "", CalculationType.REVALUATION,
            revaluationDate = Some(GmpDate(Some("06"), Some("04"), Some("2010"))), None,
            leaving = Leaving(GmpDate(None, None, None), leaving = Some("Yes")), None)

          "redirect to the revaluation rate page" in {

            val result = TestGmpController.previousPage(PageType.EQUALISE, revaluationSession)
            result.header.status must be(SEE_OTHER)
            result.header.headers.get("LOCATION").get must be(routes.RevaluationRateController.get().url)
          }
        }

        "when spa calculation and has not left the scheme" must {

          val spaSession = GmpSession(MemberDetails("", "", ""), "", CalculationType.SPA, None, None, Leaving(GmpDate(None, None, None), None), None)

          "redirect to the leaving date page" in {

            val result = TestGmpController.previousPage(PageType.EQUALISE, spaSession)
            result.header.status must be(SEE_OTHER)
            result.header.headers.get("LOCATION").get must be(routes.DateOfLeavingController.get().url)
          }
        }

        "when pa calculation and has not left the scheme" must {

          val paSession = GmpSession(MemberDetails("", "", ""), "", CalculationType.PAYABLE_AGE, None, None, Leaving(GmpDate(None, None, None), None), None)

          "redirect to the leaving date page" in {

            val result = TestGmpController.previousPage(PageType.EQUALISE, paSession)
            result.header.status must be(SEE_OTHER)
            result.header.headers.get("LOCATION").get must be(routes.DateOfLeavingController.get().url)
          }
        }

        "when spa calculation and has left the scheme" must {

          val spaSession = GmpSession(MemberDetails("", "", ""), "", CalculationType.SPA, None, None, Leaving(GmpDate(Some("1"), Some("1"), Some("2015")), Some(Leaving.YES_AFTER)), None)

          "redirect to the revaluation rate page" in {

            val result = TestGmpController.previousPage(PageType.EQUALISE, spaSession)
            result.header.status must be(SEE_OTHER)
            result.header.headers.get("LOCATION").get must be(routes.RevaluationRateController.get().url)
          }
        }

        "when pa calculation and has left the scheme" must {

          val paSession = GmpSession(MemberDetails("", "", ""), "", CalculationType.PAYABLE_AGE, None, None, Leaving(GmpDate(Some("1"), Some("1"), Some("2015")), Some(Leaving.YES_AFTER)), None)

          "redirect to the revaluation rate page" in {

            val result = TestGmpController.previousPage(PageType.EQUALISE, paSession)
            result.header.status must be(SEE_OTHER)
            result.header.headers.get("LOCATION").get must be(routes.RevaluationRateController.get().url)
          }
        }

        "when dol calculation" must {

          val dolSession = GmpSession(MemberDetails("", "", ""), "", CalculationType.DOL, None, None, Leaving(GmpDate(Some("1"), Some("1"), Some("2015")), Some(Leaving.YES_AFTER)), None)

          "redirect to the leaving date page" in {

            val result = TestGmpController.previousPage(PageType.EQUALISE, dolSession)
            result.header.status must be(SEE_OTHER)
            result.header.headers.get("LOCATION").get must be(routes.DateOfLeavingController.get().url)
          }
        }

      }

      "coming from the revaluation rate page" must {

        "when SPA calculation" must {

          val spaSession = GmpSession(MemberDetails("", "", ""), "", CalculationType.SPA, None, None, Leaving(GmpDate(None, None, None), None), None)

          "redirect to the termination page" in {
            val result = TestGmpController.previousPage(PageType.REVALUATION_RATE, spaSession)
            result.header.status must be(SEE_OTHER)
            result.header.headers.get("LOCATION").get must be(routes.DateOfLeavingController.get().url)
          }
        }

        "when GMP payable age calculation" must {

          val paSession = GmpSession(MemberDetails("", "", ""), "", CalculationType.PAYABLE_AGE, None, None, Leaving(GmpDate(None, None, None), None), None)

          "redirect to the scenario page" in {
            val result = TestGmpController.previousPage(PageType.REVALUATION_RATE, paSession)
            result.header.status must be(SEE_OTHER)
            result.header.headers.get("LOCATION").get must be(routes.DateOfLeavingController.get().url)
          }
        }

        "when revaluation calculation" must {

          val revalSession = GmpSession(MemberDetails("", "", ""), "", CalculationType.REVALUATION, Some(GmpDate(Some("1"), Some("1"), Some("1990"))), Some("1"), Leaving(GmpDate(None, None, None), None), None)

          "redirect to the revaluation page" in {
            val result = TestGmpController.previousPage(PageType.REVALUATION_RATE, revalSession)
            result.header.status must be(SEE_OTHER)
            result.header.headers.get("LOCATION").get must be(routes.RevaluationController.get().url)
          }
        }

        "when survivor calculation" must {

          val revalSession = GmpSession(MemberDetails("", "", ""), "", CalculationType.SURVIVOR, Some(GmpDate(Some("1"), Some("1"), Some("1990"))), Some("1"), Leaving(GmpDate(None, None, None), None), None)

          "redirect to the date of leaving page" in {
            val result = TestGmpController.previousPage(PageType.REVALUATION_RATE, revalSession)
            result.header.status must be(SEE_OTHER)
            result.header.headers.get("LOCATION").get must be(routes.DateOfLeavingController.get().url)
          }
        }
      }
    }
  }
}
