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
import models.MemberDetails
import play.api.data.Form
import play.api.data.Forms.{mapping, text}
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}
import play.api.i18n.Messages
import play.twirl.api.Html
import utils.GmpViewSpec
import validation.NinoValidate
import views.ViewHelpers

class MemberDetailsViewSpec extends GmpViewSpec{

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

  lazy val gmpMain = app.injector.instanceOf[gmp_main]
  lazy val viewHelpers = app.injector.instanceOf[ViewHelpers]

  override def view: Html = new views.html.member_details(gmpMain, viewHelpers)(form)
  //private val memberDetailsForm: Form[models.MemberDetails] = MemberDetailsForm.form

  def form()(implicit messages: Messages) = Form(
    mapping(
      "nino" -> text
        .verifying(ninoConstraint),
      "firstForename" -> text
        .verifying(messages("gmp.error.firstnameorinitial", messages("gmp.firstname")), _.length > 0)
        .verifying(messages("gmp.error.length", messages("gmp.lowercase.firstname"), MAX_LENGTH), _.length <= MAX_LENGTH)
        .verifying(messages("gmp.error.name.invalid", messages("gmp.lowercase.firstname")), x => x.length == 0 || x.matches(NAME_REGEX)),
      "surname" -> text
        .verifying(messages("gmp.error.member.lastname.mandatory"), x => x.length > 0)
        .verifying(messages("gmp.error.length", messages("gmp.lowercase.lastname"), MAX_LENGTH), x => x.length <= MAX_LENGTH)
        .verifying(messages("gmp.error.name.invalid", messages("gmp.lowercase.lastname")), x => x.length == 0 || x.matches(NAME_REGEX)))
    (MemberDetails.apply)(MemberDetails.unapply)
  )

  val MAX_LENGTH = 99
  val NAME_REGEX = "^[a-zA-Z][a-zA-z\\s|'|-]*$"
  val NINO_SUFFIX_REGEX = "[A-D]"
  val TEMP_NINO = "TN"

  val ninoConstraint : Constraint[String] = Constraint("constraints.nino") ({
    text =>
      val ninoText = text.replaceAll("\\s", "")
      if (ninoText.length == 0){
        Invalid(Seq(ValidationError(messages("gmp.error.member.nino.mandatory"))))
      }
      else if (ninoText.toUpperCase().startsWith(TEMP_NINO)){
        Invalid(Seq(ValidationError(messages("gmp.error.nino.temporary"))))
      }
      else if (!NinoValidate.isValid(ninoText.toUpperCase())){
        Invalid(Seq(ValidationError(messages("gmp.error.nino.invalid"))))
      }
      else if (!ninoText.takeRight(1).toUpperCase().matches(NINO_SUFFIX_REGEX)){
        Invalid(Seq(ValidationError(messages("gmp.error.nino.invalid"))))
      }
      else {
        Valid
      }

  })



}
