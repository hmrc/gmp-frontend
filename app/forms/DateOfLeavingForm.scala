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
import models.{GmpDate, Leaving, OptionalLeavingForm}
import play.api.data.{Form, Forms, Mapping}
import play.api.data.Forms._
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError, ValidationResult}
import play.api.i18n.{Messages, MessagesImpl}
import play.api.mvc.MessagesControllerComponents
import uk.gov.voa.play.form.ConditionalMappings.mandatoryIfEqual

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.util.Try
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
      .verifying(dateOfLeavingConstraint)
  )
//  private type RawFormValues = (String, String, String)
//
//  def dateOfLeavingForm1(mandatoryMessage: String) = Form(Forms.mapping(
//    leavingDate -> mandatoryIfEqual(radioButtons, Leaving.YES_AFTER, dateMapping),
//    radioButtons -> text.verifying(radioButtonSelected(mandatoryMessage))
//  )(Leaving.apply)(Leaving.unapply))
//
//  private def formValueMapping = tuple(
//    s"$leavingDate-day" -> text,
//    s"$leavingDate-month" -> text,
//    s"$leavingDate-year" -> text
//  )
//
//  private def messageKeyForError(error: String) = s"$leavingDate.$error"
//  private def invalid(error: String, params: String*) =
//    Invalid(
//      Seq(
//        ValidationError(
//          messageKeyForError(error),
//          params: _*
//        )
//      )
//    )
//
//
//
//  private def localDateFromValues(d: String, m: String, y: String) = Try(LocalDate.of(y.toInt, m.toInt, d.toInt))
//
//  private val dateIsValid: RawFormValues => ValidationResult = {
//    case (d, m, y) if Try(s"$d$m$y".toInt).isFailure => invalid("gmp.error.date.leaving.invalid")
//    case (d, m, y) if localDateFromValues(d, m, y).isFailure => invalid("gmp.error.date.leaving.invalid")
//    case _ => Valid
//  }
//
//  private def radioButtonSelected(mandatoryMessage: String) = Constraint[String] { r: String =>
//    if (r.isEmpty) invalid(mandatoryMessage) else Valid
//  }
//  private val dateFormatter = DateTimeFormatter.ofPattern("d M yyyy")
//
//  private val dateInAllowedRange: RawFormValues => ValidationResult = {
//    case (d, m, y) =>
//      localDateFromValues(d, m, y)
//      .map { parsedDate =>
//        if(parsedDate.isBefore(LocalDate.of(2016, 4, 6)))
//          invalid("gmp.error.leaving_on_or_after.too_low")
//        else if (parsedDate.isAfter(LocalDate.of(2046, 4, 5)))
//          invalid("gmp.error.leaving_on_or_after.too_high")
//        else Valid
//      }.getOrElse(Valid)
//    case _ => Valid
//  }
//
//
//  def dateMapping: Mapping[GmpDate] =  formValueMapping
//    .transform({ case (d, m, y) =>
//      (d.trim, m.trim, y.trim) }, { v: RawFormValues => v })
//    .verifying(Constraint(dateIsValid(_)))
//    .verifying(Constraint(dateInAllowedRange(_)))
//    .transform(
//      { case (d, m, y) =>
//        val date: LocalDate = LocalDate.parse(LocalDate.of(y.toInt, m.toInt, d.toInt).format(dateFormatter), dateFormatter)
//        GmpDate(Some(date.getDayOfMonth.toString), Some(date.getMonthValue.toString), Some(y))
//      },
//      (d: GmpDate) => (d.day.getOrElse(""), d.month.getOrElse(""), d.year.getOrElse(""))
//    )


}

object DateOfLeavingForm {
  object Fields {
    val radioButtons = "leaving"
    val leavingDate = "leavingDate"
  }
}

