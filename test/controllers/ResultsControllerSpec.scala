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

import config.ApplicationConfig
import config.ApplicationConfig.globalErrors
import connectors.GmpConnector
import helpers.RandomNino
import metrics.Metrics
import models._
import org.joda.time.LocalDate
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.{OneServerPerSuite, PlaySpec}
import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.{Html, HtmlFormat}
import services.SessionService
import uk.gov.hmrc.play.audit.http.connector.{AuditResult, AuditConnector}
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import views.helpers.GmpDateFormatter._
import play.api.i18n.Messages.Implicits._
import scala.concurrent.Future

class ResultsControllerSpec extends PlaySpec with OneServerPerSuite with MockitoSugar with GmpUsers {

  val mockAuthConnector = mock[GmpAuthConnector]
  val mockSessionService = mock[SessionService]
  val mockCalculationConnector = mock[GmpConnector]
  val mockApplicationConfig = mock[ApplicationConfig]
  val mockAuditConnector = mock[AuditConnector]

  object TestResultsController extends ResultsController(mockAuthConnector, mockSessionService, mockCalculationConnector, mockAuditConnector, Metrics) {
    override val context = FakeGmpContext()

    override def resultsView(response: CalculationResponse, subheader: Option[String], revalSubheader: Option[String])(implicit request: Request[_], context: config.GmpContext): HtmlFormat.Appendable = {
      views.html.results(applicationConfig = mockApplicationConfig, response, subheader, revalSubheader)
    }

  }

  private val nino: String = RandomNino.generate

  val gmpSession = GmpSession(MemberDetails(nino, "John", "Johnson"), "S1234567T", CalculationType.REVALUATION, None, None, Leaving(GmpDate(None, None, None), Some(Leaving.YES_BEFORE)), None)
  val gmpSession2 = GmpSession(MemberDetails(nino, "John", "Johnson"), "S1234567T", CalculationType.REVALUATION, Some(GmpDate(Some("12"), Some("12"), Some("1999"))), None, Leaving(GmpDate(None, None, None), Some(Leaving.NO)), None)
  val gmpSession3 = GmpSession(MemberDetails(nino, "John", "Johnson"), "S1234567T", CalculationType.REVALUATION, Some(GmpDate(Some("12"), Some("12"), Some("1999"))), None, Leaving(GmpDate(None, None, None), Some(Leaving.NO)), equalise = Some(1))

  val gmpSessionWithRate = GmpSession(MemberDetails(nino, "John", "Johnson"), "S1234567T", CalculationType.REVALUATION, None, Some("1"), Leaving(GmpDate(None, None, None), Some(Leaving.NO)), None)
  val gmpSessionWithHMRCRate = GmpSession(MemberDetails(nino, "John", "Johnson"), "S1234567T", CalculationType.REVALUATION, None, Some("0"), Leaving(GmpDate(None, None, None), Some(Leaving.NO)), None)

  val gmpSession4Nino = RandomNino.generate
  val gmpSession4NinoSpaced = gmpSession4Nino.grouped(2).foldLeft(new StringBuilder){
    (sb,s) => {
      s.length match {
        case 2 => sb append s+" "
        case _ => sb append s
      }
    }
  }.toString
  val gmpSession4 = GmpSession(MemberDetails(gmpSession4NinoSpaced, "John", "Johnson"), "S1234567T", CalculationType.REVALUATION, None, None, Leaving(GmpDate(None, None, None), Some(Leaving.NO)), None)

  val gmpSessionSameTaxYear = GmpSession(MemberDetails(nino, "John", "Johnson"), "S1234567T", CalculationType.REVALUATION, Some(GmpDate(Some("07"), Some("07"), Some("2015"))), None,
                                                                                                                                Leaving(GmpDate(Some("07"), Some("07"), Some("2015")), Some(Leaving.YES_BEFORE)),None)

  val gmpSessionDifferentTaxYear = GmpSession(MemberDetails(nino, "John", "Johnson"), "S1234567T", CalculationType.REVALUATION, Some(GmpDate(Some("07"), Some("07"), Some("2015"))), None,
                                                                                                                                     Leaving(GmpDate(Some("07"), Some("07"), Some("2017")), Some(Leaving.YES_AFTER)),None)

  val validCalculationResponse = CalculationResponse("John Johnson", nino, "S1234567T", None, None, List(CalculationPeriod(Some(new
      LocalDate(2015,
        11, 10)),
    new LocalDate(2015, 11, 10), "1.11", "2.22", 1, 0, Some(0))), 0, None, None, None, false, 1)


  val revaluationNotRevaluedSingleResponse = CalculationResponse("John Johnson", nino, "S1234567T", Some("1"),
    revaluationDate = Some(new LocalDate(2015, 7, 7)), List(CalculationPeriod(Some(new LocalDate(2015, 11, 10)),
    new LocalDate(2015, 11, 10), "1.11", "2.22", 1, 0, Some(1))), 0, None, None, None, false, 1)

  val validRevalCalculationResponseMultiplePeriod = CalculationResponse("John Johnson", nino, "S1234567T", Some("2"), Some(new
      LocalDate), List(
    CalculationPeriod(Some(new LocalDate(2015, 11, 10)), new LocalDate(2015, 11, 10), "1.11", "2.22", 1, 0, Some(0)),
    CalculationPeriod(Some(new LocalDate(2014, 11, 9)), new LocalDate(2014, 11, 10), "1.11", "2.22", 1, 56067, Some(0))), 0, None, None, None, false, 1)

  val validRevalCalculationResponseMultiplePeriodErrors = CalculationResponse("John Johnson", nino, "S1234567T", Some("2"), Some(new
      LocalDate), List(
    CalculationPeriod(Some(new LocalDate(2015, 11, 10)), new LocalDate(2015, 11, 10), "1.11", "2.22", 1, 56067, Some(0)),
    CalculationPeriod(Some(new LocalDate(2014, 11, 9)), new LocalDate(2014, 11, 10), "1.11", "2.22", 1, 56067, Some(0))), 0, None, None, None, false, 1)

  val validRevalCalculationResponseSinglePeriod = CalculationResponse("John Johnson", nino, "S1234567T", Some("2"), Some(new
      LocalDate), List(CalculationPeriod(Some(new LocalDate(2014, 11, 9)), new LocalDate(2014, 11, 10), "1.11", "2.22", 1, 0, Some(0))), 0, None, None, None, false, 1)

  val multiErrorResponse = CalculationResponse("John Johnson", nino, "S1234567T", None, Some(new LocalDate(2000, 11, 11)), List(CalculationPeriod
  (Some(new LocalDate(2015, 11, 10)),
    new LocalDate(2015, 11, 10), "0.00", "0.00", 0, 56070, Some(0)), CalculationPeriod(Some(new LocalDate(2010, 11, 10)),
    new LocalDate(2011, 11, 10), "0.00", "0.00", 0, 56007, Some(0))), 0, None, None, None, false, 1)

  val validNonRevalMultipleCalculationResponse = CalculationResponse("John Johnson", nino, "S1234567T", None, None,
    List(CalculationPeriod(Some(new LocalDate(2015, 11, 10)), new LocalDate(2015, 11, 10), "1.11", "2.22", 3, 0, Some(1)),
      CalculationPeriod(Some(new LocalDate(2015, 11, 10)), new LocalDate(2015, 11, 10), "1.11", "2.22", 3, 0, Some(0))), 0, None, None, None, false, 0)

  val dobNotFoundCalculationResponseNino = RandomNino.generate

  val dobNotFoundCalculationResponse = CalculationResponse("Insua", dobNotFoundCalculationResponseNino, "S215173Q", None, None, Nil, 56010, None, None, None, false, 1)

  val transLinkErrorCalculationResponseNino = RandomNino.generate
  val transLinkErrorCalculationResponse = CalculationResponse("Garne", transLinkErrorCalculationResponseNino, "S1608717T", None, None, Nil, 56012, None, None, None, false, 1)

  val single63123ErrorResponse = CalculationResponse("John Johnson", nino, "S1234567T", None, Some(new LocalDate(2000, 11, 11)), List(CalculationPeriod
  (Some(new LocalDate(2015, 11, 10)), new LocalDate(2015, 11, 10), "0.00", "0.00", 0, 63123, Some(0))), 0, None, None, None, false, 1)

