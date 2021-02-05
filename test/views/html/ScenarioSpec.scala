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

import forms.ScenarioForm
import models.CalculationType
import play.api.data.Form
import play.api.data.Forms.{mapping, optional, text}
import play.twirl.api.Html
import utils.GmpViewSpec
import views.ViewHelpers

class ScenarioSpec extends GmpViewSpec{
  lazy val gmpMain = app.injector.instanceOf[gmp_main]
  lazy val viewHelpers = app.injector.instanceOf[ViewHelpers]

  override def view: Html = new views.html.scenario(gmpMain, viewHelpers)(scenarioForm)
 // private val pensionDetailsForm: Form[models.CalculationType] = ScenarioForm.scenarioForm

  val scenarioForm = Form(
    mapping(
      "calcType" -> optional(text).verifying(messages("gmp.error.scenario.mandatory"), {x => {x.isDefined && x.get.matches("[0-4]{1}")}})
    )(CalculationType.apply)(CalculationType.unapply)
  )

  "Scenario page" must {
    behave like pageWithTitle(messages("gmp.scenarios.title"))
    behave like pageWithHeader(messages("gmp.scenarios.title"))
    behave like pageWithBackLink

    "have correct input labels" in {
      doc must haveInputLabelWithText("calcType-payableage", "GMP payable age")
      doc must haveInputLabelWithText("calcType-spa", "State Pension age")
      doc must haveInputLabelWithText("calcType-survivor", "Survivor")
      doc must haveInputLabelWithText("calcType-leaving", "Leaving")
      doc must haveInputLabelWithText("calcType-specificdate", "GMP value on a specific date")
      doc must haveLegendWithText(messages("gmp.scenarios.title"))
    }

    "have a continue button" in {
      doc must haveSubmitButton(messages("gmp.continue.button"))
    }
  }

}
