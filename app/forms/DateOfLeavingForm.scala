/*
 * Copyright 2022 HM Revenue & Customs
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
import forms.DateOfLeavingForm.Fields.{leavingDate, radioButtons}

import javax.inject.Inject
import models.{GmpDate, Leaving}
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}
import play.api.i18n.{Messages, MessagesImpl}
import play.api.mvc.MessagesControllerComponents


@Singleton
class DateOfLeavingForm  @Inject()(mcc: MessagesControllerComponents) {
  implicit lazy val messages: Messages = MessagesImpl(mcc.langs.availables.head, mcc.messagesApi)

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
          Seq(ValidationError(messages("gmp.error.leaving_date.daymonthyear.mandatory"), "leavingDate"))
        } else if (leaving.leaving.isDefined && leaving.leaving.get.equals(Leaving.YES_AFTER) && !checkValidDate(leaving.leavingDate)) {
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

  private val allDateValuesEnteredConstraint: Constraint[Leaving] = Constraint("leavingDate")({
    leaving => {
      println(" leaving is in form :;"+leaving)
      if(leaving.leaving.isDefined && leaving.leaving.get == Leaving.YES_AFTER){
        (leaving.leavingDate.day, leaving.leavingDate.month, leaving.leavingDate.year) match {
          case (None, Some(_), Some(_)) => Invalid(Seq(ValidationError(messages("gmp.error.leaving_date.day.missing"), "leavingDate")))
          case (Some(_), None, Some(_)) => Invalid(Seq(ValidationError(messages("gmp.error.leaving_date.month.missing"), "leavingDate")))
          case (Some(_), Some(_), None) => Invalid(Seq(ValidationError(messages("gmp.error.leaving_date.year.missing"), "leavingDate")))
          case (None, None, Some(_)) => Invalid(Seq(ValidationError(messages("gmp.error.leaving_date.daymonth.missing"), "leavingDate")))
          case (None, Some(_), None) => Invalid(Seq(ValidationError(messages("gmp.error.leaving_date.dayyear.missing"), "leavingDate")))
          case (Some(_), None, None) => Invalid(Seq(ValidationError(messages("gmp.error.leaving_date.monthyear.missing"), "leavingDate")))
          case _ => Valid
        }
      } else Valid
    }
  })


  def dateOfLeavingForm(mandatoryMessage: String) = Form(
    mapping(
      leavingDate -> mapping(
        "day" -> optional(text),
        "month" -> optional(text),
        "year" -> optional(text)
      )(GmpDate.apply)(GmpDate.unapply),
      radioButtons -> optional(text).verifying(mandatoryMessage, {
        _.isDefined
      })
    )(Leaving.apply)(Leaving.unapply)
      .verifying(allDateValuesEnteredConstraint)
      .verifying(dateOfLeavingConstraint)
  )

}

object DateOfLeavingForm {
  object Fields {
    val radioButtons = "leaving"
    val leavingDate = "leavingDate"
  }
}

