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

import models.PensionDetails
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.Messages
import validation.SconValidate
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

object PensionDetailsForm {

  val pensionDetailsForm = Form(
    mapping(
      "scon" -> text
        .verifying(Messages("gmp.error.mandatory", Messages("gmp.scon")), x => x.length != 0)
        .verifying(Messages("gmp.error.scon.invalid"), x => x.length == 0 || SconValidate.isValid(x))

    )(customApply)(customUnapply)
  )

  def customUnapply (req:PensionDetails) = {
    Some(req.scon)
  }
  def customApply(scon: String) = {
    new PensionDetails(scon)
  }
}
