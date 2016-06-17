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

package models

import helpers.RandomNino
import org.joda.time.LocalDate
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.i18n.Messages
import views.helpers.GmpDateFormatter._

class CalculationResponseSpec extends PlaySpec with MockitoSugar {

  val nino = RandomNino.generate

  "Calculation Response model" must {

    "total GMP" must {
      "calculate correctly for multiple periods" in {
        val response = CalculationResponse("John Johnson", nino, "S1234567T", Some("1"), Some(new LocalDate(2000, 11, 11)),
          List(CalculationPeriod(Some(new LocalDate(2012, 1, 1)), new LocalDate(2015, 1, 1), "1.11", "2.22", 1, 0, Some(1)),
            CalculationPeriod(Some(new LocalDate(2010, 11, 10)), new LocalDate(2011, 11, 10), "3.33", "4.44", 1, 0, Some(1))), 0, None, None, None, false, 1)
        response.totalGmp must be(4.44)
      }

      "calculate correctly for single period" in {
        val response = CalculationResponse("John Johnson", nino, "S1234567T", Some("1"), Some(new LocalDate(2000, 11, 11)),
          List(CalculationPeriod(Some(new LocalDate(2012, 1, 1)), new LocalDate(2015, 1, 1), "1.11", "2.22", 1, 0, Some(1))), 0, None, None, None, false, 1)
        response.totalGmp must be(1.11)
      }
    }

    "post 1988 GMP" must {
      "calculate correctly for multiple periods" in {
        val response = CalculationResponse("John Johnson", nino, "S1234567T", Some("1"), Some(new LocalDate(2000, 11, 11)),
          List(CalculationPeriod(Some(new LocalDate(2012, 1, 1)), new LocalDate(2015, 1, 1), "1.11", "2.22", 1, 0, Some(1)),
            CalculationPeriod(Some(new LocalDate(2010, 11, 10)), new LocalDate(2011, 11, 10), "3.33", "4.44", 1, 0, Some(1))), 0, None, None, None, false, 1)
        response.post88Gmp must be(6.66)
      }

      "calculate correctly for single period" in {
        val response = CalculationResponse("John Johnson", nino, "S1234567T", Some("1"), Some(new LocalDate(2000, 11, 11)),
          List(CalculationPeriod(Some(new LocalDate(2012, 1, 1)), new LocalDate(2015, 1, 1), "1.11", "2.22", 1, 0, Some(1))), 0, None, None, None, false, 1)
        response.post88Gmp must be(2.22)
      }
    }

    "calculation rate" must {
      "return the rate supplied when S148" in {
        val response = CalculationResponse("John Johnson", nino, "S1234567T", Some("1"), Some(new LocalDate(2000, 11, 11)),
          List(CalculationPeriod(Some(new LocalDate(2012, 1, 1)), new LocalDate(2015, 1, 1), "1.11", "2.22", 1, 0, Some(1)),
            CalculationPeriod(Some(new LocalDate(2010, 11, 10)), new LocalDate(2011, 11, 10), "3.33", "4.44", 1, 0, Some(1))), 0, None, None, None, false, 1)
        response.revaluationRate.get must be("1")
      }

      "return the rate supplied when HMRC" in {
        val response = CalculationResponse("John Johnson", nino, "S1234567T", Some("0"), Some(new LocalDate(2000, 11, 11)),
          List(CalculationPeriod(Some(new LocalDate(2012, 1, 1)), new LocalDate(2015, 1, 1), "1.11", "2.22", 1, 0, Some(0)),
            CalculationPeriod(Some(new LocalDate(2010, 11, 10)), new LocalDate(2011, 11, 10), "3.33", "4.44", 1, 0, Some(0))), 0, None, None, None, false, 1)
        response.revaluationRate.get must be("0")
      }

      "return the rate supplied when Fixed" in {
        val response = CalculationResponse("John Johnson", nino, "S1234567T", Some("2"), Some(new LocalDate(2000, 11, 11)),
          List(CalculationPeriod(Some(new LocalDate(2012, 1, 1)), new LocalDate(2015, 1, 1), "1.11", "2.22", 1, 0, Some(1)),
            CalculationPeriod(Some(new LocalDate(2010, 11, 10)), new LocalDate(2011, 11, 10), "3.33", "4.44", 1, 0, Some(1))), 0, None, None, None, false, 1)
        response.revaluationRate.get must be("2")
      }

      "return the rate supplied when Limited" in {
        val response = CalculationResponse("John Johnson", nino, "S1234567T", Some("3"), Some(new LocalDate(2000, 11, 11)),
          List(CalculationPeriod(Some(new LocalDate(2012, 1, 1)), new LocalDate(2015, 1, 1), "1.11", "2.22", 1, 0, Some(1)),
            CalculationPeriod(Some(new LocalDate(2010, 11, 10)), new LocalDate(2011, 11, 10), "3.33", "4.44", 1, 0, Some(1))), 0, None, None, None, false, 1)
        response.revaluationRate.get must be("3")
      }

      "return the first rate when HMRC supplied and single" in {
        val response = CalculationResponse("John Johnson", nino, "S1234567T", Some("0"), Some(new LocalDate(2000, 11, 11)),
          List(CalculationPeriod(Some(new LocalDate(2012, 1, 1)), new LocalDate(2015, 1, 1), "1.11", "2.22", 1, 0, Some(1))), 0, None, None, None, false, 1)
        response.revaluationRate.get must be("0")
      }
    }

    "leaving date" must {

      "return the correctly formatted date for multiple periods" in {
        val response = CalculationResponse("John Johnson", nino, "S1234567T", None, None,
          List(CalculationPeriod(Some(new LocalDate(2012, 1, 1)), new LocalDate(2015, 1, 1), "1.11", "2.22", 1, 0, Some(1)),
            CalculationPeriod(Some(new LocalDate(2010, 11, 10)), new LocalDate(2011, 11, 10), "1.11", "2.22", 1, 0, Some(1))), 0, None, None, None, false, 1)
        response.leavingDate must be(new LocalDate(2015, 1, 1))
      }

      "return the correctly formatted date for single period" in {
        val response = CalculationResponse("John Johnson", nino, "S1234567T", None, None,
          List(CalculationPeriod(Some(new LocalDate(2010, 11, 10)), new LocalDate(2011, 11, 10), "1.11", "2.22", 1, 0, Some(1))), 0, None, None, None, false, 1)
        response.leavingDate must be(new LocalDate(2011, 11, 10))
      }

      "return the revaluation date when supplied" in {
        val response = CalculationResponse("John Johnson", nino, "S1234567T", Some("1"), Some(new LocalDate(2000, 11, 11)),
          List(CalculationPeriod(Some(new LocalDate(2010, 11, 10)), new LocalDate(2011, 11, 10), "1.11", "2.22", 1, 0, Some(1))), 0, None, None, None, false, 1)
        response.leavingDate must be(new LocalDate(2000, 11, 11))
      }
    }

    "has errors" must {
      "return true when global error" in {
        val response = CalculationResponse("John Johnson", nino, "S1234567T", None, None, Nil, 56010, None, None, None, false, 1)
        response.hasErrors must be(true)
      }

      "return false when no cop errorsr" in {
        val response = CalculationResponse("John Johnson", nino, "S1234567T", Some("1"), Some(new LocalDate(2000, 11, 11)),
          List(CalculationPeriod(Some(new LocalDate(2015, 11, 10)), new LocalDate(2015, 11, 10), "1.11", "2.22", 1, 0, Some(1)),
            CalculationPeriod(Some(new LocalDate(2015, 11, 10)), new LocalDate(2015, 11, 10), "1.11", "2.22", 1, 0, Some(1))), 0, None, None, None, false, 1)
        response.hasErrors must be(false)
      }

      "return true when one cop error" in {
        val response = CalculationResponse("John Johnson", nino, "S1234567T", Some("1"), Some(new LocalDate(2000, 11, 11)),
          List(CalculationPeriod(Some(new LocalDate(2015, 11, 10)), new LocalDate(2015, 11, 10), "1.11", "2.22", 1, 0, Some(1)),
            CalculationPeriod(Some(new LocalDate(2015, 11, 10)), new LocalDate(2015, 11, 10), "1.11", "2.22", 1, 6666, None)), 0, None, None, None, false, 1)
        response.hasErrors must be(true)
      }

      "return true when multi cop error" in {
        val response = CalculationResponse("John Johnson", nino, "S1234567T", None, Some(new LocalDate(2000, 11, 11)), List(CalculationPeriod(Some(new LocalDate(2015, 11, 10)),
          new LocalDate(2015, 11, 10), "0.00", "0.00", 0, 56023, None), CalculationPeriod(Some(new LocalDate(2010, 11, 10)),
          new LocalDate(2011, 11, 10), "0.00", "0.00", 0, 56007, None)), 0, None, None, None, false, 1)
        response.hasErrors must be(true)
      }
    }

    "calculationUnsuccessful" must {

      "return false when no errors" in {
        val response = CalculationResponse("John Johnson", nino, "S1234567T", Some("1"), Some(new LocalDate(2000, 11, 11)),
          List(CalculationPeriod(Some(new LocalDate(2015, 11, 10)), new LocalDate(2015, 11, 10), "1.11", "2.22", 1, 0, Some(0)),
            CalculationPeriod(Some(new LocalDate(2015, 11, 10)), new LocalDate(2015, 11, 10), "1.11", "2.22", 1, 0, Some(0))), 0, None, None, None, false, 1)
        response.revaluationUnsuccessful must be(false)
      }

      "return false when not all in error" in {
        val response = CalculationResponse("John Johnson", nino, "S1234567T", Some("1"), Some(new LocalDate(2000, 11, 11)),
          List(CalculationPeriod(Some(new LocalDate(2015, 11, 10)), new LocalDate(2015, 11, 10), "1.11", "2.22", 1, 0, Some(0)),
            CalculationPeriod(Some(new LocalDate(2015, 11, 10)), new LocalDate(2015, 11, 10), "1.11", "2.22", 1, 0, Some(1))), 0, None, None, None, false, 1)
        response.revaluationUnsuccessful must be(false)
      }

      "return true when all in error" in {
        val response = CalculationResponse("John Johnson", nino, "S1234567T", Some("1"), Some(new LocalDate(2000, 11, 11)),
          List(CalculationPeriod(Some(new LocalDate(2015, 11, 10)), new LocalDate(2015, 11, 10), "1.11", "2.22", 1, 0, Some(1)),
            CalculationPeriod(Some(new LocalDate(2015, 11, 10)), new LocalDate(2015, 11, 10), "1.11", "2.22", 1, 0, Some(1))), 0, None, None, None, false, 1)
        response.revaluationUnsuccessful must be(true)
      }
    }

    "TaxYear" must {
      "return correct tax year" in {
        val period = CalculationPeriod(Some(new LocalDate(2003, 7, 10)), new LocalDate(2015, 4, 5), "1.11", "2.22", 1, 0, Some(0))
        period.startTaxYear must be(2003)
        period.endTaxYear must be(2014)
      }

      "return 0 when tax year cannot be determined" in {
        val period = CalculationPeriod(None, new LocalDate(2015, 4, 5), "1.11", "2.22", 1, 0, Some(0))
        period.startTaxYear must be (0)
      }
    }

    "errorCodes" must {
      "return an empty list when no error codes" in {
        val response = CalculationResponse("John Johnson", nino, "S1234567T", Some("1"), Some(new LocalDate(2000, 11, 11)),
          List(CalculationPeriod(Some(new LocalDate(2012, 1, 1)), new LocalDate(2015, 1, 1), "1.11", "2.22", 1, 0, Some(1))), 0, None, None, None, false, 1)
        response.errorCodes.size must be(0)
      }

      "return a list of error codes with global error code" in {
        val response = CalculationResponse("John Johnson", nino, "S1234567T", Some("1"), Some(new LocalDate(2000, 11, 11)),
          List(CalculationPeriod(Some(new LocalDate(2015, 11, 10)),new LocalDate(2015, 11, 10), "0.00", "0.00", 0, 0, None)), 48160, None, None, None, false, 1)
        response.errorCodes.size must be(1)
        response.errorCodes.head must be(48160)
      }

      "return a list of error codes with period error codes" in {
        val response = CalculationResponse("John Johnson", nino, "S1234567T", None, Some(new LocalDate(2000, 11, 11)),
          List(CalculationPeriod(Some(new LocalDate(2015, 11, 10)),new LocalDate(2015, 11, 10), "0.00", "0.00", 0, 56023, None),
               CalculationPeriod(Some(new LocalDate(2010, 11, 10)),new LocalDate(2011, 11, 10), "0.00", "0.00", 0, 56007, None),
               CalculationPeriod(Some(new LocalDate(2010, 11, 10)),new LocalDate(2011, 11, 10), "0.00", "0.00", 0, 0, None)), 0, None, None, None, false, 1)
        response.errorCodes.size must be(2)
        response.errorCodes must be(List(56023, 56007))
      }

      "return a list of error codes with period error codes and global error code" in {
        val response = CalculationResponse("John Johnson", nino, "S1234567T", None, Some(new LocalDate(2000, 11, 11)),
          List(CalculationPeriod(Some(new LocalDate(2015, 11, 10)),new LocalDate(2015, 11, 10), "0.00", "0.00", 0, 56023, None),
            CalculationPeriod(Some(new LocalDate(2010, 11, 10)),new LocalDate(2011, 11, 10), "0.00", "0.00", 0, 56007, None),
            CalculationPeriod(Some(new LocalDate(2010, 11, 10)),new LocalDate(2011, 11, 10), "0.00", "0.00", 0, 0, None)), 48160, None, None, None, false, 1)
        response.errorCodes.size must be(3)
        response.errorCodes must be(List(56023, 56007, 48160))
      }
    }

    "numPeriodsInError" must {
      "return 0 when no periods in error" in  {
        val response = CalculationResponse("John Johnson", nino, "S1234567T", Some("1"), Some(new LocalDate(2000, 11, 11)),
          List(CalculationPeriod(Some(new LocalDate(2012, 1, 1)), new LocalDate(2015, 1, 1), "1.11", "2.22", 1, 0, Some(1))), 0, None, None, None, false, 1)
        response.numPeriodsInError must be(0)
      }

      "return 0 when no periods in error but there is a global error" in  {
        val response = CalculationResponse("John Johnson", nino, "S1234567T", Some("1"), Some(new LocalDate(2000, 11, 11)),
          List(CalculationPeriod(Some(new LocalDate(2012, 1, 1)), new LocalDate(2015, 1, 1), "1.11", "2.22", 1, 0, Some(1))), 48160, None, None, None, false, 1)
        response.numPeriodsInError must be(0)
      }

      "return 2 when periods in error" in  {
        val response = CalculationResponse("John Johnson", nino, "S1234567T", None, Some(new LocalDate(2000, 11, 11)),
          List(CalculationPeriod(Some(new LocalDate(2015, 11, 10)),new LocalDate(2015, 11, 10), "0.00", "0.00", 0, 56023, None),
            CalculationPeriod(Some(new LocalDate(2010, 11, 10)),new LocalDate(2011, 11, 10), "0.00", "0.00", 0, 56007, None),
            CalculationPeriod(Some(new LocalDate(2010, 11, 10)),new LocalDate(2011, 11, 10), "0.00", "0.00", 0, 0, None)), 0, None, None, None, false, 1)
        response.numPeriodsInError must be(2)
      }
    }

    "header" must {
      "return correct header for survivor and revaluation" in {
        val revalDate = new LocalDate(2010, 1, 1)
        val response = CalculationResponse("John Johnson", nino, "S1234567T", None, Some(revalDate), Nil, 0, None, None, None, false, 3)
        response.header must be(Messages("gmp.results.survivor.revaluation.header", formatDate(revalDate)))
      }

      "return correct header for survivor and date of death" in {
        val dod = new LocalDate(2010, 1, 1)
        val response = CalculationResponse("John Johnson", nino, "S1234567T", None, None, Nil, 0, None, None, Some(dod), false, 3)
        response.header must be(Messages("gmp.results.survivor.header", formatDate(dod)))
      }

      "return correct header for survivor" in {
        val response = CalculationResponse("John Johnson", nino, "S1234567T", None, None, Nil, 0, None, None, None, false, 3)
        response.header must be(Messages("gmp.results.survivor.header"))
      }

      "return correct header for spa" in {
        val spaDate = new LocalDate(2010, 1, 1)
        val response = CalculationResponse("John Johnson", nino, "S1234567T", None, None, Nil, 0, Some(spaDate), None, None, false, 4)
        response.header must be(Messages("gmp.spa.header", formatDate(spaDate)))
      }



    }

  }

}
