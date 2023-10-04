/*
 * Copyright 2023 HM Revenue & Customs
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

import models.{GmpDate, InflationProof}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import org.scalatestplus.play.PlaySpec
import play.api.data.FormError
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.api.libs.json.Json
import play.api.mvc.MessagesControllerComponents

class inflationProofFormSpec extends PlaySpec with GuiceOneAppPerSuite {
  implicit lazy val messagesAPI=app.injector.instanceOf[MessagesApi]
  implicit lazy val messagesProvider=MessagesImpl(Lang("en"), messagesAPI)
  lazy val mcc = app.injector.instanceOf[MessagesControllerComponents]
  lazy val inflationProofForm = new InflationProofForm(mcc).inflationProofForm(1978,2046)

  val fromJsonMaxChars: Int = 102400
  val inflationProofDate = GmpDate(Some("01"), Some("02"), Some("2010"))
  def invalidDateError(key: String = "revaluationDate", error: String = "error.invalid"): FormError = FormError(key, List(error), List.empty)

  "InflationProof Form" must {
    "return no errors when valid values are entered" in {

      val inflationProofFormResults = inflationProofForm.bind(Json.toJson(InflationProof(inflationProofDate,Some("Yes"))), fromJsonMaxChars)

      assert(inflationProofFormResults.errors.size == 0)

    }

    "return no errors when inlfation proofing not wanted" in {

      val inflationProofFormResults = inflationProofForm.bind(Json.toJson(InflationProof(inflationProofDate.copy(None,None,None),Some("No"))), fromJsonMaxChars)

      assert(inflationProofFormResults.errors.size == 0)

    }

    "return errors when inlfation proofing requested but date not entered" in {

      val inflationProofFormResults = inflationProofForm.bind(Json.toJson(InflationProof(inflationProofDate.copy(None,None,None),Some("Yes"))), fromJsonMaxChars)

      assert(inflationProofFormResults.errors.size == 1)

    }

    "return errors when InflationProof yes/no not entered" in {

      val inflationProofFormResults = inflationProofForm.bind(Json.toJson(InflationProof(inflationProofDate.copy(None,None,None),None)), fromJsonMaxChars)

      assert(inflationProofFormResults.errors.size == 1)

    }

    "entering a day" must {

      "return an error on the date when it is not a number" in {
        val inflationProofFormResults = inflationProofForm.bind(Json.toJson(InflationProof(new GmpDate(Some("a"), Some("01"), Some("2012")),Some("Yes"))), fromJsonMaxChars)
        inflationProofFormResults.errors must contain(invalidDateError("revaluationDate.day"))
      }

      "return an error on the date when it is out of range" in {
        val inflationProofFormResults = inflationProofForm.bind(Json.toJson(InflationProof(new GmpDate(Some("32"), Some("01"), Some("2012")),Some("Yes"))), fromJsonMaxChars)
        inflationProofFormResults.errors must contain(invalidDateError("revaluationDate.day"))
      }
    }

    "entering a month" must {

      "return an error on the date when it is not a number" in {
        val inflationProofFormResults = inflationProofForm.bind(Json.toJson(InflationProof(new GmpDate(Some("01"), Some("a"), Some("2012")),Some("Yes"))), fromJsonMaxChars)
        inflationProofFormResults.errors must contain(invalidDateError("revaluationDate.month"))
      }

      "return an error on the date when it is out of range" in {
        val inflationProofFormResults = inflationProofForm.bind(Json.toJson(InflationProof(new GmpDate(Some("01"), Some("13"), Some("2012")),Some("Yes"))), fromJsonMaxChars)
        inflationProofFormResults.errors must contain(invalidDateError("revaluationDate.month"))
      }
    }

    "entering a year" must {

      "return an error on the date when it is not a number" in {
        val inflationProofFormResults = inflationProofForm.bind(Json.toJson(InflationProof(new GmpDate(Some("01"), Some("12"), Some("21a1")),Some("Yes"))), fromJsonMaxChars)
        inflationProofFormResults.errors must contain(invalidDateError("revaluationDate.year"))
      }

      "return an error on the date when it is not the correct format" in {
        val inflationProofFormResults = inflationProofForm.bind(Json.toJson(InflationProof(new GmpDate(Some("01"), Some("11"), Some("190")),Some("Yes"))), fromJsonMaxChars)
        inflationProofFormResults.errors must contain(invalidDateError("revaluationDate.year", "error.yearLength"))
        inflationProofFormResults.errors must not contain(invalidDateError("revaluationDate.day"))
      }
    }

    "entering invalid dates" must {
      "return an error when the day is missing" in {
        val inflationProofFormResults = inflationProofForm.bind(Json.toJson(InflationProof(new GmpDate(Some(""), Some("04"), Some("1978")),Some("Yes"))), fromJsonMaxChars)
        inflationProofFormResults.errors must contain(invalidDateError("revaluationDate.day", "error.dayRequired"))
      }
      "return an error when the month is missing" in {
        val inflationProofFormResults = inflationProofForm.bind(Json.toJson(InflationProof(new GmpDate(Some("04"), Some(""), Some("1978")),Some("Yes"))), fromJsonMaxChars)
        inflationProofFormResults.errors must contain(invalidDateError("revaluationDate.month", "error.monthRequired"))
      }
      "return an error when the year is missing" in {
        val inflationProofFormResults = inflationProofForm.bind(Json.toJson(InflationProof(new GmpDate(Some("04"), Some("04"), Some("")),Some("Yes"))), fromJsonMaxChars)
        inflationProofFormResults.errors must contain(invalidDateError("revaluationDate.year", "error.yearRequired"))
      }
    }

    "entering a date outside valid GMP dates" must {

      "return an error when before 05/04/1978" in {
        val inflationProofFormResults = inflationProofForm.bind(Json.toJson(InflationProof(new GmpDate(Some("03"), Some("04"), Some("1978")),Some("Yes"))), fromJsonMaxChars)
        inflationProofFormResults.errors must contain(FormError("revaluationDate", List("error.tooFarInPast"), List("4 April 1978")))
      }

      "not return an error when on 05/04/1978" in {
        val inflationProofFormResults = inflationProofForm.bind(Json.toJson(InflationProof(new GmpDate(Some("05"), Some("04"), Some("1978")),Some("Yes"))), fromJsonMaxChars)
        inflationProofFormResults.errors must not contain(invalidDateError("revaluationDate.day"))
      }

      "not return an error when after 05/04/1978" in {
        val inflationProofFormResults = inflationProofForm.bind(Json.toJson(InflationProof(new GmpDate(Some("06"), Some("04"), Some("1978")),Some("Yes"))), fromJsonMaxChars)
        inflationProofFormResults.errors must not contain(invalidDateError("revaluationDate.day"))
      }

      "return an error when after 04/04/2046" in {
        val inflationProofFormResults = inflationProofForm.bind(Json.toJson(InflationProof(new GmpDate(Some("05"), Some("04"), Some("2046")),Some("Yes"))), fromJsonMaxChars)
        inflationProofFormResults.errors.size must be(0)
      }

      "not return an error when on 04/04/2046" in {
        val inflationProofFormResults = inflationProofForm.bind(Json.toJson(InflationProof(new GmpDate(Some("04"), Some("04"), Some("2046")),Some("Yes"))), fromJsonMaxChars)
        inflationProofFormResults.errors must not contain(invalidDateError("revaluationDate.day"))
      }

      "not return an error when before 04/04/2046" in {
        val inflationProofFormResults = inflationProofForm.bind(Json.toJson(InflationProof(new GmpDate(Some("03"), Some("04"), Some("2046")),Some("Yes"))), fromJsonMaxChars)
        inflationProofFormResults.errors must not contain(invalidDateError("revaluationDate.day"))
      }
    }

  }

}
