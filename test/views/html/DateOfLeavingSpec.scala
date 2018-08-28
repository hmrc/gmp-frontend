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

import forms.DateOfLeavingForm
import models.CalculationType
import play.api.data.Form
import play.twirl.api.Html
import utils.GmpViewSpec

abstract class DateOfLeavingSpec extends GmpViewSpec {
  override def view: Html = views.html.dateofleaving(dateOfLeavingForm, scenario)

  val dateOfLeavingForm: Form[models.Leaving] = DateOfLeavingForm.dateOfLeavingForm
  val scenario = CalculationType.DOL
}

class DateOfLeavingScenarioDolSpec extends DateOfLeavingSpec {

  "DateOfLeavingScenarioDol page" must {
    behave like pageWithTitle(messages("gmp.leaving.title"))
    behave like pageWithHeader(messages("gmp.leaving.dol.question"))
    behave like pageWithBackLink

    "have correct input labels and legend with text" in {
      doc must haveInputLabelWithText("leaving-yes-before", messages("gmp.generic.yes"))
      doc must haveInputLabelWithText("leaving-yes-after", messages("gmp.generic.no"))
      doc must haveLegendWithText(messages("gmp.leaving.dol.question"))
    }

    "have correct span with text" in {
      doc must haveSpanWithText(messages("gmp.date.header_text"))
    }

    "have correct paragraph with text" in {
      doc must haveParagraphWithText(messages("gmp.date.example"))
    }

    "have correct input labels with text" in {
      doc must haveInputLabelWithText("leavingDate_day", messages("gmp.day"))
      doc must haveInputLabelWithText("leavingDate_month", messages("gmp.month"))
      doc must haveInputLabelWithText("leavingDate_year", messages("gmp.year"))
    }

    "have a submit button text" in {
      doc must haveSubmitButton(messages("gmp.continue.button"))
    }
  }
}

class DateOfLeavingScenarioSpaPayRevSpec extends DateOfLeavingSpec {

  override val scenario = CalculationType.SPA

  "DateOfLeavingScenarioSpaPayRev page" must {
    behave like pageWithHeader(messages("gmp.other.dol.left.question"))

    "have correct input labels and legend with text" in {
      doc must haveInputLabelWithText("leaving-no", messages("gmp.dol.threequestions.no"))
      doc must haveInputLabelWithText("leaving-yes-before", messages("gmp.dol.threequestions.before2016"))
      doc must haveInputLabelWithText("leaving-yes-after", messages("gmp.dol.threequestions.after2016"))
      doc must haveLegendWithText(messages("gmp.other.dol.left.question"))
    }
  }

}

class DateOfLeavingScenarioSurvivorSpec extends DateOfLeavingSpec {

  override val scenario = CalculationType.SURVIVOR

  "DateOfLeavingScenarioSurvivor page" must {
    behave like pageWithHeader(messages("gmp.survivor.dol.question"))

    "have correct input labels and legend with text" in {
      doc must haveInputLabelWithText("leaving-no", messages("gmp.dol.threequestions.no.survivor"))
      doc must haveInputLabelWithText("leaving-yes-before", messages("gmp.dol.threequestions.before2016"))
      doc must haveInputLabelWithText("leaving-yes-after", messages("gmp.dol.threequestions.after2016"))
      doc must haveLegendWithText(messages("gmp.survivor.dol.question"))
    }
  }
}