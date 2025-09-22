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

package models

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json

import java.time.LocalDate

class CalculationPeriodSpec extends AnyWordSpec with Matchers {

  "CalculationPeriod Json formats" should {
    "serialise CalculationPeriod to Json" in {
      val calculationPeriod = CalculationPeriod(None, LocalDate.now(), "gmpTotal", "post88GMPTotal", 3, 1, None)
      val json = Json.toJson(calculationPeriod)

      (json \ "startDate").asOpt[LocalDate] shouldBe None
      (json \ "endDate").as[LocalDate] shouldBe LocalDate.now()
      (json \ "gmpTotal").as[String] shouldBe "gmpTotal"
      (json \ "post88GMPTotal").as[String] shouldBe "post88GMPTotal"
      (json \ "revaluationRate").as[Int] shouldBe 3
      (json \ "errorCode").as[Int] shouldBe 1
      (json \ "revalued").asOpt[Int] shouldBe None
      (json \ "dualCalcPost90TrueTotal").asOpt[String] shouldBe None
      (json \ "dualCalcPost90OppositeTotal").asOpt[String] shouldBe None
      (json \ "inflationProofBeyondDod").asOpt[Int] shouldBe None
      (json \ "contsAndEarnings").asOpt[List[ContributionsAndEarnings]] shouldBe None
    }

    "deserialise Json to CalculationPeriod" in {
      val json = Json.obj("endDate" -> LocalDate.now, "gmpTotal" -> "gmpTotal", "post88GMPTotal" -> "post88GMPTotal", "revaluationRate" -> 3, "errorCode" -> 1)
      val calculationPeriod = json.as[CalculationPeriod]
      calculationPeriod shouldBe CalculationPeriod(None, LocalDate.now(), "gmpTotal", "post88GMPTotal", 3, 1, None)
    }
  }

  "ContributionsAndEarnings Json formats" should {
    "serialise ContributionsAndEarnings to Json" in {
      val contributionsAndEarnings = ContributionsAndEarnings(2024, "example")
      val json = Json.toJson(contributionsAndEarnings)
      (json \ "taxYear").as[Int] shouldBe 2024
      (json \ "contEarnings").as[String] shouldBe "example"
    }

    "deserialise Json to ContributionAndEarnings" in {
      val json = Json.obj("taxYear" -> 2024, "contEarnings" -> "example")
      val contributionsAndEarnings = json.as[ContributionsAndEarnings]
      contributionsAndEarnings shouldBe ContributionsAndEarnings(2024, "example")
    }
  }

  "CalculationResponse Json formats" should {
    "serialise CalculationResponse to Json" in {
      val calculationPeriod = CalculationPeriod(None, LocalDate.now(), "gmpTotal", "post88GMPTotal", 3, 1, None)
      val calculationResponse = CalculationResponse("John", "CR000001C", "S2730032C", None, None, List(calculationPeriod), 1, None, None, None, false, 3)
      val json = Json.toJson(calculationResponse)
      (json \ "name").as[String] shouldBe "John"
      (json \ "nino").as[String] shouldBe "CR000001C"
      (json \ "scon").as[String] shouldBe "S2730032C"
      (json \ "revaluationRate").asOpt[String] shouldBe None
      (json \ "revaluationDate").asOpt[LocalDate] shouldBe None
      (json \ "calculationPeriods").as[List[CalculationPeriod]] shouldBe List(calculationPeriod)
      (json \ "globalErrorCode").as[Int] shouldBe 1
      (json \ "spaDate").asOpt[CalculationPeriod] shouldBe None
      (json \ "payableAgeDate").asOpt[LocalDate] shouldBe None
      (json \ "dateOfDeath").asOpt[LocalDate] shouldBe None
      (json \ "dualCalc").as[Boolean] shouldBe false
      (json \ "calcType").as[Int] shouldBe 3
    }

    "deserialise Json to CalculationResponse" in {
      val calculationPeriod = CalculationPeriod(None, LocalDate.now(), "gmpTotal", "post88GMPTotal", 3, 1, None)
      val json = Json.obj("name" -> "John", "nino" -> "CR000001C", "scon" -> "S2730032C", "calculationPeriods" -> List(calculationPeriod), "globalErrorCode" -> 1, "dualCalc" -> false, "calcType" -> 3)
      val calculationResponse = json.as[CalculationResponse]
      calculationResponse shouldBe CalculationResponse("John", "CR000001C", "S2730032C", None, None, List(calculationPeriod), 1, None, None, None, false, 3)
    }
  }

}
