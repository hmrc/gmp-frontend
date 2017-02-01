/*
 * Copyright 2017 HM Revenue & Customs
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
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

object InflationProofForm {

  val YEAR_FIELD_LENGTH: Int = 4

  def dateMustBePresentIfRevaluationWanted(x: InflationProof): Boolean = {
    x.revaluate match {
      case Some("Yes") => x.revaluationDate.day.isDefined || x.revaluationDate.month.isDefined || x.revaluationDate.year.isDefined
      case _ => true
    }
  }

  val inflationProofForm = Form(
    mapping(
      "revaluationDate" -> mapping(
        "day" -> optional(text),
        "month" -> optional(text),
        "year" -> optional(text)
      )(GmpDate.apply)(GmpDate.unapply)
        .verifying(Messages("gmp.error.date.nonnumber"), x => checkForNumber(x.day) && checkForNumber(x.month) && checkForNumber(x.year))
        .verifying(Messages("gmp.error.day.invalid"), x => checkDayRange(x.day))
        .verifying(Messages("gmp.error.month.invalid"), x => checkMonthRange(x.month))
        .verifying(Messages("gmp.error.year.invalid.format"), x => checkYearLength(x.year))
      ,
      "revaluate" -> optional(text).verifying(Messages("gmp.error.reason.mandatory"),{_.isDefined})
    )(InflationProof.apply)(InflationProof.unapply)
      .verifying(Messages("gmp.error.reval_date.mandatory"), x => dateMustBePresentIfRevaluationWanted(x))
      .verifying(Messages("gmp.error.date.invalid"), x => checkValidDate(x.revaluationDate))
      .verifying(Messages("gmp.error.reval_date.from"), x => checkDateOnOrAfterGMPStart(x.revaluationDate))
      .verifying(Messages("gmp.error.reval_date.to"), x => checkDateOnOBeforeGMPEnd(x.revaluationDate))
  )

}
