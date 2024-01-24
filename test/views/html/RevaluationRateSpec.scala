/*
 * Copyright 2024 HM Revenue & Customs
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

import forms.RevaluationRateForm
import models._
import play.api.mvc.MessagesControllerComponents
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.html.components._
import utils.GmpViewSpec
import views.ViewHelpers

abstract class RevaluationRateSpec extends GmpViewSpec {
  lazy val layout = app.injector.instanceOf[views.html.Layout]
  lazy val viewHelpers = app.injector.instanceOf[ViewHelpers]
  lazy val govukButton = app.injector.instanceOf[GovukButton]
  lazy val govukRadios = app.injector.instanceOf[GovukRadios]
  lazy val govukErrorSummary = app.injector.instanceOf[GovukErrorSummary]
  lazy val backLink = app.injector.instanceOf[GovukBackLink]

  override def view: Html = new views.html.revaluation_rate(layout, viewHelpers, govukRadios, govukButton, govukErrorSummary, backLink)(revaluationRateForm, session)

  lazy val mcc = app.injector.instanceOf[MessagesControllerComponents]

  lazy val revaluationRateForm = new RevaluationRateForm(mcc).revaluationRateForm


   val session: GmpSession = GmpSession(MemberDetails("nino", "firstname", "surname"), "scon", "scenario",
    Some(GmpDate(Some("day"), Some("month"), Some("year"))), Some("rate"),
    Leaving(GmpDate(Some("day"), Some("month"), Some("year")), Some("leaving")), Some(1))

}

class RevaluationRatePaySpaSurSpec extends RevaluationRateSpec {
  override val session: GmpSession = GmpSession(MemberDetails("nino", "firstname", "surname"), "scon", CalculationType.SPA,
    Some(GmpDate(Some("day"), Some("month"), Some("year"))), Some("rate"),
    Leaving(GmpDate(Some("day"), Some("month"), Some("year")), Some(Leaving.YES_AFTER)), Some(1))

  "RevaluationRateRatePaySpaSur page" must {
    behave like pageWithTitle(messages("gmp.revaluation_rate.header"))
    behave like pageWithHeader(messages("gmp.revaluation_rate.header"))
    behave like pageWithNewBackLink()

    "have correct input labels with text" in {
      doc must haveInputLabelWithText("rateType-2", messages("gmp.revaluation_rate.rate_held_by_hmrc"))
      doc must haveInputLabelWithText("rateType", messages("gmp.revaluation_rate.fixed"))
      doc must haveInputLabelWithText("rateType-3", messages("gmp.revaluation_rate.s148"))
      doc must haveLegendWithText(messages("gmp.revaluation_rate.header"))
    }

    "have a submit button text" in {
      doc must haveSubmitButton(messages("gmp.continue.button"))
    }
  }
}

class RevaluationRateRevaSpec extends RevaluationRateSpec {
  override val session: GmpSession = GmpSession(MemberDetails("nino", "firstname", "surname"), "scon", CalculationType.REVALUATION,
    Some(GmpDate(Some("12"), Some("12"), Some("2010"))), Some("rate"),
    Leaving(GmpDate(Some("12"), Some("12"), Some("2010")), Some("")), Some(1))

  "RevaluationRateReva page" must {
    behave like pageWithHeader(messages("gmp.revaluation_rate.header"))

    "have correct input labels with text" in {
      doc must haveInputLabelWithText("rateType-2", messages("gmp.revaluation_rate.rate_held_by_hmrc"))
      doc must haveInputLabelWithText("rateType", messages("gmp.revaluation_rate.fixed"))
      doc must haveInputLabelWithText("rateType-4", messages("gmp.revaluation_rate.s148"))
      doc must haveInputLabelWithText("rateType-3", messages("gmp.revaluation_rate.limited"))
      doc must haveLegendWithText(messages("gmp.revaluation_rate.header"))
    }
  }
}
