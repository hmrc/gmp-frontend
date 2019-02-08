/*
 * Copyright 2019 HM Revenue & Customs
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

import forms.RevaluationRateForm._
import models.RevaluationRate
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.libs.json.Json

class RevaluationRateFormSpec extends PlaySpec with OneAppPerSuite with MockitoSugar {
  "Revaluation Rate Form" must {
    "return no errors when valid values are entered" in {

      val revaluationFormResults = revaluationRateForm.bind(Json.toJson(RevaluationRate(Some(RevaluationRate.HMRC))))

      assert(revaluationFormResults.errors.size == 0)

    }

    "return errors when value not chosed" in {

      val revaluationFormResults = revaluationRateForm.bind(Json.toJson(RevaluationRate(None)))

      assert(revaluationFormResults.errors.size == 1)

    }

    "return error when not allowed type" in {
      val revaluationFormResults = revaluationRateForm.bind(Json.toJson(RevaluationRate(Some("~@"))))

      assert(revaluationFormResults.errors.size == 1)
    }

  }
}
