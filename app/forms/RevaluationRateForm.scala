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

import com.google.inject.{Inject, Singleton}
import models.RevaluationRate
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{Messages, MessagesImpl}
import play.api.mvc.MessagesControllerComponents
@Singleton
class RevaluationRateForm @Inject()(mcc: MessagesControllerComponents) {
  implicit lazy val messages: Messages = MessagesImpl(mcc.langs.availables.head, mcc.messagesApi)

  val revaluationRateForm = Form(
    mapping(
      "rateType" -> optional(text).verifying(messages("gmp.error.revaluation.rate.error"), { x => x.isDefined &&
        List(RevaluationRate.FIXED, RevaluationRate.HMRC, RevaluationRate.LIMITED, RevaluationRate.S148).contains(x.get) })
    )(RevaluationRate.apply)(RevaluationRate.unapply)
  )

}