  val global63119ErrorResponse = CalculationResponse("John Johnson", nino, "S1234567T", None, None, List(), 63119, None, None, None, false, 1)

  val validCalculationSpaResponse = CalculationResponse("John Johnson", nino, "S1234567T", None, None, List(CalculationPeriod(Some(new
      LocalDate(2015, 11, 10)), new LocalDate(2015, 11, 10), "1.11", "2.22", 1, 0, Some(0))), 0, Some(new LocalDate(2015, 11, 10)), None, None, false, CalculationType.SPA.toInt)

  val validCalculationPayableAgeResponse = CalculationResponse("John Johnson", nino, "S1234567T", None, None, List(CalculationPeriod(Some(new
      LocalDate(2015, 11, 10)), new LocalDate(2015, 11, 10), "1.11", "2.22", 1, 0, Some(0))), 0, None, Some(new LocalDate(2015, 11, 10)), None, false, CalculationType.PAYABLE_AGE.toInt)

  val validCalculationWithContsAndEarningsResponse = CalculationResponse("John Johnson", nino, "S1234567T", None, None, List(CalculationPeriod(Some(new
      LocalDate(2014, 11, 10)), new LocalDate(2015, 11, 10), "1.11", "2.22", 1, 0, Some(0), None, None, None, Some(List(ContributionsAndEarnings(2015, "234.00"),
    ContributionsAndEarnings(2014, "124.00"))))), 0, None, Some(new LocalDate(2015, 11, 10)), None, false, 1)

  val validCalculationWithContsAndEarningsErroredResponse = CalculationResponse("John Johnson", nino, "S1234567T", None, None,
    List(
      CalculationPeriod(
        Some(new LocalDate(2014, 11, 10)), new LocalDate(2015, 11, 10), "1.11", "2.22", 1, 0, Some(0), None, None, None,
        Some(List(
          ContributionsAndEarnings(2015, "234.00"),
          ContributionsAndEarnings(2014, "124.00"))
        )
      ),
      CalculationPeriod(
        Some(new LocalDate(2014, 11, 10)), new LocalDate(2015, 11, 10), "1.11", "2.22", 1, 56067, Some(0), None, None, None,
        Some(List(
          ContributionsAndEarnings(2015, "234.00"),
          ContributionsAndEarnings(2014, "124.00"))
        )
      ),
      CalculationPeriod(
        Some(new LocalDate(2014, 11, 10)), new LocalDate(2015, 11, 10), "1.11", "2.22", 1, 0, Some(0), None, None, None,
        Some(List(
          ContributionsAndEarnings(2015, "234.00"),
          ContributionsAndEarnings(2014, "124.00"))
        )
      ),
      CalculationPeriod(
        Some(new LocalDate(2014, 11, 10)), new LocalDate(2015, 11, 10), "1.11", "2.22", 1, 0, Some(0), None, None, None,
        Some(List(
          ContributionsAndEarnings(2015, "234.00"),
          ContributionsAndEarnings(2014, "124.00"))
        )
      )
    ), 0, None, Some(new LocalDate(2015, 11, 10)), None, false, 1)

  val dualCalcResponse = CalculationResponse("John Johnson", nino, "S1234567T", Some("2"), Some(new LocalDate),
    List(
      CalculationPeriod(Some(new LocalDate(2015, 11, 10)), new LocalDate(2015, 11, 10), "1.11", "2.22", 1, 0, Some(0), Some("1.23"), Some("4.56")),
      CalculationPeriod(Some(new LocalDate(2014, 11, 9)), new LocalDate(2014, 11, 10), "1.11", "2.22", 1, 0, Some(0), Some("1.23"), Some("4.56"))
    ), 0, None, None, None, true, 1)

  val dualCalcResponse2 = CalculationResponse("John Johnson", nino, "S1234567T", Some("2"), Some(new LocalDate),
    List(
      CalculationPeriod(Some(new LocalDate(2015, 11, 10)), new LocalDate(2015, 11, 10), "1.11", "2.22", 1, 0, Some(0), Some("0.00"), Some("4.56")),
      CalculationPeriod(Some(new LocalDate(2014, 11, 9)), new LocalDate(2014, 11, 10), "1.11", "2.22", 1, 0, Some(0), Some("1.23"), Some("0.00"))
    ), 0, None, None, None, true, 1)

  val nonDualCalcResponse = CalculationResponse("John Johnson", nino, "S1234567T", Some("2"), Some(new LocalDate),
    List(
      CalculationPeriod(Some(new LocalDate(2015, 11, 10)), new LocalDate(2015, 11, 10), "1.11", "2.22", 1, 0, Some(0), None, None),
      CalculationPeriod(Some(new LocalDate(2014, 11, 9)), new LocalDate(2014, 11, 10), "1.11", "2.22", 1, 0, Some(0), None, None)
    ), 0, None, None, None, false, 1)

  val validRevaluationMultipleSameTaxYear = CalculationResponse("John Johnson", nino, "S1234567T", Some("0"), Some(new LocalDate(2016,8,24)),
    List(
      CalculationPeriod(Some(new LocalDate(2015, 11, 10)), new LocalDate(2016, 8, 24), "1.11", "2.22", 1, 0, Some(1), None, None),
      CalculationPeriod(Some(new LocalDate(2014, 11, 9)), new LocalDate(2014, 11, 10), "1.11", "2.22", 1, 0, Some(0), None, None)
    ), 0, None, None, None, false, 1)

  val validRevalSingleSameTaxYear = CalculationResponse("John Johnson", nino, "S1234567T", Some("0"), Some(new LocalDate(2016,8,24)),
    List(
      CalculationPeriod(Some(new LocalDate(2015, 11, 10)), new LocalDate(2016, 8, 24), "1.11", "2.22", 1, 0, Some(1), None, None)), 0, None, None, None, false, 1)

  val single58161CalculationResponse = CalculationResponse("John Johnson", nino, "S1234567T", None, None, List(CalculationPeriod(Some(new
      LocalDate(2015, 11, 10)), new LocalDate(2015, 11, 10), "1.11", "2.22", 0, 58161, Some(0))), 0, None, None, None, false, 1)

  val survivorCalculationResponse = CalculationResponse("John Johnson", nino, "S1234567T", None, None, List(CalculationPeriod(Some(new
      LocalDate(2015, 11, 10)), new LocalDate(2015, 11, 10), "1.11", "2.22", 0, 0, Some(0))), 0, None, None, None, false, CalculationType.SURVIVOR.toInt)

  val survivorRevaluationCalculationResponse = CalculationResponse("John Johnson", nino, "S1234567T", None, Some(new LocalDate(2010, 11, 10)), List(CalculationPeriod(Some(new
      LocalDate(2015, 11, 10)), new LocalDate(2015, 11, 10), "1.11", "2.22", 0, 0, Some(0), inflationProofBeyondDod = Some(1))), 0, None, None, None, false, CalculationType.SURVIVOR.toInt)

  val survivorRevaluationCalculationResponseNoInflation = CalculationResponse("John Johnson", nino, "S1234567T", None, revaluationDate = Some(new LocalDate(2015, 9,12)),
        List(CalculationPeriod(Some(new LocalDate(2015, 11, 10)), new LocalDate(2015, 11, 10), "1.11", "2.22", 0, 0, Some(0), inflationProofBeyondDod = Some(0))), 0, None, None, Some(new LocalDate(2016,2,2)), false, CalculationType.SURVIVOR.toInt)

  val single63151CalculationResponse = CalculationResponse("John Johnson", nino, "S1234567T", None, None, List(CalculationPeriod(Some(new
      LocalDate(2015, 11, 10)), new LocalDate(2015, 11, 10), "1.11", "2.22", 0, 63151, Some(0))), 0, None, None, None, false, 1)

  val single63149CalculationResponse = CalculationResponse("John Johnson", nino, "S1234567T", None, None, List(CalculationPeriod(Some(new
      LocalDate(2015, 11, 10)), new LocalDate(2015, 11, 10), "1.11", "2.22", 0, 63149, Some(0))), 0, None, None, None, false, 1)

