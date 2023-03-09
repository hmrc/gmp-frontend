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

import com.google.inject.{Inject, Singleton}
import models.{GmpDate, GmpSession, Leaving, RevaluationDate}
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError, ValidationResult}
import play.api.i18n.{Messages, MessagesImpl}
import play.api.mvc.MessagesControllerComponents

@Singleton
class RevaluationForm @Inject()(mcc: MessagesControllerComponents) {
  implicit lazy val messages: Messages = MessagesImpl(mcc.langs.availables.head, mcc.messagesApi)


  val YEAR_FIELD_LENGTH: Int = 4

  def mandatoryDate(date: GmpDate): Boolean = {
    date.day.isDefined || date.month.isDefined || date.year.isDefined
  }

  def  revaluationDateConstraint(session: GmpSession): Constraint[RevaluationDate] = Constraint("revaluationDate")({
    revaluationDate => {
      val errors =
        if (session.leaving.leaving.isDefined &&
          session.leaving.leaving.get.equals(Leaving.NO) &&
            !revaluationDate.revaluationDate.isOnOrAfter06042016)
        {
          Seq(ValidationError(messages("gmp.error.revaluation_pre2016_not_left"), "revaluationDate")) // 2016
        }
        else if (revaluationDate.revaluationDate.isBefore(session.leaving.leavingDate) &&
          session.leaving.leaving == Some(Leaving.YES_AFTER)) {
          Seq(ValidationError(Messages("gmp.error.revaluation_before_leaving", session.leaving.leavingDate.getAsText), "revaluationDate"))
        }
        else if (session.leaving.leaving.isDefined &&
        session.leaving.leaving.get.equals(Leaving.YES_BEFORE) &&
        !revaluationDate.revaluationDate.isOnOrAfter05041978){
          Seq(ValidationError(Messages("gmp.error.reval_date.from"), "revaluationDate"))
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

  private val allDateValuesEntered: GmpDate => ValidationResult = {
    case GmpDate(None, None, None) => invalid("date.emptyfields")
    case GmpDate(None, None, _) => invalid("daymonth.missing")
    case GmpDate(_, None, None) => invalid("monthyear.missing")
    case GmpDate(None, _, None) => invalid("dayyear.missing")
    case GmpDate(None, _, _) => invalid("day.missing")
    case GmpDate(_, None, _) => invalid("month.missing")
    case GmpDate(_, _, None) => invalid("year.missing")
    case _ => Valid
  }

  private val dateIsReal: GmpDate => ValidationResult = {
    case d:GmpDate if(checkValidDate(d))=> Valid
    case _ => invalid("gmp.error.date.invalid")
  }

  private val checkDateOnBefore: GmpDate => ValidationResult = {
    case d: GmpDate if(checkDateOnOBeforeGMPEnd(d)) => Valid
    case _ => invalid("gmp.error.reval_date.to")
  }

  private def invalid(error: String, params: String*) =
    Invalid(
      Seq(
        ValidationError(
          messageKeyForError(error),
          params: _*
        )
      )
    )

  private def messageKeyForError(error: String) = s"reval-date.error.$error"

  lazy val revaluationDateMapping = mapping(
      "day" -> optional(text),
      "month" -> optional(text),
      "year" -> optional(text)
    )(GmpDate.apply)(GmpDate.unapply)
    .verifying(Constraint(allDateValuesEntered))
      .verifying(Constraint(dateIsReal))
      .verifying(Constraint(checkDateOnBefore))

  val leavingMapping = mapping(
    "leavingDate" -> mapping(
      "day" -> optional(text),
      "month" -> optional(text),
      "year" -> optional(text)
    )(GmpDate.apply)(GmpDate.unapply),
    "leaving" -> optional(text)
  )(Leaving.apply)(Leaving.unapply)

  def revaluationForm(session: GmpSession) = Form(
    mapping(
      "leaving" -> leavingMapping,
      "revaluationDate" -> revaluationDateMapping
    )(RevaluationDate.apply)(RevaluationDate.unapply)
      .verifying(revaluationDateConstraint(session))
  )
}

