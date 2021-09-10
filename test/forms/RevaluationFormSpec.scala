/*
 * Copyright 2021 HM Revenue & Customs
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

import models.{GmpDate, Leaving, RevaluationDate}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import org.scalatestplus.play.PlaySpec
import play.api.data.FormError
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.api.libs.json.Json
import play.api.mvc.MessagesControllerComponents

class RevaluationFormSpec extends PlaySpec with GuiceOneAppPerSuite {

  val revaluationDate = GmpDate(Some("01"), Some("02"), Some("2010"))
  val leavingDate = GmpDate(None, None, None)
  val leaving = Leaving(leavingDate, None)
  val leavingBefore2016 = Leaving(leavingDate, Some(Leaving.YES_BEFORE))
  val leavingWithDate = Leaving(GmpDate(Some("01"), Some("01"), Some("2012")), None)
  val leavingWithDateAndNO = Leaving(GmpDate(Some("01"), Some("01"), Some("2012")), Some(Leaving.NO))
  implicit lazy val messagesAPI=app.injector.instanceOf[MessagesApi]
  implicit lazy val messagesProvider=MessagesImpl(Lang("en"), messagesAPI)
  lazy val mcc = app.injector.instanceOf[MessagesControllerComponents]
  lazy val revaluationForm = new RevaluationForm(mcc).revaluationForm
  val fromJsonMaxChars: Int = 102400

  "Revaluation Form" must {
    "return no errors when valid values are entered" in {

      val revaluationFormResults = revaluationForm.bind(Json.toJson(RevaluationDate(leaving, revaluationDate)), fromJsonMaxChars)

      assert(revaluationFormResults.errors.size == 0)

    }

    "return errors when revaluation date not entered" in {

      val revaluationFormResults = revaluationForm.bind(Json.toJson(RevaluationDate(leaving, revaluationDate.copy(None,None,None))), fromJsonMaxChars)

      assert(revaluationFormResults.errors.size == 1)

    }

    "entering a day" must {

      "return an error on the date when it is not a number" in {
        val revaluationFormResults = revaluationForm.bind(Json.toJson(RevaluationDate(leaving, GmpDate(Some("a"), Some("01"), Some("2012")))),fromJsonMaxChars)
        revaluationFormResults.errors must contain(FormError("revaluationDate", List(Messages("gmp.error.date.invalid"))))
      }

      "return an error on the date when it is out of range" in {
        val revaluationFormResults = revaluationForm.bind(Json.toJson(RevaluationDate(leaving, new GmpDate(Some("32"), Some("01"), Some("2012")))),fromJsonMaxChars)
        revaluationFormResults.errors must contain(FormError("revaluationDate", List(Messages("gmp.error.date.invalid"))))
      }
    }

    "entering a month" must {

      "return an error on the date when it is not a number" in {
        val revaluationFormResults = revaluationForm.bind(Json.toJson(RevaluationDate(leaving, new GmpDate(Some("01"), Some("a"), Some("2012")))),fromJsonMaxChars)
        revaluationFormResults.errors must contain(FormError("revaluationDate", List(Messages("gmp.error.date.invalid"))))
      }

      "return an error on the date when it is out of range" in {
        val revaluationFormResults = revaluationForm.bind(Json.toJson(RevaluationDate(leaving, new GmpDate(Some("01"), Some("13"), Some("2012")))),fromJsonMaxChars)
        revaluationFormResults.errors must contain(FormError("revaluationDate", List(Messages("gmp.error.date.invalid"))))
      }
    }

    "entering a year" must {

      "return an error on the date when it is not a number" in {
        val revaluationFormResults = revaluationForm.bind(Json.toJson(RevaluationDate(leaving, new GmpDate(Some("01"), Some("12"), Some("21a1")))),fromJsonMaxChars)
        revaluationFormResults.errors must contain(FormError("revaluationDate", List(Messages("gmp.error.date.invalid"))))
      }

      "return an error on the date when it is not the correct format" in {
        val revaluationFormResults = revaluationForm.bind(Json.toJson(RevaluationDate(leaving, new GmpDate(Some("01"), Some("11"), Some("190")))),fromJsonMaxChars)
        revaluationFormResults.errors must contain(FormError("revaluationDate", List(Messages("gmp.error.date.invalid"))))
      }
    }

    "entering invalid dates" must {
      "return an error when the day is missing" in {
        val revaluationFormResults = revaluationForm.bind(Json.toJson(RevaluationDate(leaving, new GmpDate(Some(""), Some("04"), Some("1978")))),fromJsonMaxChars)
        revaluationFormResults.errors must contain(FormError("revaluationDate", List(Messages("gmp.error.date.invalid"))))
      }
      "return an error when the month is missing" in {
        val revaluationFormResults = revaluationForm.bind(Json.toJson(RevaluationDate(leaving, new GmpDate(Some("04"), Some(""), Some("1978")))),fromJsonMaxChars)
        revaluationFormResults.errors must contain(FormError("revaluationDate", List(Messages("gmp.error.date.invalid"))))
      }
      "return an error when the year is missing" in {
        val revaluationFormResults = revaluationForm.bind(Json.toJson(RevaluationDate(leaving, new GmpDate(Some("04"), Some("04"), Some("")))),fromJsonMaxChars)
        revaluationFormResults.errors must contain(FormError("revaluationDate", List(Messages("gmp.error.date.invalid"))))
      }
    }

    "entering a date outside valid GMP dates" must {

      "return an error when before 05/04/1978 and also left the scheme before 2016" in {
        val revaluationFormResults = revaluationForm.bind(Json.toJson(RevaluationDate(leavingBefore2016, new GmpDate(Some("04"), Some("04"), Some("1978")))),fromJsonMaxChars)
        revaluationFormResults.errors must contain(FormError("", List(Messages("gmp.error.reval_date.from")), List("revaluationDate")))
      }

      "not return an error when on 05/04/1978" in {
        val revaluationFormResults = revaluationForm.bind(Json.toJson(RevaluationDate(leaving, new GmpDate(Some("05"), Some("04"), Some("1978")))),fromJsonMaxChars)
        revaluationFormResults.errors must not contain(FormError("revaluationDate", List(Messages("gmp.error.reval_date.from"))))
      }

      "not return an error when after 05/04/1978" in {
        val revaluationFormResults = revaluationForm.bind(Json.toJson(RevaluationDate(leaving, new GmpDate(Some("06"), Some("04"), Some("1978")))),fromJsonMaxChars)
        revaluationFormResults.errors must not contain(FormError("revaluationDate", List(Messages("gmp.error.reval_date.from"))))
      }

      "return an error when after 04/04/2046" in {
        val revaluationFormResults = revaluationForm.bind(Json.toJson(RevaluationDate(leaving, new GmpDate(Some("05"), Some("04"), Some("2046")))),fromJsonMaxChars)
        revaluationFormResults.errors must contain(FormError("revaluationDate", List(Messages("gmp.error.reval_date.to"))))
      }

      "not return an error when on 04/04/2046" in {
        val revaluationFormResults = revaluationForm.bind(Json.toJson(RevaluationDate(leaving, new GmpDate(Some("04"), Some("04"), Some("2046")))),fromJsonMaxChars)
        revaluationFormResults.errors must not contain(FormError("revaluationDate", List(Messages("gmp.error.reval_date.to"))))
      }

      "not return an error when before 04/04/2046" in {
        val revaluationFormResults = revaluationForm.bind(Json.toJson(RevaluationDate(leaving, new GmpDate(Some("03"), Some("04"), Some("2046")))),fromJsonMaxChars)
        revaluationFormResults.errors must not contain(FormError("revaluationDate", List(Messages("gmp.error.reval_date.to"))))
      }
    }

    "revaluationDate" must {
      "return an error if before leavingDate" in {
        val revaluationFormResults = revaluationForm.bind(Json.toJson(RevaluationDate(leavingWithDate, revaluationDate)),fromJsonMaxChars)
        revaluationFormResults.errors must contain(FormError("", List(Messages("gmp.error.revaluation_before_leaving", leavingWithDate.leavingDate.getAsText)), List("revaluationDate")))
      }

      "return an error if Leaving.NO and revaluation date entered was before 2016" in {
        val revaluationFormResults = revaluationForm.bind(Json.toJson(RevaluationDate(leavingWithDateAndNO, revaluationDate)),fromJsonMaxChars)
        revaluationFormResults.errors must contain(FormError("", List(Messages("gmp.error.revaluation_pre2016_not_left")), List("revaluationDate")))
      }
    }

  }

}