  val single63148CalculationResponse = CalculationResponse("John Johnson", nino, "S1234567T", None, None, List(CalculationPeriod(Some(new
      LocalDate(2015, 11, 10)), new LocalDate(2015, 11, 10), "1.11", "2.22", 0, 63148, Some(0))), 0, None, None, None, false, 1)

  val single63147CalculationResponse = CalculationResponse("John Johnson", nino, "S1234567T", None, None, List(CalculationPeriod(Some(new
      LocalDate(2015, 11, 10)), new LocalDate(2015, 11, 10), "1.11", "2.22", 0, 63147, Some(0))), 0, None, None, None, false, 1)

  val single63150CalculationResponse = CalculationResponse("John Johnson", nino, "S1234567T", None, None, List(CalculationPeriod(Some(new
      LocalDate(2015, 11, 10)), new LocalDate(2015, 11, 10), "1.11", "2.22", 0, 63150, Some(0))), 0, None, None, None, false, 1)

  val single63167CalculationResponse = CalculationResponse("John Johnson", nino, "S1234567T", None, None, List(CalculationPeriod(Some(new
      LocalDate(2015, 11, 10)), new LocalDate(2015, 11, 10), "1.11", "2.22", 0, 63167, Some(0))), 0, None, None, None, false, 1)

  val survivor63167CalculationResponse = CalculationResponse("John Johnson", nino, "S1234567T", None, None, List(), 0, None, None, None, false, 3)

