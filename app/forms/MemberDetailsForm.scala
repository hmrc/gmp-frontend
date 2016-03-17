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

import models.{Nino, MemberDetails}
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.{ValidationError, Invalid, Valid, Constraint}
import play.api.i18n.Messages


object MemberDetailsForm {

  val MAX_LENGTH = 99
  val NAME_REGEX = "^[a-zA-Z][a-zA-z\\s|'|-]*$"
  val NINO_SUFFIX_REGEX = "[A-D]"
  val TEMP_NINO = "TN"

  val ninoConstraint : Constraint[String] = Constraint("constraints.nino") ({
    text =>
      val ninoText = text.replaceAll("\\s", "")
      if (ninoText.length == 0){
        Invalid(Seq(ValidationError(Messages("gmp.error.mandatory", Messages("gmp.nino")))))
      }
      else if (ninoText.toUpperCase().startsWith(TEMP_NINO)){
        Invalid(Seq(ValidationError(Messages("gmp.error.nino.temporary"))))
      }
      else if (!Nino.isValid(ninoText.toUpperCase())){
        Invalid(Seq(ValidationError(Messages("gmp.error.nino.invalid"))))
      }
      else if (!ninoText.takeRight(1).toUpperCase().matches(NINO_SUFFIX_REGEX)){
        Invalid(Seq(ValidationError(Messages("gmp.error.nino.invalid"))))
      }
      else {
        Valid
      }
  })


  val form = Form(
    mapping(
      "nino" -> text
        .verifying(ninoConstraint),
      "firstForename" -> text
        .verifying(Messages("gmp.error.mandatory", Messages("gmp.lowercase.firstname")), _.length > 0)
        .verifying(Messages("gmp.error.length", Messages("gmp.firstname"), MAX_LENGTH), _.length <= MAX_LENGTH)
        .verifying(Messages("gmp.error.name.invalid", Messages("gmp.lowercase.firstname")), x => x.length == 0 || x.matches(NAME_REGEX)),
      "surname" -> text
        .verifying(Messages("gmp.error.mandatory", Messages("gmp.lowercase.lastname")), x => x.length > 0)
        .verifying(Messages("gmp.error.length", Messages("gmp.lastname"), MAX_LENGTH), x => x.length <= MAX_LENGTH)
        .verifying(Messages("gmp.error.name.invalid", Messages("gmp.lowercase.lastname")), x => x.length == 0 || (x.length > 1 && x.matches(NAME_REGEX))))
    (MemberDetails.apply)(MemberDetails.unapply)
  )

}
