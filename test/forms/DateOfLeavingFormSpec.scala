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

import forms.DateOfLeavingForm._
import models.{GmpDate, Leaving}
import org.joda.time.{DateTime, DateTimeUtils}
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.data.FormError
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.i18n.Messages.Implicits._

class DateOfLeavingFormSpec extends PlaySpec with OneAppPerSuite with MockitoSugar{
  DateTimeUtils.setCurrentMillisFixed(new DateTime(2016,1,1,1,1).toDate.getTime)
  val leavingDate = GmpDate(Some("06"), Some("04"), Some("2016"))

  "Leaving Form" must {
    "return no errors when valid values are entered" in {

      val dateOfLeavingFormResults = dateOfLeavingForm.bind(Json.toJson(Leaving(leavingDate,Some(Leaving.YES_AFTER))))

      assert(dateOfLeavingFormResults.errors.size == 0)

    }

    "return errors when leaving date not entered" in {

      val dateOfLeavingFormResults = dateOfLeavingForm.bind(Json.toJson(Leaving(leavingDate.copy(None,None,None),Some(Leaving.YES_AFTER))))

      assert(dateOfLeavingFormResults.errors.size == 1)

    }

    "entering a day" must {

      "return an error on the date when it is not a number" in {
        val dateOfLeavingFormResults = dateOfLeavingForm.bind(Json.toJson(Leaving(GmpDate(Some("a"), Some("01"), Some("2012")),Some(Leaving.YES_AFTER))))
        dateOfLeavingFormResults.errors must contain(FormError("leavingDate", List(Messages("gmp.error.date.nonnumber"))))
      }

      "return an error on the date when it is out of range" in {
        val dateOfLeavingFormResults = dateOfLeavingForm.bind(Json.toJson(Leaving(new GmpDate(Some("32"), Some("01"), Some("2012")),Some(Leaving.YES_AFTER))))
        dateOfLeavingFormResults.errors must contain(FormError("leavingDate", List(Messages("gmp.error.day.invalid"))))
      }
    }

    "entering a month" must {

      "return an error on the date when it is not a number" in {
        val dateOfLeavingFormResults = dateOfLeavingForm.bind(Json.toJson(Leaving(new GmpDate(Some("01"), Some("a"), Some("2012")),Some(Leaving.YES_AFTER))))
        dateOfLeavingFormResults.errors must contain(FormError("leavingDate", List(Messages("gmp.error.date.nonnumber"))))
      }

      "return an error on the date when it is out of range" in {
        val dateOfLeavingFormResults = dateOfLeavingForm.bind(Json.toJson(Leaving(new GmpDate(Some("01"), Some("13"), Some("2012")),Some(Leaving.YES_AFTER))))
        dateOfLeavingFormResults.errors must contain(FormError("leavingDate", List(Messages("gmp.error.month.invalid"))))
      }
    }

    "entering a year" must {

      "return an error on the date when it is not a number" in {
        val dateOfLeavingFormResults = dateOfLeavingForm.bind(Json.toJson(Leaving(new GmpDate(Some("01"), Some("12"), Some("21a1")),Some(Leaving.YES_AFTER))))
        dateOfLeavingFormResults.errors must contain(FormError("leavingDate", List(Messages("gmp.error.date.nonnumber"))))
      }

      "return an error on the date when it is not the correct format" in {
        val dateOfLeavingFormResults = dateOfLeavingForm.bind(Json.toJson(Leaving(new GmpDate(Some("01"), Some("11"), Some("190")),Some(Leaving.YES_AFTER))))
        dateOfLeavingFormResults.errors must contain(FormError("leavingDate", List(Messages("gmp.error.year.invalid.format"))))
        dateOfLeavingFormResults.errors must not contain(FormError("leavingDate", List(Messages("gmp.error.year.invalid"))))
      }
    }

    "entering invalid dates" must {
      "return an error when the day is missing" in {
        val dateOfLeavingFormResults = dateOfLeavingForm.bind(Json.toJson(Leaving(new GmpDate(Some(""), Some("04"), Some("1978")),Some(Leaving.YES_AFTER))))
        dateOfLeavingFormResults.errors must contain(FormError("", List(Messages("gmp.error.date.invalid")), List("leavingDate")))
      }
      "return an error when the month is missing" in {
        val dateOfLeavingFormResults = dateOfLeavingForm.bind(Json.toJson(Leaving(new GmpDate(Some("04"), Some(""), Some("1978")),Some(Leaving.YES_AFTER))))
        dateOfLeavingFormResults.errors must contain(FormError("", List(Messages("gmp.error.date.invalid")), List("leavingDate")))
      }
      "return an error when the year is missing" in {
        val dateOfLeavingFormResults = dateOfLeavingForm.bind(Json.toJson(Leaving(new GmpDate(Some("04"), Some("04"), Some("")),Some(Leaving.YES_AFTER))))
        dateOfLeavingFormResults.errors must contain(FormError("", List(Messages("gmp.error.date.invalid")), List("leavingDate")))
      }

      "return an error if before 06/04/2016" in {
        val dateOfLeavingFormResults = dateOfLeavingForm.bind(Json.toJson(Leaving(new GmpDate(Some("04"), Some("04"), Some("2016")),Some(Leaving.YES_AFTER))))
        dateOfLeavingFormResults.errors must contain(FormError("", List(Messages("gmp.error.date.invalid")), List("leavingDate")))
      }
    }

    "enter a valid date" must {
      "enter a date before 06/04/2016 and YES_BEFORE" in {
        val dateOfLeavingFormResults = dateOfLeavingForm.bind(Json.toJson(Leaving(new GmpDate(Some("04"), Some("04"), Some("2016")),Some(Leaving.YES_BEFORE))))
        dateOfLeavingFormResults.errors.size must be(0)
      }

      "enter a valid date" must {
        "enter a date after 06/04/2016 and YES_AFTER" in {
          val dateOfLeavingFormResults = dateOfLeavingForm.bind(Json.toJson(Leaving(new GmpDate(Some("07"), Some("04"), Some("2016")),Some(Leaving.YES_AFTER))))
          dateOfLeavingFormResults.errors.size must be(0)
        }
      }
    }
  }
}
