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
import play.api.i18n.Messages
import uk.gov.hmrc.emailaddress.EmailAddress

object BulkReferenceForm {
  val MAX_REFERENCE_LENGTH: Int = 40
  val CHARS_ALLOWED = "^[a-zA-Z0-9_-]*$"

  val bulkReferenceForm = Form(
    mapping(
      "email" -> text
        .verifying(Messages("gmp.error.mandatory", Messages("gmp.email")), x => x.length != 0)
        .verifying(Messages("gmp.error.email.invalid", Messages("gmp.email")), x => EmailAddress.isValid(x)),
      "reference" -> text
        .verifying(Messages("gmp.error.mandatory", Messages("gmp.reference")), x => x.length != 0)
        .verifying(Messages("gmp.error.invalid", Messages("gmp.reference")), x => x.length < MAX_REFERENCE_LENGTH)
        .verifying(Messages("gmp.error.invalid", Messages("gmp.reference")), x => x.matches(CHARS_ALLOWED))
    )(BulkReference.apply)(BulkReference.unapply)
  )
}
