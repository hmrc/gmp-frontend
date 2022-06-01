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

package views.html

import forms.{InflationProofForm, checkDateOnOBeforeGMPEnd, checkDateOnOrAfterGMPStart, checkDayRange, checkForNumber, checkMonthRange, checkValidDate, checkYearLength}
import models.{GmpDate, InflationProof}
import play.api.data.Form
import play.api.data.Forms.{mapping, optional, text}
import play.twirl.api.Html
import utils.GmpViewSpec
import views.ViewHelpers

class InflationProofSpec extends GmpViewSpec {
  lazy val gmpMain = app.injector.instanceOf[gmp_main]
  lazy val viewHelpers = app.injector.instanceOf[ViewHelpers]

  override def view: Html = new views.html.inflation_proof(gmpMain, viewHelpers)(inflationProofForm)

  val inflationProofForm = Form(
    mapping(
      "revaluationDate" -> mapping(
        "day" -> optional(text),
        "month" -> optional(text),
        "year" -> optional(text)
      )(GmpDate.apply)(GmpDate.unapply)
        .verifying(messages("gmp.error.date.nonnumber"), x => checkForNumber(x.day) && checkForNumber(x.month) && checkForNumber(x.year))
        .verifying(messages("gmp.error.day.invalid"), x => checkDayRange(x.day))
        .verifying(messages("gmp.error.month.invalid"), x => checkMonthRange(x.month))
        .verifying(messages("gmp.error.year.invalid.format"), x => checkYearLength(x.year))
      ,
      "revaluate" -> optional(text).verifying(messages("gmp.error.reason.mandatory"),{_.isDefined})
    )(InflationProof.apply)(InflationProof.unapply)
      .verifying(messages("gmp.error.reval_date.mandatory"), x => dateMustBePresentIfRevaluationWanted(x))

  )
  def dateMustBePresentIfRevaluationWanted(x: InflationProof): Boolean = {
    x.revaluate match {
      case Some("Yes") => x.revaluationDate.day.isDefined || x.revaluationDate.month.isDefined || x.revaluationDate.year.isDefined
      case _ => true
    }
  }
  "Inflation Proof page" must {
    behave like pageWithTitle(messages("gmp.inflation_proof.question"))
    behave like pageWithHeader(messages("gmp.inflation_proof.question"))
    behave like pageWithBackLink

    "have correct input labels with text" in {
      println(doc);
      doc must haveInputLabelWithText("revaluate-revaluation-date", messages("gmp.generic.yes"))
      doc must haveInputLabelWithText("revaluate-none", messages("gmp.generic.no"))
      doc must haveLegendWithText(messages("gmp.inflation_proof.question"))
    }

    "have correct span and a paragraph" in {
      doc must haveSpanWithText(messages("gmp.inflation_proofed"))
      doc must haveSpanWithText(messages("gmp.date.example"))
    }

    "have valid input labels" in {
      doc must haveInputLabelWithText("revaluationDate_day", messages("gmp.day"))
      doc must haveInputLabelWithText("revaluationDate_month", messages("gmp.month"))
      doc must haveInputLabelWithText("revaluationDate_year", messages("gmp.year"))
      doc must haveParagraphWithText(messages("gmp.inflationproof.subtext"))
    }

    "have a submit button text" in {
      doc must haveSubmitButton(messages("gmp.check_gmp.button"))
    }
  }

}
