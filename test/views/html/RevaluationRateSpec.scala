/*
 * Copyright 2021 HM Revenue & Customs
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

import forms.{DateOfLeavingForm, RevaluationRateForm}
import models._
import play.api.data.Form
import play.api.mvc.MessagesControllerComponents
import play.twirl.api.Html
import utils.GmpViewSpec
import views.ViewHelpers

abstract class RevaluationRateSpec extends GmpViewSpec {
  lazy val gmpMain = app.injector.instanceOf[gmp_main]
  lazy val viewHelpers = app.injector.instanceOf[ViewHelpers]

  override def view: Html = new views.html.revaluation_rate(gmpMain, viewHelpers)(revaluationRateForm, session)

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
    behave like pageWithBackLink

    "have correct input labels with text" in {
      doc must haveInputLabelWithText("rateType-hmrc", messages("gmp.revaluation_rate.rate_held_by_hmrc"))
      doc must haveInputLabelWithText("rateType-fixed", messages("gmp.revaluation_rate.fixed"))
      doc must haveInputLabelWithText("rateType-s148", messages("gmp.revaluation_rate.s148"))
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
      doc must haveInputLabelWithText("rateType-hmrc", messages("gmp.revaluation_rate.rate_held_by_hmrc"))
      doc must haveInputLabelWithText("rateType-fixed", messages("gmp.revaluation_rate.fixed"))
      doc must haveInputLabelWithText("rateType-s148", messages("gmp.revaluation_rate.s148"))
      doc must haveInputLabelWithText("rateType-limited", messages("gmp.revaluation_rate.limited"))
      doc must haveLegendWithText(messages("gmp.revaluation_rate.header"))
    }
  }
}
