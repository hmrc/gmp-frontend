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

import forms.MemberDetailsForm
import play.api.data.Form
import play.twirl.api.Html
import utils.GmpViewSpec

class MemberDetailsViewSpec extends GmpViewSpec{

  override def view: Html = views.html.member_details(memberDetailsForm)
  private val memberDetailsForm: Form[models.MemberDetails] = MemberDetailsForm.form

  "MemberDetails page " must {
    behave like pageWithTitle(messages("gmp.member_details.header"))
    behave like pageWithHeader(messages("gmp.member_details.header"))
    behave like pageWithBackLink

    "have correct input labels" in {
      doc must haveInputLabelWithText("nino", messages("gmp.nino") + " " + messages("gmp.nino.hint"))
      doc must haveInputLabelWithText("firstForename", messages("gmp.firstname"))
      doc must haveInputLabelWithText("surname", messages("gmp.lastname"))
    }

    "have a continue button" in {
      doc must haveSubmitButton(messages("gmp.continue.button"))
    }
  }

}
