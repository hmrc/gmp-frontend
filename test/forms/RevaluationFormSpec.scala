/*
 * Copyright 2018 HM Revenue & Customs
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

import forms.RevaluationForm._
import models.{GmpDate, Leaving, RevaluationDate}
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.data.FormError
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.libs.json.Json

class RevaluationFormSpec extends PlaySpec with OneAppPerSuite {

  val revaluationDate = GmpDate(Some("01"), Some("02"), Some("2010"))
  val leavingDate = GmpDate(None, None, None)
  val leaving = Leaving(leavingDate, None)
  val leavingWithDate = Leaving(GmpDate(Some("01"), Some("01"), Some("2012")), None)
  val leavingWithDateAndNO = Leaving(GmpDate(Some("01"), Some("01"), Some("2012")), Some(Leaving.NO))

  "Revaluation Form" must {
    "return no errors when valid values are entered" in {

      val revaluationFormResults = revaluationForm.bind(Json.toJson(RevaluationDate(revaluationDate, leaving)))

      assert(revaluationFormResults.errors.size == 0)

    }

    "return errors when revaluation date not entered" in {

      val revaluationFormResults = revaluationForm.bind(Json.toJson(RevaluationDate(revaluationDate.copy(None,None,None), leaving)))

      assert(revaluationFormResults.errors.size == 1)

    }

    "entering a day" must {

      "return an error on the date when it is not a number" in {
        val revaluationFormResults = revaluationForm.bind(Json.toJson(RevaluationDate(GmpDate(Some("a"), Some("01"), Some("2012")), leaving)))
        revaluationFormResults.errors must contain(FormError("revaluationDate", List(Messages("gmp.error.date.nonnumber"))))
      }

      "return an error on the date when it is out of range" in {
        val revaluationFormResults = revaluationForm.bind(Json.toJson(RevaluationDate(new GmpDate(Some("32"), Some("01"), Some("2012")), leaving)))
        revaluationFormResults.errors must contain(FormError("revaluationDate", List(Messages("gmp.error.day.invalid"))))
      }
    }

    "entering a month" must {

      "return an error on the date when it is not a number" in {
        val revaluationFormResults = revaluationForm.bind(Json.toJson(RevaluationDate(new GmpDate(Some("01"), Some("a"), Some("2012")), leaving)))
        revaluationFormResults.errors must contain(FormError("revaluationDate", List(Messages("gmp.error.date.nonnumber"))))
      }

      "return an error on the date when it is out of range" in {
        val revaluationFormResults = revaluationForm.bind(Json.toJson(RevaluationDate(new GmpDate(Some("01"), Some("13"), Some("2012")), leaving)))
        revaluationFormResults.errors must contain(FormError("revaluationDate", List(Messages("gmp.error.month.invalid"))))
      }
    }

    "entering a year" must {

      "return an error on the date when it is not a number" in {
        val revaluationFormResults = revaluationForm.bind(Json.toJson(RevaluationDate(new GmpDate(Some("01"), Some("12"), Some("21a1")), leaving)))
        revaluationFormResults.errors must contain(FormError("revaluationDate", List(Messages("gmp.error.date.nonnumber"))))
      }

      "return an error on the date when it is not the correct format" in {
        val revaluationFormResults = revaluationForm.bind(Json.toJson(RevaluationDate(new GmpDate(Some("01"), Some("11"), Some("190")), leaving)))
        revaluationFormResults.errors must contain(FormError("revaluationDate", List(Messages("gmp.error.year.invalid.format"))))
        revaluationFormResults.errors must not contain(FormError("revaluationDate", List(Messages("gmp.error.year.invalid"))))
      }
    }

    "entering invalid dates" must {
      "return an error when the day is missing" in {
        val revaluationFormResults = revaluationForm.bind(Json.toJson(RevaluationDate(new GmpDate(Some(""), Some("04"), Some("1978")), leaving)))
        revaluationFormResults.errors must contain(FormError("revaluationDate", List(Messages("gmp.error.date.invalid"))))
      }
      "return an error when the month is missing" in {
        val revaluationFormResults = revaluationForm.bind(Json.toJson(RevaluationDate(new GmpDate(Some("04"), Some(""), Some("1978")), leaving)))
        revaluationFormResults.errors must contain(FormError("revaluationDate", List(Messages("gmp.error.date.invalid"))))
      }
      "return an error when the year is missing" in {
        val revaluationFormResults = revaluationForm.bind(Json.toJson(RevaluationDate(new GmpDate(Some("04"), Some("04"), Some("")), leaving)))
        revaluationFormResults.errors must contain(FormError("revaluationDate", List(Messages("gmp.error.date.invalid"))))
      }
    }

    "entering a date outside valid GMP dates" must {

      "return an error when before 05/04/1978" in {
        val revaluationFormResults = revaluationForm.bind(Json.toJson(RevaluationDate(new GmpDate(Some("04"), Some("04"), Some("1978")), leaving)))
        revaluationFormResults.errors must contain(FormError("revaluationDate", List(Messages("gmp.error.reval_date.from"))))
      }

      "not return an error when on 05/04/1978" in {
        val revaluationFormResults = revaluationForm.bind(Json.toJson(RevaluationDate(new GmpDate(Some("05"), Some("04"), Some("1978")), leaving)))
        revaluationFormResults.errors must not contain(FormError("revaluationDate", List(Messages("gmp.error.reval_date.from"))))
      }

      "not return an error when after 05/04/1978" in {
        val revaluationFormResults = revaluationForm.bind(Json.toJson(RevaluationDate(new GmpDate(Some("06"), Some("04"), Some("1978")), leaving)))
        revaluationFormResults.errors must not contain(FormError("revaluationDate", List(Messages("gmp.error.reval_date.from"))))
      }

      "return an error when after 04/04/2046" in {
        val revaluationFormResults = revaluationForm.bind(Json.toJson(RevaluationDate(new GmpDate(Some("05"), Some("04"), Some("2046")), leaving)))
        revaluationFormResults.errors must contain(FormError("revaluationDate", List(Messages("gmp.error.reval_date.to"))))
      }

      "not return an error when on 04/04/2046" in {
        val revaluationFormResults = revaluationForm.bind(Json.toJson(RevaluationDate(new GmpDate(Some("04"), Some("04"), Some("2046")), leaving)))
        revaluationFormResults.errors must not contain(FormError("revaluationDate", List(Messages("gmp.error.reval_date.to"))))
      }

      "not return an error when before 04/04/2046" in {
        val revaluationFormResults = revaluationForm.bind(Json.toJson(RevaluationDate(new GmpDate(Some("03"), Some("04"), Some("2046")), leaving)))
        revaluationFormResults.errors must not contain(FormError("revaluationDate", List(Messages("gmp.error.reval_date.to"))))
      }
    }

    "revaluationDate" must {
      "return an error if before leavingDate" in {
        val revaluationFormResults = revaluationForm.bind(Json.toJson(RevaluationDate(revaluationDate, leavingWithDate)))
        revaluationFormResults.errors must contain(FormError("", List(Messages("gmp.error.revaluation_before_leaving", leavingWithDate.leavingDate.getAsText)), List("revaluationDate")))
      }

      "return an error if Leaving.NO and revaluation date entered was before 2016" in {
        val revaluationFormResults = revaluationForm.bind(Json.toJson(RevaluationDate(revaluationDate, leavingWithDateAndNO)))
        revaluationFormResults.errors must contain(FormError("", List(Messages("gmp.error.revaluation_pre2016_not_left")), List("revaluationDate")))
      }
    }

  }

}
