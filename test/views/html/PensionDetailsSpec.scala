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

package views.html

import forms.PensionDetailsForm
import models.PensionDetails
import play.api.data.Form
import play.api.data.Forms.{mapping, text}
import play.twirl.api.Html
import utils.GmpViewSpec
import validation.SconValidate

class PensionDetailsSpec extends GmpViewSpec {
  override def view: Html = views.html.pension_details(pensionDetailsForm)

 // private val pensionDetailsForm: Form[models.PensionDetails] = PensionDetailsForm.pensionDetailsForm

  def pensionDetailsForm = Form(
    mapping(
      "scon" -> text
        .verifying(messages("gmp.error.mandatory.new"), x => x.length != 0)
        .verifying(messages("gmp.error.scon.invalid"), x => x.length == 0 || SconValidate.isValid(x))

    )(customApply)(customUnapply)
  )

  def customUnapply (req:PensionDetails) = {
    Some(req.scon)
  }
  def customApply(scon: String) = {
    new PensionDetails(scon)
  }

  "PensionDetails page" must {
    behave like pageWithTitle(messages("gmp.pension_details.header"))
    behave like pageWithHeader(messages("gmp.pension_details.header"))


    "have a URL text" in {
      doc must haveLinkWithText(messages("gmp.contact.hmrc"))
    }


    "have a submit button text" in {
      doc must haveSubmitButton(messages("gmp.continue.button"))
    }
  }
}
