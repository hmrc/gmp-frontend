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

package forms

import models.{GmpDate, Leaving}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import org.scalatestplus.play.PlaySpec
import play.api.data.FormError
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.api.libs.json.Json
import play.api.mvc.MessagesControllerComponents

class DateOfLeavingFormSpec extends PlaySpec with GuiceOneAppPerSuite with MockitoSugar{
  implicit lazy val messagesAPI: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit lazy val messagesProvider: MessagesImpl = MessagesImpl(Lang("en"), messagesAPI)
  lazy val mcc: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]
  lazy val dateOfLeavingForm = new DateOfLeavingForm(mcc).dateOfLeavingForm()
  val fromJsonMaxChars: Int = 102400
  val leavingDate = GmpDate(Some("06"), Some("04"), Some("2016"))
  def invalidDateError(key: String = "leavingDate", error: String = "error.invalid"): FormError = FormError(key, List(error), List.empty)

  "Leaving Form" must {
    "return no errors when valid values are entered" in {

      val dateOfLeavingFormResults = dateOfLeavingForm.bind(Json.toJson(Leaving(leavingDate,Some(Leaving.YES_AFTER))), fromJsonMaxChars)

      assert(dateOfLeavingFormResults.errors.size == 0)

    }

    "return errors when leaving date not entered" in {

      val dateOfLeavingFormResults = dateOfLeavingForm.bind(Json.toJson(Leaving(leavingDate.copy(None,None,None),Some(Leaving.YES_AFTER))),fromJsonMaxChars)

      assert(dateOfLeavingFormResults.errors.size == 1)

    }

    "entering a day" must {

      "return an error on the date when it is not a number" in {
        val dateOfLeavingFormResults = dateOfLeavingForm.bind(Json.toJson(Leaving(GmpDate(Some("a"), Some("01"), Some("2012")),Some(Leaving.YES_AFTER))), fromJsonMaxChars)
        dateOfLeavingFormResults.errors must contain(invalidDateError("leavingDate.day"))
      }

      "return an error on the date when it is out of range" in {
        val dateOfLeavingFormResults = dateOfLeavingForm.bind(Json.toJson(Leaving(new GmpDate(Some("32"), Some("01"), Some("2012")),Some(Leaving.YES_AFTER))),fromJsonMaxChars)
        dateOfLeavingFormResults.errors must contain(invalidDateError("leavingDate.day"))
      }
    }

    "entering a month" must {

      "return an error on the date when it is not a number" in {
        val dateOfLeavingFormResults = dateOfLeavingForm.bind(Json.toJson(Leaving(new GmpDate(Some("01"), Some("a"), Some("2012")),Some(Leaving.YES_AFTER))),fromJsonMaxChars)
        dateOfLeavingFormResults.errors must contain(invalidDateError("leavingDate.month"))
      }

      "return an error on the date when it is out of range" in {
        val dateOfLeavingFormResults = dateOfLeavingForm.bind(Json.toJson(Leaving(new GmpDate(Some("01"), Some("13"), Some("2012")),Some(Leaving.YES_AFTER))), fromJsonMaxChars)
        dateOfLeavingFormResults.errors must contain(invalidDateError("leavingDate.month"))
      }
    }

    "entering a year" must {

      "return an error on the date when it is not a number" in {
        val dateOfLeavingFormResults = dateOfLeavingForm.bind(Json.toJson(Leaving(new GmpDate(Some("01"), Some("12"), Some("21a1")),Some(Leaving.YES_AFTER))), fromJsonMaxChars)
        dateOfLeavingFormResults.errors must contain(invalidDateError("leavingDate.year"))
      }

      "return an error on the date when it is not the correct format" in {
        val dateOfLeavingFormResults = dateOfLeavingForm.bind(Json.toJson(Leaving(new GmpDate(Some("01"), Some("11"), Some("190")),Some(Leaving.YES_AFTER))), fromJsonMaxChars)
        dateOfLeavingFormResults.errors must contain(invalidDateError("leavingDate.year", "error.yearLength"))
      }
    }

    "entering invalid dates" must {
      "return an error when the day is missing" in {
        val dateOfLeavingFormResults = dateOfLeavingForm.bind(Json.toJson(Leaving(new GmpDate(Some(""), Some("04"), Some("1978")),Some(Leaving.YES_AFTER))), fromJsonMaxChars)
        dateOfLeavingFormResults.errors must contain(invalidDateError("leavingDate.day", "error.dayRequired"))
      }
      "return an error when the month is missing" in {
        val dateOfLeavingFormResults = dateOfLeavingForm.bind(Json.toJson(Leaving(new GmpDate(Some("04"), Some(""), Some("1978")),Some(Leaving.YES_AFTER))), fromJsonMaxChars)
        dateOfLeavingFormResults.errors must contain(invalidDateError("leavingDate.month", "error.monthRequired"))
      }
      "return an error when the year is missing" in {
        val dateOfLeavingFormResults = dateOfLeavingForm.bind(Json.toJson(Leaving(new GmpDate(Some("04"), Some("04"), Some("")),Some(Leaving.YES_AFTER))), fromJsonMaxChars)
        dateOfLeavingFormResults.errors must contain(invalidDateError("leavingDate.year", "error.yearRequired"))
      }

      "return an error if before 06/04/2016" in {
        val dateOfLeavingFormResults = dateOfLeavingForm.bind(Json.toJson(Leaving(new GmpDate(Some("04"), Some("04"), Some("2016")),Some(Leaving.YES_AFTER))), fromJsonMaxChars)
        dateOfLeavingFormResults.errors must contain(FormError("leavingDate", List("error.tooFarInPast"), List("6 April 2016")))
      }

      "return an error if not before 01/01/2100" in {
        val dateOfLeavingFormResults = dateOfLeavingForm.bind(Json.toJson(Leaving(new GmpDate(Some("01"), Some("01"), Some("2100")),Some(Leaving.YES_AFTER))), fromJsonMaxChars)
        dateOfLeavingFormResults.errors must contain(FormError("leavingDate", List("error.tooFuture"), List("5 April 2046")))
      }
    }

    "enter a valid date" must {
      "enter a date before 06/04/2016 and YES_BEFORE" in {
        val dateOfLeavingFormResults = dateOfLeavingForm.bind(Json.toJson(Leaving(new GmpDate(Some("04"), Some("04"), Some("2016")),Some(Leaving.YES_BEFORE))), fromJsonMaxChars)
        dateOfLeavingFormResults.errors.size must be(0)
      }

      "enter a valid date" must {
        "enter a date after 06/04/2016 and YES_AFTER" in {
          val dateOfLeavingFormResults = dateOfLeavingForm.bind(Json.toJson(Leaving(new GmpDate(Some("07"), Some("04"), Some("2016")),Some(Leaving.YES_AFTER))), fromJsonMaxChars)
          dateOfLeavingFormResults.errors.size must be(0)
        }
      }
    }
  }
}
