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

package forms

import com.google.inject.Singleton
import models.MemberDetails
import play.api.Play
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}
import play.api.i18n.{Messages, MessagesApi, MessagesImpl}
import play.api.mvc.MessagesControllerComponents
import validation.NinoValidate

@Singleton
class BaseMemberDetailsForm {

  val messagesControllerComponents = Play.current.injector.instanceOf[MessagesControllerComponents]
  val messagesApi =  Play.current.injector.instanceOf[MessagesApi]

  implicit lazy val messages: Messages = MessagesImpl(messagesControllerComponents.langs.availables.head, messagesApi)


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

}
case object MemberDetailsForm extends BaseMemberDetailsForm