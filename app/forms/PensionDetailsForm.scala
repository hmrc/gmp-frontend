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
import models.PensionDetails
import play.api.Play.current
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.inject.guice.GuiceApplicationBuilder
import validation.SconValidate

@Singleton
class BasePensionDetailsForm (messages: Messages){

  val pensionDetailsForm = Form(
    mapping(
      "scon" -> text
        .verifying(messages("gmp.error.mandatory.new"), x => x.length != 0)
        .verifying(messages("gmp.error.scon.invalid"), x => x.length == 0 || SconValidate.isValid(x))

    )(customApply)(customUnapply)
  )

  def customUnapply (req:PensionDetails) = {
    Some(req.scon)
  }
  def customApply(scon: String) = {
    new PensionDetails(scon)
  }
}
case object PensionDetailsForm extends BasePensionDetailsForm( {
  new GuiceApplicationBuilder().injector().instanceOf[Messages]
}
)