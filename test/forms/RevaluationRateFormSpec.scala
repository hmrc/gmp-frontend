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

import models.RevaluationRate
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import org.scalatestplus.play.PlaySpec

import play.api.i18n.{Lang, MessagesApi, MessagesImpl}
import play.api.libs.json.Json
import play.api.mvc.MessagesControllerComponents

class RevaluationRateFormSpec extends PlaySpec with GuiceOneAppPerSuite with MockitoSugar {

  implicit lazy val messagesAPI: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit lazy val messagesProvider: MessagesImpl = MessagesImpl(Lang("en"), messagesAPI)
  lazy val mcc = app.injector.instanceOf[MessagesControllerComponents]
  lazy val revaluationRateForm = new RevaluationRateForm(mcc).revaluationRateForm
  val fromJsonMaxChars: Int = 102400

  "Revaluation Rate Form" must {
    "return no errors when valid values are entered" in {

      val revaluationFormResults = revaluationRateForm.bind(Json.toJson(RevaluationRate(Some(RevaluationRate.HMRC))), fromJsonMaxChars)

      assert(revaluationFormResults.errors.size == 0)

    }

    "return errors when value not chosed" in {

      val revaluationFormResults = revaluationRateForm.bind(Json.toJson(RevaluationRate(None)), fromJsonMaxChars)

      assert(revaluationFormResults.errors.size == 1)

    }

    "return error when not allowed type" in {
      val revaluationFormResults = revaluationRateForm.bind(Json.toJson(RevaluationRate(Some("~@"))), fromJsonMaxChars)

      assert(revaluationFormResults.errors.size == 1)
    }

  }
}
