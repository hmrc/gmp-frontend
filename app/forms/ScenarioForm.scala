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
import models.CalculationType
import play.api.Play
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{Messages, MessagesApi, MessagesImpl}
import play.api.mvc.MessagesControllerComponents

@Singleton
class BaseScenarioForm {

  val messagesControllerComponents = Play.current.injector.instanceOf[MessagesControllerComponents]
  val messagesApi =  Play.current.injector.instanceOf[MessagesApi]

  implicit lazy val messages: Messages = MessagesImpl(messagesControllerComponents.langs.availables.head, messagesApi)


  val scenarioForm = Form(
    mapping(
      "calcType" -> optional(text).verifying(messages("gmp.error.scenario.mandatory"), {x => {x.isDefined && x.get.matches("[0-4]{1}")}})
    )(CalculationType.apply)(CalculationType.unapply)
  )

}

case object ScenarioForm extends BaseScenarioForm
