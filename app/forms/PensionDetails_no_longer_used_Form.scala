/*
 * Copyright 2024 HM Revenue & Customs
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

import javax.inject.Inject
import models.PensionDetailsScon
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationResult}
import play.api.i18n.{Messages, MessagesImpl}
import play.api.mvc.MessagesControllerComponents
import validation.SconValidate

@Singleton
class PensionDetails_no_longer_used_Form @Inject()(mcc: MessagesControllerComponents) {
  implicit lazy val messages: Messages = MessagesImpl(mcc.langs.availables.head, mcc.messagesApi)

  def strip(scon: String): String = scon.replaceAll(" ", "")

  def validateScon(s: String): ValidationResult = {
    val simpleFormat = "^\\w{1}\\d{7}\\w{1}$"
    val stripped = strip(s)
    if(stripped.nonEmpty && !stripped.matches(simpleFormat)) {
      Invalid("error.invalid")
    } else if (stripped.nonEmpty && !SconValidate.isValid(stripped)) {
      Invalid("error.notRecognised")
    } else {
      Valid
    }
  }
  val validScon: Constraint[String] = Constraint(validateScon)
  def pensionDetailsForm: Form[PensionDetailsScon] = Form(
    mapping(
      "scon" -> nonEmptyText.transform[String](_.trim.toUpperCase, identity)
        .verifying(validScon)
    )(customApply)(customUnapply)
  )

  def customUnapply (req:PensionDetailsScon): Some[String] = {
    Some(strip(req.scon))
  }

  def customApply(scon: String): PensionDetailsScon = {
    new PensionDetailsScon(strip(scon))
  }
}

