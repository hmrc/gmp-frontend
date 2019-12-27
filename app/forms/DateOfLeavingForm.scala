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

package forms

import com.google.inject.Singleton
import models.{GmpDate, Leaving}
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}
import play.api.i18n.Messages
import play.api.inject.guice.GuiceApplicationBuilder
@Singleton
class BaseDateOfLeavingForm(messages: Messages) {

  val YEAR_FIELD_LENGTH: Int = 4

  def dateMustBePresentIfHaveDateOfLeavingAfterApril2016(x: Leaving): Boolean = {
    x.leaving match {
      case Some(leavingmessage) if leavingmessage == Leaving.YES_AFTER => x.leavingDate.day.isDefined ||
        x.leavingDate.month.isDefined || x.leavingDate.year.isDefined
      case _ => true
    }
  }

  val dateOfLeavingConstraint: Constraint[Leaving] = Constraint("leavingDate")({
    leaving => {
      val errors =
        if (!dateMustBePresentIfHaveDateOfLeavingAfterApril2016(leaving)) {
          Seq(ValidationError(messages("gmp.error.leaving_date.mandatory"), "leavingDate"))
        } else if (!checkValidDate(leaving.leavingDate)) {
          Seq(ValidationError(messages("gmp.error.date.leaving.invalid"), "leavingDate"))
        }
        else if (leaving.leaving.isDefined && leaving.leaving.get.equals(Leaving.YES_AFTER) && !leaving.leavingDate.isOnOrAfter06042016) {
          Seq(ValidationError(messages("gmp.error.leaving_on_or_after.too_low"), "leavingDate"))
        }
        else if (leaving.leaving.isDefined && leaving.leaving.get.equals(Leaving.YES_AFTER) && !leaving.leavingDate.isBefore05042046) {
          Seq(ValidationError(messages("gmp.error.leaving_on_or_after.too_high"), "leavingDate"))
        }
        else {
          Nil
        }

      if (errors.isEmpty) {
        Valid
      } else {
        Invalid(errors)
      }
    }
  })

  val dateOfLeavingForm = Form(
    mapping(
      "leavingDate" -> mapping(
        "day" -> optional(text),
        "month" -> optional(text),
        "year" -> optional(text)
      )(GmpDate.apply)(GmpDate.unapply)
//        .verifying(Messages("gmp.error.date.nonnumber"), x => checkForNumber(x.day) && checkForNumber(x.month) && checkForNumber(x.year))
//        .verifying(Messages("gmp.error.day.invalid"), x => checkDayRange(x.day))
//        .verifying(Messages("gmp.error.month.invalid"), x => checkMonthRange(x.month))
//        .verifying(Messages("gmp.error.year.invalid.format"), x => checkYearLength(x.year))
      ,
      "leaving" -> optional(text).verifying(messages("gmp.error.reason.mandatory"), {
        _.isDefined
      })
    )(Leaving.apply)(Leaving.unapply)
      .verifying(dateOfLeavingConstraint)
  )

}

case object DateOfLeavingForm extends BaseDateOfLeavingForm( {
  new GuiceApplicationBuilder().injector().instanceOf[Messages]
}
)