  "ResultsController" must {

    "respond to GET /guaranteed-minimum-pension/results" in {
      val result = route(FakeRequest(GET, "/guaranteed-minimum-pension/results"))
      status(result.get) must not equal (NOT_FOUND)
    }

    "GET" must {

      "require authorisation" in {
        val result = TestResultsController.get.apply(FakeRequest())
        status(result) must equal(SEE_OTHER)
        redirectLocation(result).get must include("/gg/sign-in")
      }

      "when authorised" must {

        "respond with a status of OK" in {
          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession)))
          when(mockCalculationConnector.calculateSingle(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(validCalculationResponse))
          withAuthorisedUser { request =>
            val result = TestResultsController.get.apply(request)
            status(result) must equal(OK)
            contentAsString(result) must include(Messages("gmp.button.request-another"))
            contentAsString(result) must include(Messages("gmp.back.link"))
          }
        }

        "load the results page without revalrate when dol" in {
          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession)))
          when(mockCalculationConnector.calculateSingle(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(validCalculationResponse))
          withAuthorisedUser { request =>
            val result = TestResultsController.get.apply(request)
            contentAsString(result) must not include (Messages("gmp.revaluation.rate"))
            contentAsString(result) must include(Messages("gmp.leaving.scheme.header", formatDate(validCalculationResponse.leavingDate)))
          }
        }

        "load the results page when revaluation date has been wiped" in {
          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession)))
          when(mockCalculationConnector.calculateSingle(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(validCalculationResponse))
          withAuthorisedUser { request =>
            val result = TestResultsController.get.apply(request)
            contentAsString(result) must include(Messages("gmp.back.link"))
          }
        }

        "load the results page when revaluation date exists with revaluation S148" in {
          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession.copy(rate = Some(RevaluationRate
            .S148)))))
          when(mockCalculationConnector.calculateSingle(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(validCalculationResponse))
          withAuthorisedUser { request =>
            val result = TestResultsController.get.apply(request)
            contentAsString(result) must include(Messages("gmp.entered_details.title"))
          }
        }

        "load the multiple period results page when revaluation date and termiantion date are the same and with member still in the scheme" in {

          val date = GmpDate(day = Some("24"), month = Some("08"), year = Some("2016"))

          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(
            Future.successful(Some(gmpSession.copy(revaluationDate = Some(date), rate = Some(RevaluationRate.HMRC), leaving = Leaving(date, Some(Leaving.NO))))))

          when(mockCalculationConnector.calculateSingle(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(validRevaluationMultipleSameTaxYear))
          withAuthorisedUser { request =>
            val result = TestResultsController.get.apply(request)
            contentAsString(result) must not include (Messages("gmp.notrevalued.subheader"))
            contentAsString(result) must include(Messages("gmp.leaving.revalued.header", "24 August 2016", "HMRC held"))
            contentAsString(result) must include(Messages("--"))
          }

        }

        "load the single period results page when revaluation date and termiantion date are the same and with member still in the scheme" in {

          val date = GmpDate(day = Some("24"), month = Some("08"), year = Some("2016"))

          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(
            Future.successful(Some(gmpSession.copy(revaluationDate = Some(date), leaving = Leaving(date, Some(Leaving.NO))))))

          when(mockCalculationConnector.calculateSingle(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(validRevalSingleSameTaxYear))
          withAuthorisedUser { request =>
            val result = TestResultsController.get.apply(request)
            contentAsString(result) must include(Messages("gmp.notrevalued.subheader"))
            contentAsString(result) must include(Messages("gmp.leaving.scheme.header", "24 August 2016"))

          }

        }

        //spa calculation

        "load the results page for spa" in {
          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession)))
          when(mockCalculationConnector.calculateSingle(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(validCalculationSpaResponse))
          withAuthorisedUser { request =>
            val result = TestResultsController.get.apply(request)
            contentAsString(result) must include(Messages("gmp.spa.header", "10 November 2015"))
          }
        }

        //payable age calculation

        "load the results page for payable age" in {
          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession)))
          when(mockCalculationConnector.calculateSingle(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(validCalculationPayableAgeResponse))
          withAuthorisedUser { request =>
            val result = TestResultsController.get.apply(request)
            contentAsString(result) must include(Messages("gmp.payable_age.header", "10 November 2015"))
          }
        }

        //survivor

        "show the correct header when survivor and not revaluing" in {
          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession.copy(scenario = CalculationType.SURVIVOR))))
          when(mockCalculationConnector.calculateSingle(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(survivorCalculationResponse))
          withAuthorisedUser { request =>
            val result = TestResultsController.get.apply(request)
            contentAsString(result) must include("Surviving partner’s GMP entitlement at date of death")
            contentAsString(result) must include(Messages("gmp.total.entitlement"))
            contentAsString(result) must include(Messages("gmp.post88.entitlement"))
          }
        }

        "show the correct header when survivor and revaluing" in {
          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession.copy(scenario = CalculationType.SURVIVOR))))
          when(mockCalculationConnector.calculateSingle(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(survivorRevaluationCalculationResponse))
          withAuthorisedUser { request =>
            val result = TestResultsController.get.apply(request)
            contentAsString(result) must include("Surviving partner’s GMP entitlement at 10 November 2010")
            contentAsString(result) must include(Messages("gmp.results.survivior.disclaimer"))
          }
        }

        "show the correct subheader when survivor and no inflation proof" in {
          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession.copy(scenario = CalculationType.SURVIVOR))))
          when(mockCalculationConnector.calculateSingle(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(survivorRevaluationCalculationResponse))
          withAuthorisedUser { request =>
            val result = TestResultsController.get.apply(request)
            contentAsString(result) must not include (Messages("gmp.no_inflation.subheader"))
          }
        }

        "show the correct subheader when survivor and inflation proof" in {
          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession.copy(scenario = CalculationType.SURVIVOR))))
          when(mockCalculationConnector.calculateSingle(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(survivorRevaluationCalculationResponseNoInflation))
          withAuthorisedUser { request =>
            val result = TestResultsController.get.apply(request)
            contentAsString(result) must include(Messages("gmp.no_inflation.subheader"))
          }
        }

        "show the returned date of death and the correct header when survivor and no inflation proof" in {
          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession.copy(scenario = CalculationType.SURVIVOR))))
          when(mockCalculationConnector.calculateSingle(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(
            survivorRevaluationCalculationResponseNoInflation.copy(dateOfDeath = Some(new LocalDate("2017-01-01")), revaluationDate = None)))
          withAuthorisedUser { request =>
            val result = TestResultsController.get.apply(request)
            contentAsString(result) must include("Surviving partner’s GMP entitlement at date of death (1 January 2017)")
            contentAsString(result) must not include (Messages("gmp.no_inflation.subheader"))
          }
        }

        // Single/Multiple DOL

        "show the correct header and subheader when leaving the scheme with single result" in {
          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession)))
          when(mockCalculationConnector.calculateSingle(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(validCalculationResponse.copy(calcType = 0)))
          withAuthorisedUser { request =>
            val result = TestResultsController.get.apply(request)
            contentAsString(result) must include(Messages("gmp.notrevalued.subheader"))
          }
        }

        "show the correct header and subheader when leaving the scheme with multiple results" in {
          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession)))
          when(mockCalculationConnector.calculateSingle(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful
          (validNonRevalMultipleCalculationResponse))
          withAuthorisedUser { request =>
            val result = TestResultsController.get.apply(request)
            contentAsString(result) must include(Messages("gmp.leaving.scheme.header", formatDate(validNonRevalMultipleCalculationResponse.leavingDate)))
            contentAsString(result) must include(Messages("gmp.notrevalued.multi.subheader"))
            contentAsString(result) must not include ("<td id=\"rate-@count\" class=\"numeric\">")
          }
        }

        // Non revaluation single transfer/divorce (DOL)

        "show the non-revalued header and subheader correctly when transferring with single result" in {
          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession)))
          when(mockCalculationConnector.calculateSingle(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful
          (revaluationNotRevaluedSingleResponse))
          withAuthorisedUser { request =>
            val result = TestResultsController.get.apply(request)
            contentAsString(result) must include(Messages("gmp.leaving.scheme.header", formatDate(revaluationNotRevaluedSingleResponse.leavingDate)))
            contentAsString(result) must include(Messages("gmp.notrevalued.subheader"))
          }
        }

        // Non revaluation multiple transfer/divorce (DOL)

        "show the non-revalued header and subheader correctly when transferring with multiple result" in {
          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession)))
          when(mockCalculationConnector.calculateSingle(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful
          (validNonRevalMultipleCalculationResponse.copy(revaluationRate = None)))
          withAuthorisedUser { request =>
            val result = TestResultsController.get.apply(request)
            contentAsString(result) must include(Messages("gmp.leaving.scheme.header", formatDate(validNonRevalMultipleCalculationResponse.leavingDate)))
            contentAsString(result) must include(Messages("gmp.notrevalued.multi.subheader"))
          }
        }

        // Revaluation single transfer/divorce

        "show the revalued header correctly when transferring with single result" in {
          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession)))
          when(mockCalculationConnector.calculateSingle(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful
          (validCalculationResponse.copy(revaluationDate = Some(new LocalDate(2000, 11, 11)), revaluationRate = Some("1"))))
          withAuthorisedUser { request =>
            val result = TestResultsController.get.apply(request)
            contentAsString(result) must include(Messages("gmp.leaving.revalued.header", "11 November 2000", "S148"))
          }
        }

        "show the dol header correctly when transferring with single result that was not revalued" in {
          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession)))
          when(mockCalculationConnector.calculateSingle(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful
          (revaluationNotRevaluedSingleResponse))
          withAuthorisedUser { request =>
            val result = TestResultsController.get.apply(request)
            contentAsString(result) must include(Messages("gmp.leaving.scheme.header", formatDate(revaluationNotRevaluedSingleResponse.leavingDate)))
            contentAsString(result) must include(Messages("gmp.notrevalued.subheader"))
          }
        }

        "not show the returned rate on member details table when transferring with single result" in {
          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession)))
          when(mockCalculationConnector.calculateSingle(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful
          (validCalculationResponse.copy(revaluationDate = Some(new LocalDate(2000, 11, 11)), revaluationRate = Some("1"))))
          withAuthorisedUser { request =>
            val result = TestResultsController.get.apply(request)
            contentAsString(result) must not include ("<td id=\"gmp-rate\">")
            contentAsString(result).replaceAll("&#x27;", "'") must include(Messages("gmp.queryhandling.resultsmessage"))
          }
        }

        "show the revalued header correctly when divorcing with single result" in {
          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession)))
          when(mockCalculationConnector.calculateSingle(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful
          (validCalculationResponse.copy(revaluationDate = Some(new LocalDate(2000, 11, 11)), revaluationRate = Some("1"))))
          withAuthorisedUser { request =>
            val result = TestResultsController.get.apply(request)
            contentAsString(result) must include(Messages("gmp.leaving.revalued.header", "11 November 2000", "S148"))
          }
        }

        // Revaluation multiple transfer/divorce

        "show the revalued header correctly when transferring with multiple result" in {
          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession)))
          when(mockCalculationConnector.calculateSingle(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful
          (validNonRevalMultipleCalculationResponse.copy(revaluationDate = Some(new LocalDate(2000, 11, 11)), revaluationRate = Some("0"))))
          withAuthorisedUser { request =>
            val result = TestResultsController.get.apply(request)
            contentAsString(result) must include(Messages("gmp.leaving.revalued.header", "11 November 2000", "HMRC held"))
          }
        }

        "show the revalued header correctly when divorcing with multiple result" in {
          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession)))
          when(mockCalculationConnector.calculateSingle(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful
          (validNonRevalMultipleCalculationResponse.copy(revaluationDate = Some(new LocalDate(2000, 11, 11)), revaluationRate = Some("3"))))
          withAuthorisedUser { request =>
            val result = TestResultsController.get.apply(request)
            contentAsString(result) must include(Messages("gmp.leaving.revalued.header", "11 November 2000", "Limited"))
          }
        }

        "show non revalued sub-header when revaluation in the same tax year" in {
          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSessionSameTaxYear)))
          when(mockCalculationConnector.calculateSingle(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(revaluationNotRevaluedSingleResponse))

          withAuthorisedUser { request =>
            val result = TestResultsController.get.apply(request)
            contentAsString(result) must include(Messages("gmp.leaving.scheme.header", formatDate(new LocalDate(2015, 7, 7))))
            contentAsString(result) must include(Messages("gmp.notrevalued.subheader"))
          }
        }

        "not show non revalued sub-header when revaluation not in the same tax year" in {
          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSessionDifferentTaxYear)))
          when(mockCalculationConnector.calculateSingle(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(nonDualCalcResponse))

          withAuthorisedUser { request =>
            val result = TestResultsController.get.apply(request)
            contentAsString(result) must not include (Messages("gmp.notrevalued.subheader"))
          }
        }

        "show the rate column in the multiple results table, when hmrc held rate" in {
          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession)))
          when(mockCalculationConnector.calculateSingle(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful
          (validRevalCalculationResponseMultiplePeriod.copy(revaluationRate = Some("0"))))
          withAuthorisedUser { request =>
            val result = TestResultsController.get.apply(request)
            contentAsString(result) must include(Messages("gmp.rate"))
          }
        }

        "not show the rate column in the multiple results table, when not hmrc held rate" in {
          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession)))
          when(mockCalculationConnector.calculateSingle(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful
          (validRevalCalculationResponseMultiplePeriod.copy(revaluationRate = Some("1"))))
          withAuthorisedUser { request =>
            val result = TestResultsController.get.apply(request)
            contentAsString(result) must not include (Messages("gmp.rate"))
          }
        }

        "show the actual rate in the single period results, when hmrc held rate" in {
          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession.copy(rate = Some("0")))))
          when(mockCalculationConnector.calculateSingle(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(validRevalCalculationResponseSinglePeriod))
          withAuthorisedUser { request =>
            val result = TestResultsController.get.apply(request)
            contentAsString(result) must include(Messages("gmp.revaluation_rate.type_2"))
          }
        }

        "show correct error page title" in {
          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession)))
          when(mockCalculationConnector.calculateSingle(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(single63123ErrorResponse))
          withAuthorisedUser { request =>
            val result = TestResultsController.get.apply(request)
            val content = contentAsString(result).replaceAll("&#x27;", "'")
            content must include(Messages("gmp.results.error"))
          }
        }

        "show error box with member details single period" in {
          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession)))
          when(mockCalculationConnector.calculateSingle(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(single63123ErrorResponse))
          withAuthorisedUser { request =>
            val result = TestResultsController.get.apply(request)
            val content = contentAsString(result).replaceAll("&#x27;", "'")
            content must include(Messages("gmp.cannot_calculate"))
            content must include(Messages(globalErrors.getString("63123.reason")))
            content must include(Messages(globalErrors.getString("63123.solution")))
            content must include(Messages(globalErrors.getString("63123.also")))
            content must include(Messages("gmp.entered_details.title"))
            content must not include (Messages("gmp.rate"))
            content must not include (Messages("gmp.back_to_dashboard"))
          }
        }

        "show error single period for 58161" in {
          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession)))
          when(mockCalculationConnector.calculateSingle(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(single58161CalculationResponse))
          withAuthorisedUser { request =>
            val result = TestResultsController.get.apply(request)
            val content = contentAsString(result).replaceAll("&#x27;", "'")
            content must include(Messages("gmp.cannot_calculate"))
            content must include(Messages(globalErrors.getString("58161.reason")))
            content must include(Messages(globalErrors.getString("58161.solution")))
            content must include(Messages("gmp.entered_details.title"))
            content must not include (Messages("gmp.rate"))
          }
        }

        "show error single period for 63151" in {
          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession)))
          when(mockCalculationConnector.calculateSingle(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(single63151CalculationResponse))
          withAuthorisedUser { request =>
            val result = TestResultsController.get.apply(request)
            val content = contentAsString(result).replaceAll("&#x27;", "'")
            content must include(Messages("gmp.cannot_calculate"))
            content must include(Messages(globalErrors.getString("63151.reason")))
            content must include(Messages(globalErrors.getString("63151.solution")))
            content must include(Messages(globalErrors.getString("63151.also")))
          }
        }

        "show error single period for 63149" in {
          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession)))
          when(mockCalculationConnector.calculateSingle(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(single63149CalculationResponse))
          withAuthorisedUser { request =>
            val result = TestResultsController.get.apply(request)
            val content = contentAsString(result).replaceAll("&#x27;", "'")
            content must include(Messages("gmp.cannot_calculate"))
            content must include(Messages(globalErrors.getString("63149.reason")))
            content must include(Messages(globalErrors.getString("63149.solution")))
            content must include(Messages(globalErrors.getString("63149.also")))
          }
        }

        "show error single period for 63148" in {
          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession)))
          when(mockCalculationConnector.calculateSingle(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(single63148CalculationResponse))
          withAuthorisedUser { request =>
            val result = TestResultsController.get.apply(request)
            val content = contentAsString(result).replaceAll("&#x27;", "'")
            content must include(Messages("gmp.cannot_calculate"))
            content must include(Messages(globalErrors.getString("63148.reason")))
            content must include(Messages(globalErrors.getString("63148.solution")))
            content must include(Messages(globalErrors.getString("63148.also")))
          }
        }

        "show error single period for 63147" in {
          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession)))
          when(mockCalculationConnector.calculateSingle(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(single63147CalculationResponse))
          withAuthorisedUser { request =>
            val result = TestResultsController.get.apply(request)
            val content = contentAsString(result).replaceAll("&#x27;", "'")
            content must include(Messages("gmp.cannot_calculate"))
            content must include(Messages(globalErrors.getString("63147.reason")))
            content must include(Messages(globalErrors.getString("63147.solution")))
            content must include(Messages(globalErrors.getString("63147.also")))
          }
        }

        "show error single period for 63150" in {
          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession)))
          when(mockCalculationConnector.calculateSingle(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(single63150CalculationResponse))
          withAuthorisedUser { request =>
            val result = TestResultsController.get.apply(request)
            val content = contentAsString(result).replaceAll("&#x27;", "'")
            content must include(Messages("gmp.cannot_calculate"))
            content must include(Messages(globalErrors.getString("63150.reason")))
            content must include(Messages(globalErrors.getString("63150.solution")))
          }
        }

        "show error single period for 63167" in {
          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession)))
          when(mockCalculationConnector.calculateSingle(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(single63167CalculationResponse))
          withAuthorisedUser { request =>
            val result = TestResultsController.get.apply(request)
            val content = contentAsString(result).replaceAll("&#x27;", "'")
            content must include(Messages("gmp.cannot_calculate"))
            content must include(Messages(globalErrors.getString("63167.reason")))
            content must include(Messages(globalErrors.getString("63167.solution")))
          }
        }

        "show error box with member details global" in {
          when(mockCalculationConnector.calculateSingle(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(global63119ErrorResponse))
          withAuthorisedUser { request =>
            val result = TestResultsController.get.apply(request)
            val content = contentAsString(result).replaceAll("&#x27;", "'")
            content must include(Messages("gmp.cannot_calculate"))
            content must include(Messages(globalErrors.getString("63119.reason")))
            content must include(Messages(globalErrors.getString("63119.solution")))
            content must include(Messages("gmp.entered_details.title"))
          }
        }

        "shows errors in multi results pages" in {
          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession)))
          when(mockCalculationConnector.calculateSingle(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(multiErrorResponse))
          withAuthorisedUser { request =>
            val result = TestResultsController.get.apply(request)
            val content = contentAsString(result).replaceAll("&#x27;", "'")
            content must include(Messages("gmp.part_problem"))
            content must include(Messages(globalErrors.getString(s"${multiErrorResponse.calculationPeriods.head.errorCode}.reason")))
            content must include(Messages(globalErrors.getString(s"${multiErrorResponse.calculationPeriods.tail.head.errorCode}.reason")))
            content must include(Messages("gmp.queryhandling.resultsmessage"))
            content must not include (Messages("gmp.back_to_dashboard"))
          }
        }

        "show the query handling message" in {
          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession)))
          when(mockCalculationConnector.calculateSingle(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(validCalculationResponse))
          withAuthorisedUser { request =>
            val result = TestResultsController.get.apply(request)
            contentAsString(result).replaceAll("&#x27;", "'") must include(Messages("gmp.queryhandling.resultsmessage"))
          }
        }

        "when returns global error" must {

          "display global error message page" in {
            when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession)))
            when(mockCalculationConnector.calculateSingle(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful
            (dobNotFoundCalculationResponse))

            withAuthorisedUser { request =>
              val result = TestResultsController.get.apply(request)
              val content = contentAsString(result).replaceAll("&#x27;", "'")
              content must include(Messages("gmp.cannot_calculate"))
              content.replaceAll("&#x27;", "'") must include(Messages(globalErrors.getString(s"${dobNotFoundCalculationResponse.globalErrorCode}.reason")))
              content must include(Messages("gmp.what_now"))
              content must include(Messages(globalErrors.getString(s"${dobNotFoundCalculationResponse.globalErrorCode}.solution")))
              contentAsString(result) must include(Messages("gmp.button.request-another"))
              contentAsString(result) must include(Messages("gmp.entered_details.title"))
            }
          }

          "display a different global error message page" in {
            when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession)))
            when(mockCalculationConnector.calculateSingle(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful
            (transLinkErrorCalculationResponse))

            withAuthorisedUser { request =>
              val result = TestResultsController.get.apply(request)
              val content = contentAsString(result).replaceAll("&#x27;", "'")
              content must include(Messages("gmp.cannot_calculate"))
              content must include(Messages("gmp.what_now"))
              content must include(Messages(globalErrors.getString(s"${transLinkErrorCalculationResponse.globalErrorCode}.solution")))
            }
          }

        }

        "go to failure page when session not returned" in {
          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))

          withAuthorisedUser { request =>
            val result = TestResultsController.get.apply(request)
            contentAsString(result)replaceAll("&#x27;", "'") must include (Messages("gmp.cannot_calculate.gmp"))
            contentAsString(result) must include (Messages("gmp.error.session_parts_missing", "/guaranteed-minimum-pension/dashboard"))
          }
        }

      }


      "subheader" must {

        "be non existent when errors are returned" in {
          val date = GmpDate(day = Some("24"), month = Some("08"), year = Some("2016"))
          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSessionWithHMRCRate.copy(leaving = Leaving(date, Some(Leaving.YES_AFTER))))))
          when(mockCalculationConnector.calculateSingle(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(survivor63167CalculationResponse))
          withAuthorisedUser { request =>
            val result = TestResultsController.get.apply(request)
            status(result) must equal(OK)
          }
        }

        "show the correct subheader when gmp payable age and member left scheme and hmrc rate entered" in {
          val date = GmpDate(day = Some("24"), month = Some("08"), year = Some("2016"))
          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSessionWithHMRCRate.copy(leaving = Leaving(date, Some(Leaving.YES_AFTER))))))
          when(mockCalculationConnector.calculateSingle(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(validCalculationResponse.copy(revaluationRate = Some("0"), calcType = 2)))
          withAuthorisedUser { request =>
            val result = TestResultsController.get.apply(request)
            contentAsString(result) must include("Revaluation rate chosen: HMRC held rate (S148).")
          }
        }

        "show the correct subheader when gmp payable age and member left scheme and rate entered" in {
          val date = GmpDate(day = Some("24"), month = Some("08"), year = Some("2016"))
          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSessionWithRate.copy(leaving = Leaving(date, Some(Leaving.YES_AFTER))))))
          when(mockCalculationConnector.calculateSingle(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(validCalculationResponse.copy(revaluationRate = Some("1"), calcType = 2)))
          withAuthorisedUser { request =>
            val result = TestResultsController.get.apply(request)
            contentAsString(result) must include(Messages("gmp.chosen_rate.subheader", "S148."))
          }
        }

        "show the correct subheader when gmp payable age and member left scheme and rate not entered" in {
          val date = GmpDate(day = Some("24"), month = Some("08"), year = Some("2016"))
          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSessionWithRate.copy(leaving = Leaving(date, Some(Leaving.YES_AFTER))))))
          when(mockCalculationConnector.calculateSingle(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(validCalculationResponse.copy(calcType = 2)))
          withAuthorisedUser { request =>
            val result = TestResultsController.get.apply(request)
            contentAsString(result) must include(Messages("gmp.held_rate.subheader", "S148."))
          }
        }

        "show no subheader when gmp payable age and member still in scheme and rate" in {
          val date = GmpDate(day = Some("24"), month = Some("08"), year = Some("2016"))
          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSessionWithRate.copy(leaving = Leaving(date, Some(Leaving.NO))))))
          when(mockCalculationConnector.calculateSingle(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(validCalculationResponse.copy(calcType = 2)))
          withAuthorisedUser { request =>
            val result = TestResultsController.get.apply(request)
            contentAsString(result) must not include (Messages("gmp.held_rate.subheader", "S148."))
          }
        }

        "show no subheader when gmp payable age and member still in scheme and no rate" in {
          val date = GmpDate(day = Some("24"), month = Some("08"), year = Some("2016"))
          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession.copy(leaving = Leaving(date, Some(Leaving.NO))))))
          when(mockCalculationConnector.calculateSingle(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(validCalculationResponse.copy(calcType = 2)))
          withAuthorisedUser { request =>
            val result = TestResultsController.get.apply(request)
            contentAsString(result) must not include (Messages("gmp.held_rate.subheader", "S148."))
          }
        }

        "show the correct subheader when state pension age and member left scheme and rate entered" in {
          val date = GmpDate(day = Some("24"), month = Some("08"), year = Some("2016"))
          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSessionWithRate.copy(leaving = Leaving(date, Some(Leaving.YES_AFTER))))))
          when(mockCalculationConnector.calculateSingle(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(validCalculationResponse.copy(revaluationRate = Some("1"), calcType = 4)))
          withAuthorisedUser { request =>
            val result = TestResultsController.get.apply(request)
            contentAsString(result) must include(Messages("gmp.chosen_rate.subheader", "S148."))
          }
        }

        "show the correct subheader when state pension age and member left scheme and rate not entered" in {
          val date = GmpDate(day = Some("24"), month = Some("08"), year = Some("2016"))
          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSessionWithRate.copy(leaving = Leaving(date, Some(Leaving.YES_AFTER))))))
          when(mockCalculationConnector.calculateSingle(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(validCalculationResponse.copy(calcType = 4)))
          withAuthorisedUser { request =>
            val result = TestResultsController.get.apply(request)
            contentAsString(result) must include(Messages("gmp.held_rate.subheader", "S148."))
          }
        }

        "show no subheader when state pension age and member still in scheme" in {
          val date = GmpDate(day = Some("24"), month = Some("08"), year = Some("2016"))
          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSessionWithRate.copy(leaving = Leaving(date, Some(Leaving.NO))))))
          when(mockCalculationConnector.calculateSingle(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(validCalculationResponse.copy(calcType = 4)))
          withAuthorisedUser { request =>
            val result = TestResultsController.get.apply(request)
            contentAsString(result) must not include (Messages("gmp.held_rate.subheader", "S148."))
          }
        }

        "show no subheader when state pension age and member still in scheme and no rate" in {
          val date = GmpDate(day = Some("24"), month = Some("08"), year = Some("2016"))
          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession.copy(leaving = Leaving(date, Some(Leaving.NO))))))
          when(mockCalculationConnector.calculateSingle(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(validCalculationResponse.copy(calcType = 2)))
          withAuthorisedUser { request =>
            val result = TestResultsController.get.apply(request)
            contentAsString(result) must not include (Messages("gmp.held_rate.subheader", "S148."))
          }
        }

        "show the correct subheader when survivor and member left scheme and hmrc rate entered and no inflation proof" in {
          val date = GmpDate(day = Some("24"), month = Some("08"), year = Some("2016"))
          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSessionWithHMRCRate.copy(leaving = Leaving(date, Some(Leaving.YES_AFTER))))))
          when(mockCalculationConnector.calculateSingle(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(survivorRevaluationCalculationResponseNoInflation.copy(revaluationRate = Some("0"),
            calculationPeriods = List(CalculationPeriod(Some(new LocalDate(2015, 11, 10)), new LocalDate(2015, 11, 10), "1.11", "2.22", 1, 0, Some(0), inflationProofBeyondDod = Some(0))))))
          withAuthorisedUser { request =>
            val result = TestResultsController.get.apply(request)
            contentAsString(result) must include("Revaluation rate chosen: HMRC held rate (S148).")
            contentAsString(result) must include(Messages("gmp.no_inflation.subheader"))
          }
        }

        "show the correct subheader when survivor and member left scheme, rate entered and inflation proof" in {
          val date = GmpDate(day = Some("24"), month = Some("08"), year = Some("2016"))
          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSessionWithHMRCRate.copy(leaving = Leaving(date, Some(Leaving.YES_AFTER))))))
          when(mockCalculationConnector.calculateSingle(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(survivorRevaluationCalculationResponse.copy(revaluationRate = Some("1"), calculationPeriods = List(CalculationPeriod(Some(new
              LocalDate(2015, 11, 10)), new LocalDate(2015, 11, 10), "1.11", "2.22", 1, 0, Some(0), inflationProofBeyondDod = Some(1))))))
          withAuthorisedUser { request =>
            val result = TestResultsController.get.apply(request)
            contentAsString(result) must include(Messages("gmp.chosen_rate.subheader", "S148."))
            contentAsString(result) must not include (Messages("gmp.no_inflation.subheader"))
          }
        }

        "show no subheader when survivor and member still in scheme" in {
          val date = GmpDate(day = Some("24"), month = Some("08"), year = Some("2016"))
          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSessionWithRate.copy(leaving = Leaving(date, Some(Leaving.NO))))))
          when(mockCalculationConnector.calculateSingle(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(survivorRevaluationCalculationResponseNoInflation.copy(revaluationRate = Some("0"),
            calculationPeriods = List(CalculationPeriod(Some(new LocalDate(2015, 11, 10)), new LocalDate(2015, 11, 10), "1.11", "2.22", 1, 0, Some(0), inflationProofBeyondDod = Some(0))))))
          withAuthorisedUser { request =>
            val result = TestResultsController.get.apply(request)
            contentAsString(result) must not include ("Revaluation rate chosen: HMRC held rate (S148).")
            contentAsString(result) must not include (Messages("gmp.no_inflation.subheader"))
          }
        }
      }
    }

    "respond to get contributions and earnings /guaranteed-minimum-pension/contributions-earnings" in {
      val result = route(FakeRequest(GET, "/guaranteed-minimum-pension/contributions-earnings"))
      status(result.get) must not equal (NOT_FOUND)
    }

    "Get contributions and earnings" must {

      "require authorisation" in {
        val result = TestResultsController.get.apply(FakeRequest())
        status(result) must equal(SEE_OTHER)
        redirectLocation(result).get must include("/gg/sign-in")
      }

      "when authorised" must {


        "respond with a status of OK" in {
          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession)))
          when(mockCalculationConnector.calculateSingle(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(validCalculationWithContsAndEarningsResponse))
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          withAuthorisedUser { request =>
            val result = TestResultsController.getContributionsAndEarnings.apply(request)
            status(result) must equal(OK)
            contentAsString(result) must include(Messages("gmp.contributions_earnings.header"))
          }
        }

        "respond with a status of OK when auditconnector fails" in {
          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession)))
          when(mockCalculationConnector.calculateSingle(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(validCalculationWithContsAndEarningsResponse))
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.failed(new Exception()))
          withAuthorisedUser { request =>
            val result = TestResultsController.getContributionsAndEarnings.apply(request)
            status(result) must equal(OK)
            contentAsString(result) must include(Messages("gmp.contributions_earnings.header"))
          }
        }

        "respond with a status of OK when response contains global error" in {
          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession)))
          when(mockCalculationConnector.calculateSingle(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(validCalculationWithContsAndEarningsResponse.copy(globalErrorCode = 1)))
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          withAuthorisedUser { request =>
            val result = TestResultsController.getContributionsAndEarnings.apply(request)
            status(result) must equal(OK)
            contentAsString(result) must include(Messages("gmp.contributions_earnings.header"))
          }
        }

        "go to failure page when session not returned" in {
          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
          withAuthorisedUser { request =>
            val result = TestResultsController.getContributionsAndEarnings.apply(request)
            contentAsString(result)replaceAll("&#x27;", "'") must include (Messages("gmp.cannot_calculate.gmp"))
            contentAsString(result) must include (Messages("gmp.error.session_parts_missing", "/guaranteed-minimum-pension/dashboard"))
          }
        }

        "go to failure page when session missing scon" in {
          val emptySession = GmpSession(MemberDetails("", "", ""), "", "", None, None, Leaving(GmpDate(None, None, None), None), None)
          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(emptySession)))
          withAuthorisedUser { request =>
            val result = TestResultsController.getContributionsAndEarnings.apply(request)
            contentAsString(result)replaceAll("&#x27;", "'") must include (Messages("gmp.cannot_calculate.gmp"))
            contentAsString(result) must include (Messages("gmp.error.session_parts_missing", "/guaranteed-minimum-pension/pension-details"))
          }
        }

        "go to failure page when session missing nino" in {
          val emptySession = GmpSession(MemberDetails("", "", ""), "S1234567T", "", None, None, Leaving(GmpDate(None, None, None), None), None)
          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(emptySession)))
          withAuthorisedUser { request =>
            val result = TestResultsController.getContributionsAndEarnings.apply(request)
            contentAsString(result)replaceAll("&#x27;", "'") must include (Messages("gmp.cannot_calculate.gmp"))
            contentAsString(result) must include (Messages("gmp.error.session_parts_missing", "/guaranteed-minimum-pension/member-details"))
          }
        }

        "go to failure page when session missing firstname" in {
          val emptySession = GmpSession(MemberDetails(nino, "", ""), "S1234567T", "", None, None, Leaving(GmpDate(None, None, None), None), None)
          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(emptySession)))
          withAuthorisedUser { request =>
            val result = TestResultsController.getContributionsAndEarnings.apply(request)
            contentAsString(result)replaceAll("&#x27;", "'") must include (Messages("gmp.cannot_calculate.gmp"))
            contentAsString(result) must include (Messages("gmp.error.session_parts_missing", "/guaranteed-minimum-pension/member-details"))
          }
        }

        "go to failure page when session missing lastname" in {
          val emptySession = GmpSession(MemberDetails(nino, "A", ""), "S1234567T", "", None, None, Leaving(GmpDate(None, None, None), None), None)
          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(emptySession)))
          withAuthorisedUser { request =>
            val result = TestResultsController.getContributionsAndEarnings.apply(request)
            contentAsString(result)replaceAll("&#x27;", "'") must include (Messages("gmp.cannot_calculate.gmp"))
            contentAsString(result) must include (Messages("gmp.error.session_parts_missing", "/guaranteed-minimum-pension/member-details"))
          }
        }

        "go to failure page when session missing scenario" in {
          val emptySession = GmpSession(MemberDetails(nino, "A", "AAA"), "S1234567T", "", None, None, Leaving(GmpDate(None, None, None), None), None)
          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(emptySession)))
          withAuthorisedUser { request =>
            val result = TestResultsController.getContributionsAndEarnings.apply(request)
            contentAsString(result)replaceAll("&#x27;", "'") must include (Messages("gmp.cannot_calculate.gmp"))
            contentAsString(result) must include (Messages("gmp.error.session_parts_missing", "/guaranteed-minimum-pension/calculation-reason"))
          }
        }

        "go to failure page when session missing leaving" in {
          val emptySession = GmpSession(MemberDetails(nino, "A", "AAA"), "S1234567T", "0", None, None, Leaving(GmpDate(None, None, None), None), None)
          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(emptySession)))
          withAuthorisedUser { request =>
            val result = TestResultsController.getContributionsAndEarnings.apply(request)
            contentAsString(result)replaceAll("&#x27;", "'") must include (Messages("gmp.cannot_calculate.gmp"))
            contentAsString(result) must include (Messages("gmp.error.session_parts_missing", "/guaranteed-minimum-pension/left-scheme"))
          }
        }

        "contain contributions and earnings" in {
          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession2)))
          when(mockCalculationConnector.calculateSingle(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(validCalculationWithContsAndEarningsResponse))
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          withAuthorisedUser { request =>
            val result = TestResultsController.getContributionsAndEarnings.apply(request)
            status(result) must equal(OK)
            contentAsString(result) must include(Messages("gmp.contracted_out_period_table_header", "10 11 2014 to 10 11 2015"))
            contentAsString(result) must include(Messages("gmp.tax_year_table_column_header"))
            contentAsString(result) must include(Messages("gmp.contracted_out_period_table_column_header"))
            contentAsString(result) must include("2014")
            contentAsString(result) must include("2015")
            contentAsString(result) must include("2016")
            contentAsString(result) must include("234.00")
            contentAsString(result) must include("124.00")
            contentAsString(result).replaceAll("&#x27;", "'") must include(Messages("gmp.queryhandling.contsandearnings"))
            contentAsString(result) must include(Messages("gmp.back.link"))
          }
        }

        "contain contributions and earnings with periods in error present" in {
          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession2)))
          when(mockCalculationConnector.calculateSingle(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(validCalculationWithContsAndEarningsErroredResponse))
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          withAuthorisedUser { request =>
            val result = TestResultsController.getContributionsAndEarnings.apply(request)
            status(result) must equal(OK)
            contentAsString(result) must include(Messages("gmp.error.no_conts_and_earnings"))
            contentAsString(result) must include(Messages("gmp.only_part_problem"))
            contentAsString(result) must include(Messages("gmp.contracted_out_period_error", "10/11/2014 to 10/11/2015"))
            contentAsString(result) must not include(Messages("gmp.back_to_dashboard"))
          }
        }

        "contain memeber details, print and get another calculation button" in {
          when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession)))
          when(mockCalculationConnector.calculateSingle(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(validCalculationWithContsAndEarningsResponse))
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          withAuthorisedUser { request =>
            val result = TestResultsController.getContributionsAndEarnings.apply(request)
            status(result) must equal(OK)
            contentAsString(result) must include(Messages("gmp.print"))
            contentAsString(result) must include(Messages("gmp.name"))
            contentAsString(result) must include(Messages("gmp.nino"))
            contentAsString(result) must include(Messages("gmp.scon.noabrv"))
            contentAsString(result) must include(Messages("gmp.queryhandling.contsandearnings"))
            contentAsString(result) must include(Messages("gmp.button.request-another"))
          }
        }
      }
    }

    "Asking for contributions and earnings" must {

      "set a flag in request model" in {

        withAuthorisedUser { request =>
          val result = TestResultsController.createCalculationRequest(gmpSession)
          result.requestEarnings must be(Some(1))
        }
      }
    }


    "Having left the scheme" must {
      "set dol in request model" in {

        withAuthorisedUser { request =>
          val result = TestResultsController.createCalculationRequest(gmpSession2.copy(leaving = Leaving(GmpDate(Some("1"), Some("1"), Some("2010")), leaving = Some("Yes"))))
          result.terminationDate must be(Some(new LocalDate(2010, 1, 1)))

        }
      }
    }

    "nino in session" must {
      "remove spaces when creating request" in {
        withAuthorisedUser { request =>
          val result = TestResultsController.createCalculationRequest(gmpSession4)
          result.nino must be(gmpSession4Nino)
        }
      }

      "create request with fixed rate" in {
        withAuthorisedUser { request =>
          val result = TestResultsController.createCalculationRequest(gmpSession.copy(rate = Some(RevaluationRate.FIXED)))
          result.revaluationRate must be(Some(2))
        }
      }

      "create request with limited rate" in {
        withAuthorisedUser { request =>
          val result = TestResultsController.createCalculationRequest(gmpSession.copy(rate = Some(RevaluationRate.LIMITED)))
          result.revaluationRate must be(Some(3))
        }
      }
    }

    "Contributions and earning link" must {

      "have the contributions and earnings link" in {
        when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession)))
        when(mockCalculationConnector.calculateSingle(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(validCalculationWithContsAndEarningsResponse))
        withAuthorisedUser { request =>
          val result = TestResultsController.get.apply(request)
          contentAsString(result) must include(Messages("gmp.link.contributions-earnings"))
        }
      }

      "have the contribution and earnings link when multi period and not all periods are in error" in {
        when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession)))
        when(mockCalculationConnector.calculateSingle(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(validRevalCalculationResponseMultiplePeriod))
        withAuthorisedUser { request =>
          val result = TestResultsController.get.apply(request)
          contentAsString(result) must include(Messages("gmp.link.contributions-earnings"))
        }
      }

      "do not have the contribution and earnings link when multi period and all periods are in error" in {
        when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession)))
        when(mockCalculationConnector.calculateSingle(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(validRevalCalculationResponseMultiplePeriodErrors))
        withAuthorisedUser { request =>
          val result = TestResultsController.get.apply(request)
          contentAsString(result) must not include(Messages("gmp.link.contributions-earnings"))
        }
      }
    }

    "DualCalc" must {
      "display dualcalc fields" in {
        when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession)))
        when(mockCalculationConnector.calculateSingle(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(dualCalcResponse))

        withAuthorisedUser { request =>
          val result = TestResultsController.get.apply(request)
          contentAsString(result) must include(Messages("gmp.true"))
          contentAsString(result) must include(Messages("gmp.opposite"))
        }
      }

      "display dualcalc fields when requested" in {
        when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession3)))
        when(mockCalculationConnector.calculateSingle(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(dualCalcResponse))

        withAuthorisedUser { request =>
          val result = TestResultsController.get.apply(request)
          contentAsString(result) must include(Messages("gmp.post90.true"))
          contentAsString(result) must include(Messages("gmp.post90.opposite"))
        }
      }

      "not display dualcalc fields when not requested" in {
        when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession2)))
        when(mockCalculationConnector.calculateSingle(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(nonDualCalcResponse))

        withAuthorisedUser { request =>
          val result = TestResultsController.get.apply(request)
          contentAsString(result) must not include (Messages("gmp.true_calculation"))
          contentAsString(result) must not include (Messages("gmp.opposite_calculation"))
          contentAsString(result) must not include (Messages("gmp.true"))
          contentAsString(result) must not include (Messages("gmp.opposite"))
        }
      }

      "display correct totals when dual calc" in {
        when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession2)))
        when(mockCalculationConnector.calculateSingle(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(dualCalcResponse))

        withAuthorisedUser { request =>
          val result = TestResultsController.get.apply(request)
          contentAsString(result) must include ("2.46")
          contentAsString(result) must include ("9.12")

        }
      }

      "display correct totals when dual calc with no total for period" in {
        when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpSession2)))
        when(mockCalculationConnector.calculateSingle(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(dualCalcResponse2))

        withAuthorisedUser { request =>
          val result = TestResultsController.get.apply(request)
          contentAsString(result) must include ("1.23")
          contentAsString(result) must include ("4.56")
          contentAsString(result) must include ("--")

        }
      }
    }

    "session missing info" must {
      "display error page when missing scon with correct back link" in {
        val emptySession = GmpSession(MemberDetails("", "", ""), "", "", None, None, Leaving(GmpDate(None, None, None), None), None)
        when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(emptySession)))

        withAuthorisedUser { request =>
          val result = TestResultsController.get.apply(request)
          contentAsString(result)replaceAll("&#x27;", "'") must include (Messages("gmp.cannot_calculate.gmp"))
          contentAsString(result) must include (Messages("gmp.error.session_parts_missing", "/guaranteed-minimum-pension/pension-details"))
        }
      }

      "display error page when missing nino with correct back link" in {
        val emptySession = GmpSession(MemberDetails("", "", ""), "S1234567T", "", None, None, Leaving(GmpDate(None, None, None), None), None)
        when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(emptySession)))

        withAuthorisedUser { request =>
          val result = TestResultsController.get.apply(request)
          contentAsString(result)replaceAll("&#x27;", "'") must include (Messages("gmp.cannot_calculate.gmp"))
          contentAsString(result) must include (Messages("gmp.error.session_parts_missing", "/guaranteed-minimum-pension/member-details"))
        }
      }

      "display error page when missing firstName with correct back link" in {
        val emptySession = GmpSession(MemberDetails(nino, "", ""), "S1234567T", "", None, None, Leaving(GmpDate(None, None, None), None), None)
        when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(emptySession)))

        withAuthorisedUser { request =>
          val result = TestResultsController.get.apply(request)
          contentAsString(result)replaceAll("&#x27;", "'") must include (Messages("gmp.cannot_calculate.gmp"))
          contentAsString(result) must include (Messages("gmp.error.session_parts_missing", "/guaranteed-minimum-pension/member-details"))
        }
      }

      "display error page when missing surname with correct back link" in {
        val emptySession = GmpSession(MemberDetails(nino, "A", ""), "S1234567T", "", None, None, Leaving(GmpDate(None, None, None), None), None)
        when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(emptySession)))

        withAuthorisedUser { request =>
          val result = TestResultsController.get.apply(request)
          contentAsString(result)replaceAll("&#x27;", "'") must include (Messages("gmp.cannot_calculate.gmp"))
          contentAsString(result) must include (Messages("gmp.error.session_parts_missing", "/guaranteed-minimum-pension/member-details"))
        }
      }

      "display error page when missing scenario with correct back link" in {
        val emptySession = GmpSession(MemberDetails(nino, "A", "AAA"), "S1234567T", "", None, None, Leaving(GmpDate(None, None, None), None), None)
        when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(emptySession)))

        withAuthorisedUser { request =>
          val result = TestResultsController.get.apply(request)
          contentAsString(result)replaceAll("&#x27;", "'") must include (Messages("gmp.cannot_calculate.gmp"))
          contentAsString(result) must include (Messages("gmp.error.session_parts_missing", "/guaranteed-minimum-pension/calculation-reason"))
        }
      }

      "display error page when missing leaving with correct back link" in {
        val emptySession = GmpSession(MemberDetails(nino, "A", "AAA"), "S1234567T", "0", None, None, Leaving(GmpDate(None, None, None), None), None)
        when(mockSessionService.fetchGmpSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(emptySession)))

        withAuthorisedUser { request =>
          val result = TestResultsController.get.apply(request)
          contentAsString(result)replaceAll("&#x27;", "'") must include (Messages("gmp.cannot_calculate.gmp"))
          contentAsString(result) must include (Messages("gmp.error.session_parts_missing", "/guaranteed-minimum-pension/left-scheme"))
        }
      }
    }

  }

}
