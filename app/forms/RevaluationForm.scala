/*
 * Copyright 2020 HM Revenue & Customs
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
import models.{GmpDate, Leaving, RevaluationDate}
import play.api.Play
import play.api.Play.current
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}
import play.api.i18n.{Messages, MessagesApi, MessagesImpl}
import play.api.i18n.Messages.Implicits._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.MessagesControllerComponents

@Singleton
class BaseRevaluationForm {

  val messagesControllerComponents = Play.current.injector.instanceOf[MessagesControllerComponents]
  val messagesApi =  Play.current.injector.instanceOf[MessagesApi]

  implicit lazy val messages: Messages = MessagesImpl(messagesControllerComponents.langs.availables.head, messagesApi)


  val YEAR_FIELD_LENGTH: Int = 4

  def mandatoryDate(date: GmpDate): Boolean = {
    date.day.isDefined || date.month.isDefined || date.year.isDefined
  }

  val revaluationDateConstraint: Constraint[RevaluationDate] = Constraint("revaluationDate")({
    revaluationDate => {
      val errors =
        if (revaluationDate.leaving.leaving.isDefined &&
            revaluationDate.leaving.leaving.get.equals(Leaving.NO) &&
            !revaluationDate.revaluationDate.isOnOrAfter06042016)
        {
          Seq(ValidationError(messages("gmp.error.revaluation_pre2016_not_left"), "revaluationDate")) // 2016
        }
        else if (revaluationDate.revaluationDate.isBefore(revaluationDate.leaving.leavingDate) &&
          revaluationDate.leaving.leaving != Some("no")) {
          Seq(ValidationError(Messages("gmp.error.revaluation_before_leaving", revaluationDate.leaving.leavingDate.getAsText), "revaluationDate"))
        }
        else if (revaluationDate.leaving.leaving.isDefined &&
        revaluationDate.leaving.leaving.get.equals(Leaving.YES_BEFORE) &&
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

  val revaluationDateMapping = mapping(
      "day" -> optional(text),
      "month" -> optional(text),
      "year" -> optional(text)
    )(GmpDate.apply)(GmpDate.unapply)
      .verifying(Messages("gmp.error.reval_date.mandatory"), x => mandatoryDate(x))
      .verifying(Messages("gmp.error.date.invalid"), x => checkValidDate(x))
      .verifying(Messages("gmp.error.reval_date.to"), x => checkDateOnOBeforeGMPEnd(x)
  )

  val leavingMapping = mapping(
    "leavingDate" -> mapping(
      "day" -> optional(text),
      "month" -> optional(text),
      "year" -> optional(text)
    )(GmpDate.apply)(GmpDate.unapply),
    "leaving" -> optional(text)
  )(Leaving.apply)(Leaving.unapply)

  val revaluationForm = Form(
    mapping(
      "leaving" -> leavingMapping,
      "revaluationDate" -> revaluationDateMapping
    )(RevaluationDate.apply)(RevaluationDate.unapply)
      .verifying(revaluationDateConstraint)
  )
}

case object RevaluationForm extends BaseRevaluationForm