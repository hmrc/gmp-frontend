/*
 * Copyright 2022 HM Revenue & Customs
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

import models.CalculationType
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import org.scalatestplus.play.PlaySpec
import play.api.data.FormError
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.api.libs.json.Json
import play.api.mvc.MessagesControllerComponents

class ScenarioFormSpec extends PlaySpec with GuiceOneAppPerSuite with MockitoSugar {

  implicit lazy val messagesAPI=app.injector.instanceOf[MessagesApi]
  implicit lazy val messagesProvider=MessagesImpl(Lang("en"), messagesAPI)
  lazy val mcc = app.injector.instanceOf[MessagesControllerComponents]
  lazy val scenarioForm = new ScenarioForm(mcc).scenarioForm
  val fromJsonMaxChars: Int = 102400

  "Calculation Reason Form" must {

    "be valid when passed a calculation reason" in {

      val calculationReason = Json.toJson(CalculationType(Some("1")))
      val calculationReasonResult = scenarioForm.bind(calculationReason, fromJsonMaxChars)

      assert(calculationReasonResult.errors.size == 0)
      assert(!calculationReasonResult.errors.contains(FormError("calcType",List("gmp.error.scenario.mandatory"))))

    }

    "contain correct error" in {
      val calculationReasonResult = scenarioForm.bind(Map[String, String]())

      assert(calculationReasonResult.errors.size == 1)
      assert(calculationReasonResult.errors.contains(FormError("calcType",List(Messages("gmp.error.scenario.mandatory")))))
    }

    "does not accept invalid format" in  {
      val calculationReason = Json.toJson(CalculationType(Some("%&20!")))
      val calculationReasonResult = scenarioForm.bind(calculationReason, fromJsonMaxChars)
      assert(calculationReasonResult.errors.size == 1)
      assert(calculationReasonResult.errors.contains(FormError("calcType",List(Messages("gmp.error.scenario.mandatory")))))
    }
  }



}
