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

import forms.EqualiseForm
import models.Equalise
import play.api.data.Form
import play.api.data.Forms.{mapping, number, optional}
import play.twirl.api.Html
import utils.GmpViewSpec
import views.ViewHelpers

class EqualiseSpec extends GmpViewSpec {
  lazy val gmpMain = app.injector.instanceOf[gmp_main]
  lazy val viewHelpers = app.injector.instanceOf[ViewHelpers]

  override def view: Html = new views.html.equalise(gmpMain, viewHelpers)(equaliseForm)

  val equaliseForm = Form(
    mapping(
      "equalise" -> optional(number).verifying(messages("gmp.error.equalise.error_message"), {_.isDefined})
    )(Equalise.apply)(Equalise.unapply)
  )
  "Equalise page" must {
    behave like pageWithTitle("Do you also want an opposite gender calculation? - Guaranteed Minimum Pension - GOV.UK")
    behave like pageWithHeader(messages("gmp.equalise_header"))
    behave like pageWithBackLink

    "have correct input labels with text" in {
      doc must haveInputLabelWithText("equalise-yes", messages("gmp.generic.yes"))
      doc must haveInputLabelWithText("equalise-no", messages("gmp.generic.no"))
    }

    "have correct legend with text" in {
      doc must haveLegendWithText(messages("gmp.equalise_header"))
    }

    "have a submit button text" in {
      doc must haveSubmitButton(messages("gmp.continue.button"))
    }
  }

}
