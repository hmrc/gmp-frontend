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

import models.CalculationType
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.data.FormError
import play.api.libs.json.Json
import forms.ScenarioForm._

class ScenarioFormSpec extends PlaySpec with MockitoSugar{

  "Calculation Reason Form" must {

    "be valid when passed a calculation reason" in {

      val calculationReason = Json.toJson(CalculationType(Some("1")))
      val calculationReasonResult = scenarioForm.bind(calculationReason)

      assert(calculationReasonResult.errors.size == 0)
      assert(!calculationReasonResult.errors.contains(FormError("calcType",List("gmp.error.reason.mandatory"))))

    }

    "contain correct error" in {
      val calculationReasonResult = scenarioForm.bind(Map[String, String]())

      assert(calculationReasonResult.errors.size == 1)
      assert(calculationReasonResult.errors.contains(FormError("calcType",List("gmp.error.reason.mandatory"))))
    }
  }



}
