/*
 * Copyright 2016 HM Revenue & Customs
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

import models.BulkReference
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.{Valid, ValidationError, Invalid, Constraint}
import play.api.i18n.Messages
import uk.gov.hmrc.emailaddress.EmailAddress

object BulkReferenceForm {
  val MAX_REFERENCE_LENGTH: Int = 40
  val CHARS_ALLOWED = "^[a-zA-Z0-9_-]*$"
  val emailConstraintRegex = "^((?:[a-zA-Z][a-zA-Z0-9_]*))(.)((?:[a-zA-Z][a-zA-Z0-9_]*))*$"

  /*val emailConstraint : Constraint[String] = Constraint("constraints.email") ({
    text =>
      if (text.trim.length == 0){
        Invalid(Seq(ValidationError(Messages("gmp.error.mandatory.an", Messages("gmp.email")))))
      }
      else if (!EmailAddress.isValid(text.trim.toUpperCase())){
        Invalid(Seq(ValidationError(Messages("gmp.error.email.invalid"))))
      }
      else if(text.trim matches emailConstraintRegex){
        Invalid(Seq(ValidationError(Messages("gmp.error.email.invalid"))))
      }
      else {
        Valid
      }
  })*/

  // Temporary constraint which requires the email address to be empty
  val emailConstraint = Constraint[String]("constraints.email") { text =>
    text.trim.length match {
      case 0 => Valid
      case _ => Invalid(Seq(ValidationError("The email address is currently ignored")))
    }
  }

  val bulkReferenceForm = Form(
    mapping(
      "email" -> text.verifying(emailConstraint),
      "reference" -> text
        .verifying(Messages("gmp.error.mandatory", Messages("gmp.reference")), x => x.trim.length != 0)
        .verifying(Messages("gmp.error.invalid", Messages("gmp.reference")), x => x.trim.length < MAX_REFERENCE_LENGTH)
        .verifying(Messages("gmp.error.invalid", Messages("gmp.reference")), x => x.trim.matches(CHARS_ALLOWED))
    )(BulkReference.apply)(BulkReference.unapply)
  )
}
