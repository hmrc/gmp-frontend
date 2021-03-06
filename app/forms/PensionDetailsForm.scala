/*
 * Copyright 2021 HM Revenue & Customs
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
import models.PensionDetails
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{Messages, MessagesImpl}
import play.api.mvc.MessagesControllerComponents
import validation.SconValidate

@Singleton
class PensionDetailsForm @Inject()(mcc: MessagesControllerComponents) {
  implicit lazy val messages: Messages = MessagesImpl(mcc.langs.availables.head, mcc.messagesApi)


  def pensionDetailsForm = Form(
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

