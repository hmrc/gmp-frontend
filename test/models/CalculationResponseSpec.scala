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

package models

import helpers.RandomNino
import java.time.LocalDate
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import org.scalatestplus.play.PlaySpec
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import views.helpers.GmpDateFormatter._

class CalculationResponseSpec extends PlaySpec with MockitoSugar with GuiceOneServerPerSuite {

  implicit val messagesAPI: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit val messagesProvider: MessagesImpl = MessagesImpl(Lang("en"), messagesAPI)

  val messages = messagesProvider.messages


  val nino = RandomNino.generate

  "Calculation Response model" must {

    "total GMP" must {
      "calculate correctly for multiple periods" in {
        val response = CalculationResponse("John Johnson", nino, "S1234567T", Some("1"), Some(LocalDate.of(2000, 11, 11)),
          List(CalculationPeriod(Some(LocalDate.of(2012, 1, 1)), LocalDate.of(2015, 1, 1), "1.11", "2.22", 1, 0, Some(1)),
            CalculationPeriod(Some(LocalDate.of(2010, 11, 10)), LocalDate.of(2011, 11, 10), "3.33", "4.44", 1, 0, Some(1))), 0, None, None, None, false, 1)
        response.totalGmp must be(4.44)
      }

      "calculate correctly for single period" in {
        val response = CalculationResponse("John Johnson", nino, "S1234567T", Some("1"), Some(LocalDate.of(2000, 11, 11)),
          List(CalculationPeriod(Some(LocalDate.of(2012, 1, 1)), LocalDate.of(2015, 1, 1), "1.11", "2.22", 1, 0, Some(1))), 0, None, None, None, false, 1)
        response.totalGmp must be(1.11)
      }
    }

    "post 1988 GMP" must {
      "calculate correctly for multiple periods" in {
        val response = CalculationResponse("John Johnson", nino, "S1234567T", Some("1"), Some(LocalDate.of(2000, 11, 11)),
          List(CalculationPeriod(Some(LocalDate.of(2012, 1, 1)), LocalDate.of(2015, 1, 1), "1.11", "2.22", 1, 0, Some(1)),
            CalculationPeriod(Some(LocalDate.of(2010, 11, 10)), LocalDate.of(2011, 11, 10), "3.33", "4.44", 1, 0, Some(1))), 0, None, None, None, false, 1)
        response.post88Gmp must be(6.66)
      }

      "calculate correctly for single period" in {
        val response = CalculationResponse("John Johnson", nino, "S1234567T", Some("1"), Some(LocalDate.of(2000, 11, 11)),
          List(CalculationPeriod(Some(LocalDate.of(2012, 1, 1)), LocalDate.of(2015, 1, 1), "1.11", "2.22", 1, 0, Some(1))), 0, None, None, None, false, 1)
        response.post88Gmp must be(2.22)
      }
    }

    "calculation rate" must {
      "return the rate supplied when S148" in {
        val response = CalculationResponse("John Johnson", nino, "S1234567T", Some("1"), Some(LocalDate.of(2000, 11, 11)),
          List(CalculationPeriod(Some(LocalDate.of(2012, 1, 1)), LocalDate.of(2015, 1, 1), "1.11", "2.22", 1, 0, Some(1)),
            CalculationPeriod(Some(LocalDate.of(2010, 11, 10)), LocalDate.of(2011, 11, 10), "3.33", "4.44", 1, 0, Some(1))), 0, None, None, None, false, 1)
        response.revaluationRate.get must be("1")
      }

      "return the rate supplied when HMRC" in {
        val response = CalculationResponse("John Johnson", nino, "S1234567T", Some("0"), Some(LocalDate.of(2000, 11, 11)),
          List(CalculationPeriod(Some(LocalDate.of(2012, 1, 1)), LocalDate.of(2015, 1, 1), "1.11", "2.22", 1, 0, Some(0)),
            CalculationPeriod(Some(LocalDate.of(2010, 11, 10)), LocalDate.of(2011, 11, 10), "3.33", "4.44", 1, 0, Some(0))), 0, None, None, None, false, 1)
        response.revaluationRate.get must be("0")
      }

      "return the rate supplied when Fixed" in {
        val response = CalculationResponse("John Johnson", nino, "S1234567T", Some("2"), Some(LocalDate.of(2000, 11, 11)),
          List(CalculationPeriod(Some(LocalDate.of(2012, 1, 1)), LocalDate.of(2015, 1, 1), "1.11", "2.22", 1, 0, Some(1)),
            CalculationPeriod(Some(LocalDate.of(2010, 11, 10)), LocalDate.of(2011, 11, 10), "3.33", "4.44", 1, 0, Some(1))), 0, None, None, None, false, 1)
        response.revaluationRate.get must be("2")
      }

      "return the rate supplied when Limited" in {
        val response = CalculationResponse("John Johnson", nino, "S1234567T", Some("3"), Some(LocalDate.of(2000, 11, 11)),
          List(CalculationPeriod(Some(LocalDate.of(2012, 1, 1)), LocalDate.of(2015, 1, 1), "1.11", "2.22", 1, 0, Some(1)),
            CalculationPeriod(Some(LocalDate.of(2010, 11, 10)), LocalDate.of(2011, 11, 10), "3.33", "4.44", 1, 0, Some(1))), 0, None, None, None, false, 1)
        response.revaluationRate.get must be("3")
      }

      "return the first rate when HMRC supplied and single" in {
        val response = CalculationResponse("John Johnson", nino, "S1234567T", Some("0"), Some(LocalDate.of(2000, 11, 11)),
          List(CalculationPeriod(Some(LocalDate.of(2012, 1, 1)), LocalDate.of(2015, 1, 1), "1.11", "2.22", 1, 0, Some(1))), 0, None, None, None, false, 1)
        response.revaluationRate.get must be("0")
      }
    }

    "leaving date" must {

      "return the correctly formatted date for multiple periods" in {
        val response = CalculationResponse("John Johnson", nino, "S1234567T", None, None,
          List(CalculationPeriod(Some(LocalDate.of(2012, 1, 1)), LocalDate.of(2015, 1, 1), "1.11", "2.22", 1, 0, Some(1)),
            CalculationPeriod(Some(LocalDate.of(2010, 11, 10)), LocalDate.of(2011, 11, 10), "1.11", "2.22", 1, 0, Some(1))), 0, None, None, None, false, 1)
        response.leavingDate must be(LocalDate.of(2015, 1, 1))
      }

      "return the correctly formatted date for single period" in {
        val response = CalculationResponse("John Johnson", nino, "S1234567T", None, None,
          List(CalculationPeriod(Some(LocalDate.of(2010, 11, 10)), LocalDate.of(2011, 11, 10), "1.11", "2.22", 1, 0, Some(1))), 0, None, None, None, false, 1)
        response.leavingDate must be(LocalDate.of(2011, 11, 10))
      }

      "return the revaluation date when supplied" in {
        val response = CalculationResponse("John Johnson", nino, "S1234567T", Some("1"), Some(LocalDate.of(2000, 11, 11)),
          List(CalculationPeriod(Some(LocalDate.of(2010, 11, 10)), LocalDate.of(2011, 11, 10), "1.11", "2.22", 1, 0, Some(1))), 0, None, None, None, false, 1)
        response.leavingDate must be(LocalDate.of(2000, 11, 11))
      }
    }

    "has errors" must {
      "return true when global error" in {
        val response = CalculationResponse("John Johnson", nino, "S1234567T", None, None, Nil, 56010, None, None, None, false, 1)
        response.hasErrors must be(true)
      }

      "return false when no cop errorsr" in {
        val response = CalculationResponse("John Johnson", nino, "S1234567T", Some("1"), Some(LocalDate.of(2000, 11, 11)),
          List(CalculationPeriod(Some(LocalDate.of(2015, 11, 10)), LocalDate.of(2015, 11, 10), "1.11", "2.22", 1, 0, Some(1)),
            CalculationPeriod(Some(LocalDate.of(2015, 11, 10)), LocalDate.of(2015, 11, 10), "1.11", "2.22", 1, 0, Some(1))), 0, None, None, None, false, 1)
        response.hasErrors must be(false)
      }

      "return true when one cop error" in {
        val response = CalculationResponse("John Johnson", nino, "S1234567T", Some("1"), Some(LocalDate.of(2000, 11, 11)),
          List(CalculationPeriod(Some(LocalDate.of(2015, 11, 10)), LocalDate.of(2015, 11, 10), "1.11", "2.22", 1, 0, Some(1)),
            CalculationPeriod(Some(LocalDate.of(2015, 11, 10)), LocalDate.of(2015, 11, 10), "1.11", "2.22", 1, 6666, None)), 0, None, None, None, false, 1)
        response.hasErrors must be(true)
      }

      "return true when multi cop error" in {
        val response = CalculationResponse("John Johnson", nino, "S1234567T", None, Some(LocalDate.of(2000, 11, 11)), List(CalculationPeriod(Some(LocalDate.of(2015, 11, 10)),
          LocalDate.of(2015, 11, 10), "0.00", "0.00", 0, 56023, None), CalculationPeriod(Some(LocalDate.of(2010, 11, 10)),
          LocalDate.of(2011, 11, 10), "0.00", "0.00", 0, 56007, None)), 0, None, None, None, false, 1)
        response.hasErrors must be(true)
      }
    }

    "hasSuccessfulCalculations" must {

      "return true when global error = 0" in {
        val response = CalculationResponse("David Dickinson", nino, "S1234567T", None, None, Nil, 0, None, None, None, false, 1)
        response.hasSuccessfulCalculations must be(true)
      }

      "return true when there is at least one successful period" in {
        val periods = List(
          CalculationPeriod(Some(LocalDate.of(2015, 11, 10)), LocalDate.of(2015, 11, 10), "1.11", "2.22", 1, 10, Some(1)),
          CalculationPeriod(Some(LocalDate.of(2015, 11, 10)), LocalDate.of(2015, 11, 10), "1.11", "2.22", 1, 0, Some(1))
        )

        val response = CalculationResponse("David Dickinson", nino, "S1234567T", None, None, periods, 1, None, None, None, false, 1)

        response.hasSuccessfulCalculations must be(true)
      }

      "return false if all periods return an error code" in {
        val periods = List(
          CalculationPeriod(Some(LocalDate.of(2015, 11, 10)), LocalDate.of(2015, 11, 10), "1.11", "2.22", 1, 10, Some(1)),
          CalculationPeriod(Some(LocalDate.of(2015, 11, 10)), LocalDate.of(2015, 11, 10), "1.11", "2.22", 1, 13, Some(1))
        )

        val response = CalculationResponse("David Dickinson", nino, "S1234567T", None, None, periods, 1, None, None, None, false, 1)

        response.hasSuccessfulCalculations must be(false)
      }

    }

    "calculationUnsuccessful" must {

      "return false when no errors" in {
        val response = CalculationResponse("John Johnson", nino, "S1234567T", Some("1"), Some(LocalDate.of(2000, 11, 11)),
          List(CalculationPeriod(Some(LocalDate.of(2015, 11, 10)), LocalDate.of(2015, 11, 10), "1.11", "2.22", 1, 0, Some(0)),
            CalculationPeriod(Some(LocalDate.of(2015, 11, 10)), LocalDate.of(2015, 11, 10), "1.11", "2.22", 1, 0, Some(0))), 0, None, None, None, false, 1)
        response.revaluationUnsuccessful must be(false)
      }

      "return false when not all in error" in {
        val response = CalculationResponse("John Johnson", nino, "S1234567T", Some("1"), Some(LocalDate.of(2000, 11, 11)),
          List(CalculationPeriod(Some(LocalDate.of(2015, 11, 10)), LocalDate.of(2015, 11, 10), "1.11", "2.22", 1, 0, Some(0)),
            CalculationPeriod(Some(LocalDate.of(2015, 11, 10)), LocalDate.of(2015, 11, 10), "1.11", "2.22", 1, 0, Some(1))), 0, None, None, None, false, 1)
        response.revaluationUnsuccessful must be(false)
      }

      "return true when all in error" in {
        val response = CalculationResponse("John Johnson", nino, "S1234567T", Some("1"), Some(LocalDate.of(2000, 11, 11)),
          List(CalculationPeriod(Some(LocalDate.of(2015, 11, 10)), LocalDate.of(2015, 11, 10), "1.11", "2.22", 1, 0, Some(1)),
            CalculationPeriod(Some(LocalDate.of(2015, 11, 10)), LocalDate.of(2015, 11, 10), "1.11", "2.22", 1, 0, Some(1))), 0, None, None, None, false, 1)
        response.revaluationUnsuccessful must be(true)
      }
    }

    "TaxYear" must {
      "return correct tax year" in {
        val period = CalculationPeriod(Some(LocalDate.of(2003, 7, 10)), LocalDate.of(2015, 4, 5), "1.11", "2.22", 1, 0, Some(0))
        period.startTaxYear must be(2003)
        period.endTaxYear must be(2014)
      }

      "return 0 when tax year cannot be determined" in {
        val period = CalculationPeriod(None, LocalDate.of(2015, 4, 5), "1.11", "2.22", 1, 0, Some(0))
        period.startTaxYear must be (0)
      }
    }

    "errorCodes" must {
      "return an empty list when no error codes" in {
        val response = CalculationResponse("John Johnson", nino, "S1234567T", Some("1"), Some(LocalDate.of(2000, 11, 11)),
          List(CalculationPeriod(Some(LocalDate.of(2012, 1, 1)), LocalDate.of(2015, 1, 1), "1.11", "2.22", 1, 0, Some(1))), 0, None, None, None, false, 1)
        response.errorCodes.size must be(0)
      }

      "return a list of error codes with global error code" in {
        val response = CalculationResponse("John Johnson", nino, "S1234567T", Some("1"), Some(LocalDate.of(2000, 11, 11)),
          List(CalculationPeriod(Some(LocalDate.of(2015, 11, 10)),LocalDate.of(2015, 11, 10), "0.00", "0.00", 0, 0, None)), 48160, None, None, None, false, 1)
        response.errorCodes.size must be(1)
        response.errorCodes.head must be(48160)
      }

      "return a list of error codes with period error codes" in {
        val response = CalculationResponse("John Johnson", nino, "S1234567T", None, Some(LocalDate.of(2000, 11, 11)),
          List(CalculationPeriod(Some(LocalDate.of(2015, 11, 10)),LocalDate.of(2015, 11, 10), "0.00", "0.00", 0, 56023, None),
               CalculationPeriod(Some(LocalDate.of(2010, 11, 10)),LocalDate.of(2011, 11, 10), "0.00", "0.00", 0, 56007, None),
               CalculationPeriod(Some(LocalDate.of(2010, 11, 10)),LocalDate.of(2011, 11, 10), "0.00", "0.00", 0, 0, None)), 0, None, None, None, false, 1)
        response.errorCodes.size must be(2)
        response.errorCodes must be(List(56023, 56007))
      }

      "return a list of error codes with period error codes and global error code" in {
        val response = CalculationResponse("John Johnson", nino, "S1234567T", None, Some(LocalDate.of(2000, 11, 11)),
          List(CalculationPeriod(Some(LocalDate.of(2015, 11, 10)),LocalDate.of(2015, 11, 10), "0.00", "0.00", 0, 56023, None),
            CalculationPeriod(Some(LocalDate.of(2010, 11, 10)),LocalDate.of(2011, 11, 10), "0.00", "0.00", 0, 56007, None),
            CalculationPeriod(Some(LocalDate.of(2010, 11, 10)),LocalDate.of(2011, 11, 10), "0.00", "0.00", 0, 0, None)), 48160, None, None, None, false, 1)
        response.errorCodes.size must be(3)
        response.errorCodes must be(List(56023, 56007, 48160))
      }
    }

    "numPeriodsInError" must {
      "return 0 when no periods in error" in  {
        val response = CalculationResponse("John Johnson", nino, "S1234567T", Some("1"), Some(LocalDate.of(2000, 11, 11)),
          List(CalculationPeriod(Some(LocalDate.of(2012, 1, 1)), LocalDate.of(2015, 1, 1), "1.11", "2.22", 1, 0, Some(1))), 0, None, None, None, false, 1)
        response.numPeriodsInError must be(0)
      }

      "return 0 when no periods in error but there is a global error" in  {
        val response = CalculationResponse("John Johnson", nino, "S1234567T", Some("1"), Some(LocalDate.of(2000, 11, 11)),
          List(CalculationPeriod(Some(LocalDate.of(2012, 1, 1)), LocalDate.of(2015, 1, 1), "1.11", "2.22", 1, 0, Some(1))), 48160, None, None, None, false, 1)
        response.numPeriodsInError must be(0)
      }

      "return 2 when periods in error" in  {
        val response = CalculationResponse("John Johnson", nino, "S1234567T", None, Some(LocalDate.of(2000, 11, 11)),
          List(CalculationPeriod(Some(LocalDate.of(2015, 11, 10)),LocalDate.of(2015, 11, 10), "0.00", "0.00", 0, 56023, None),
            CalculationPeriod(Some(LocalDate.of(2010, 11, 10)),LocalDate.of(2011, 11, 10), "0.00", "0.00", 0, 56007, None),
            CalculationPeriod(Some(LocalDate.of(2010, 11, 10)),LocalDate.of(2011, 11, 10), "0.00", "0.00", 0, 0, None)), 0, None, None, None, false, 1)
        response.numPeriodsInError must be(2)
      }
    }

    "header" must {
      "return correct header for survivor and revaluation" in {
        val revalDate = LocalDate.of(2010, 1, 1)
        val response = CalculationResponse("John Johnson", nino, "S1234567T", None, Some(revalDate), Nil, 0, None, None, None, false, 3)
        response.header(messages) must be(Messages("gmp.results.survivor.revaluation.header", formatDate(revalDate)))
      }

      "return correct header for survivor and date of death" in {
        val dod = LocalDate.of(2010, 1, 1)
        val response = CalculationResponse("John Johnson", nino, "S1234567T", None, None, Nil, 0, None, None, Some(dod), false, 3)
        response.header(messages) must be(Messages("gmp.results.survivor.header", formatDate(dod)))
      }

      "return correct header for survivor" in {
        val response = CalculationResponse("John Johnson", nino, "S1234567T", None, None, Nil, 0, None, None, None, false, 3)
        response.header(messages) must be(Messages("gmp.results.survivor.header"))
      }

      "return correct header for spa" in {
        val spaDate = LocalDate.of(2010, 1, 1)
        val response = CalculationResponse("John Johnson", nino, "S1234567T", None, None, Nil, 0, Some(spaDate), None, None, false, 4)
        response.header(messages) must be(Messages("gmp.spa.header", formatDate(spaDate)))
      }

      "return correct header for pa" in {
        val paDate = LocalDate.of(2010, 1, 1)
        val response = CalculationResponse("John Johnson", nino, "S1234567T", None, None, Nil, 0, None, Some(paDate), None, false, 2)
        response.header(messages) must be(Messages("gmp.payable_age.header", formatDate(paDate)))
      }

      "return correct header with no revaluation" in {
        val response = CalculationResponse("John Johnson", nino, "S1234567T", None, None, List(CalculationPeriod(Some(LocalDate.of(2015, 11, 10)),LocalDate.of(2015, 11, 10), "0.00", "0.00", 0, 0, None)), 0, None, None, None, false, 1)
        response.header(messages) must be(Messages("gmp.leaving.scheme.header", formatDate(LocalDate.of(2015, 11, 10))))
      }

      "return correct header with with revaluation date but revaluation unsuccessful" in {
        val revalDate = LocalDate.of(2010, 1, 1)
        val response = CalculationResponse("John Johnson", nino, "S1234567T", None, Some(revalDate), List(CalculationPeriod(Some(LocalDate.of(2015, 11, 10)),LocalDate.of(2015, 11, 10), "0.00", "0.00", 0, 0, Some(1))), 0, None, None, None, false, 1)
        response.header(messages) must be(Messages("gmp.leaving.scheme.header", formatDate(revalDate)))
      }

      "return correct header for revaluation" in {
        val revalDate = LocalDate.of(2010, 1, 1)
        val response = CalculationResponse("John Johnson", nino, "S1234567T", None, Some(revalDate), List(CalculationPeriod(Some(LocalDate.of(2015, 11, 10)),LocalDate.of(2015, 11, 10), "0.00", "0.00", 0, 0, None)), 0, None, None, None, false, 1)
        response.header(messages) must be(Messages("gmp.leaving.revalued.header", formatDate(revalDate)))
      }

    }

  }

}
