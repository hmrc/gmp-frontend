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

import forms.BulkReferenceForm
import play.api.data.Form
import play.api.i18n.Messages
import play.twirl.api.Html
import utils.GmpViewSpec

class BulkReferenceSpec extends GmpViewSpec {

  "BulkReference page" must {

    behave like pageWithTitle(messages("gmp.bulk_reference.title"))
    behave like pageWithHeader(messages("gmp.bulk_reference.header"))
    behave like pageWithBackLink

    behave like pageWithButtonForm(
      "/guaranteed-minimum-pension/getting-results",
      messages("gmp.bulk_reference.button"))

    "display an input field for text entry" in {
      doc.getElementById("email") must not be null
      doc must haveInputLabelWithText("email", expectedText=s"${messages("gmp.email.address")} ${messages("gmp.bulk_reference.email_text")}")

      doc.getElementById("reference") must not be null
      doc must haveInputLabelWithText("reference", expectedText=s"${messages("gmp.reference.calcname")} ${messages("gmp.bulk_reference.reference_text")}")
    }

  }

  override def view: Html = views.html.bulk_reference(form)
  private val form: Form[models.BulkReference] = BulkReferenceForm.bulkReferenceForm

}
