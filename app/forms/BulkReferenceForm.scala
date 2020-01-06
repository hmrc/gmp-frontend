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

import com.google.inject.{Inject, Singleton}
import models.BulkReference
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.emailaddress.EmailAddress

@Singleton
class BaseBulkReferenceForm @Inject()(messages: Messages){

  val MAX_REFERENCE_LENGTH: Int = 99
  val CHARS_ALLOWED = "^[\\s,a-zA-Z0-9_-]*$"
  val emailConstraintRegex = "^((?:[a-zA-Z][a-zA-Z0-9_]*))(.)((?:[a-zA-Z][a-zA-Z0-9_]*))*$"
  val WHITE_SPACES = ".*\\s.*"

  val emailConstraint : Constraint[String] = Constraint("constraints.email") ({
    text =>
      if (text.trim.length == 0){
        Invalid(Seq(ValidationError(messages("gmp.error.mandatory.an", messages("gmp.email")))))
      }
      else if (!EmailAddress.isValid(text.trim.toUpperCase())){
        Invalid(Seq(ValidationError(messages("gmp.error.email.invalid"))))
      }
      else if(text.trim matches emailConstraintRegex){
        Invalid(Seq(ValidationError(messages("gmp.error.email.invalid"))))
      }
      else {
        Valid
      }
  })

  val bulkReferenceForm = Form(
    mapping(
      "email" -> text.verifying(emailConstraint),
      "reference" -> text
        .verifying(messages("gmp.error.mandatory", messages("gmp.reference")), x => x.trim.length != 0)
        .verifying(messages("gmp.error.csv.member_ref.length.invalid", messages("gmp.reference")), x => x.trim.length <= MAX_REFERENCE_LENGTH)
        .verifying(messages("gmp.error.csv.member_ref.character.invalid", messages("gmp.reference")), x => x.trim.matches(CHARS_ALLOWED))
        .verifying(messages("gmp.error.csv.member_ref.spaces.invalid", messages("gmp.reference")), x => !(x.trim matches WHITE_SPACES))
    )(BulkReference.apply)(BulkReference.unapply)
  )
}



case object BulkReferenceForm extends BaseBulkReferenceForm( {
  new GuiceApplicationBuilder().injector().instanceOf[Messages]
}
)