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

package views.html

import forms.RevaluationForm
import play.api.data.Form
import play.twirl.api.Html
import utils.GmpViewSpec

class RevaluationSpec extends GmpViewSpec{
  override def view: Html = views.html.revaluation(revaluationForm)
  private val revaluationForm: Form[models.RevaluationDate] = RevaluationForm.revaluationForm

  "Revaluation page" must {
    behave like pageWithTitle(messages("gmp.revaluation.title"))
    behave like pageWithHeader(messages("gmp.revaluation.question"))
    behave like pageWithBackLink

    "have correct div" in {
      doc must haveDivWithText(messages("gmp.revaluation.info"))
    }

    "have a paragraph with text" in {
      doc must haveParagraphWithText(messages("gmp.date.example"))
    }

    "have correct input labels with text" in {
      doc must haveParagraphWithText(messages("gmp.date.example"))
      doc must haveInputLabelWithText("revaluationDate_day", messages("gmp.day"))
      doc must haveInputLabelWithText("revaluationDate_month", messages("gmp.month"))
      doc must haveInputLabelWithText("revaluationDate_year", messages("gmp.year"))
    }

    "have a submit button text" in {
      doc must haveSubmitButton(messages("gmp.continue.button"))
    }
  }
}
