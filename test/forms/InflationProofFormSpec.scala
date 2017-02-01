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

package forms

import forms.InflationProofForm._
import models.{GmpDate, InflationProof}
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.data.FormError
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.i18n.Messages.Implicits._

class inflationProofFormSpec extends PlaySpec with OneAppPerSuite {

  val inflationProofDate = GmpDate(Some("01"), Some("02"), Some("2010"))

  "InflationProof Form" must {
    "return no errors when valid values are entered" in {

      val inflationProofFormResults = inflationProofForm.bind(Json.toJson(InflationProof(inflationProofDate,Some("Yes"))))

      assert(inflationProofFormResults.errors.size == 0)

    }

    "return no errors when inlfation proofing not wanted" in {

      val inflationProofFormResults = inflationProofForm.bind(Json.toJson(InflationProof(inflationProofDate.copy(None,None,None),Some("No"))))

      assert(inflationProofFormResults.errors.size == 0)

    }

    "return errors when inlfation proofing requested but date not entered" in {

      val inflationProofFormResults = inflationProofForm.bind(Json.toJson(InflationProof(inflationProofDate.copy(None,None,None),Some("Yes"))))

      assert(inflationProofFormResults.errors.size == 1)

    }

    "return errors when InflationProof yes/no not entered" in {

      val inflationProofFormResults = inflationProofForm.bind(Json.toJson(InflationProof(inflationProofDate.copy(None,None,None),None)))

      assert(inflationProofFormResults.errors.size == 1)

    }

    "entering a day" must {

      "return an error on the date when it is not a number" in {
        val inflationProofFormResults = inflationProofForm.bind(Json.toJson(InflationProof(new GmpDate(Some("a"), Some("01"), Some("2012")),Some("Yes"))))
        inflationProofFormResults.errors must contain(FormError("revaluationDate", List(Messages("gmp.error.date.nonnumber"))))
      }

      "return an error on the date when it is out of range" in {
        val inflationProofFormResults = inflationProofForm.bind(Json.toJson(InflationProof(new GmpDate(Some("32"), Some("01"), Some("2012")),Some("Yes"))))
        inflationProofFormResults.errors must contain(FormError("revaluationDate", List(Messages("gmp.error.day.invalid"))))
      }
    }

    "entering a month" must {

      "return an error on the date when it is not a number" in {
        val inflationProofFormResults = inflationProofForm.bind(Json.toJson(InflationProof(new GmpDate(Some("01"), Some("a"), Some("2012")),Some("Yes"))))
        inflationProofFormResults.errors must contain(FormError("revaluationDate", List(Messages("gmp.error.date.nonnumber"))))
      }

      "return an error on the date when it is out of range" in {
        val inflationProofFormResults = inflationProofForm.bind(Json.toJson(InflationProof(new GmpDate(Some("01"), Some("13"), Some("2012")),Some("Yes"))))
        inflationProofFormResults.errors must contain(FormError("revaluationDate", List(Messages("gmp.error.month.invalid"))))
      }
    }

    "entering a year" must {

      "return an error on the date when it is not a number" in {
        val inflationProofFormResults = inflationProofForm.bind(Json.toJson(InflationProof(new GmpDate(Some("01"), Some("12"), Some("21a1")),Some("Yes"))))
        inflationProofFormResults.errors must contain(FormError("revaluationDate", List(Messages("gmp.error.date.nonnumber"))))
      }

      "return an error on the date when it is not the correct format" in {
        val inflationProofFormResults = inflationProofForm.bind(Json.toJson(InflationProof(new GmpDate(Some("01"), Some("11"), Some("190")),Some("Yes"))))
        inflationProofFormResults.errors must contain(FormError("revaluationDate", List(Messages("gmp.error.year.invalid.format"))))
        inflationProofFormResults.errors must not contain(FormError("revaluationDate", List(Messages("gmp.error.year.invalid"))))
      }
    }

    "entering invalid dates" must {
      "return an error when the day is missing" in {
        val inflationProofFormResults = inflationProofForm.bind(Json.toJson(InflationProof(new GmpDate(Some(""), Some("04"), Some("1978")),Some("Yes"))))
        inflationProofFormResults.errors must contain(FormError("", List(Messages("gmp.error.date.invalid"))))
      }
      "return an error when the month is missing" in {
        val inflationProofFormResults = inflationProofForm.bind(Json.toJson(InflationProof(new GmpDate(Some("04"), Some(""), Some("1978")),Some("Yes"))))
        inflationProofFormResults.errors must contain(FormError("", List(Messages("gmp.error.date.invalid"))))
      }
      "return an error when the year is missing" in {
        val inflationProofFormResults = inflationProofForm.bind(Json.toJson(InflationProof(new GmpDate(Some("04"), Some("04"), Some("")),Some("Yes"))))
        inflationProofFormResults.errors must contain(FormError("", List(Messages("gmp.error.date.invalid"))))
      }
    }

    "entering a date outside valid GMP dates" must {

      "return an error when before 05/04/1978" in {
        val inflationProofFormResults = inflationProofForm.bind(Json.toJson(InflationProof(new GmpDate(Some("04"), Some("04"), Some("1978")),Some("Yes"))))
        inflationProofFormResults.errors must contain(FormError("", List(Messages("gmp.error.reval_date.from"))))
      }

      "not return an error when on 05/04/1978" in {
        val inflationProofFormResults = inflationProofForm.bind(Json.toJson(InflationProof(new GmpDate(Some("05"), Some("04"), Some("1978")),Some("Yes"))))
        inflationProofFormResults.errors must not contain(FormError("", List(Messages("gmp.error.reval_date.from"))))
      }

      "not return an error when after 05/04/1978" in {
        val inflationProofFormResults = inflationProofForm.bind(Json.toJson(InflationProof(new GmpDate(Some("06"), Some("04"), Some("1978")),Some("Yes"))))
        inflationProofFormResults.errors must not contain(FormError("", List(Messages("gmp.error.reval_date.from"))))
      }

      "return an error when after 04/04/2046" in {
        val inflationProofFormResults = inflationProofForm.bind(Json.toJson(InflationProof(new GmpDate(Some("05"), Some("04"), Some("2046")),Some("Yes"))))
        inflationProofFormResults.errors must contain(FormError("", List(Messages("gmp.error.reval_date.to"))))
      }

      "not return an error when on 04/04/2046" in {
        val inflationProofFormResults = inflationProofForm.bind(Json.toJson(InflationProof(new GmpDate(Some("04"), Some("04"), Some("2046")),Some("Yes"))))
        inflationProofFormResults.errors must not contain(FormError("", List(Messages("gmp.error.reval_date.to"))))
      }

      "not return an error when before 04/04/2046" in {
        val inflationProofFormResults = inflationProofForm.bind(Json.toJson(InflationProof(new GmpDate(Some("03"), Some("04"), Some("2046")),Some("Yes"))))
        inflationProofFormResults.errors must not contain(FormError("", List(Messages("gmp.error.reval_date.to"))))
      }
    }

  }

}
