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

import java.time.LocalDateTime



class CalculationRequestLineSpec extends AnyWordSpec with Matchers {

  "CalculationRequestLine Json format" should {
    "serialise CalculationRequestLine to JSON" in {
      val calculationRequestLine = CalculationRequestLine("S2730000B", "CR000001A", "Chris", "Samuels", None, None, None, None, None, 1)
      val json = Json.toJson(calculationRequestLine)
      (json \ "scon").as[String] shouldBe "S2730000B"
      (json \ "nino").as[String] shouldBe "CR000001A"
      (json \ "firstForename").as[String] shouldBe "Chris"
      (json \ "surname").as[String] shouldBe "Samuels"
      (json \ "memberReference").asOpt[String] shouldBe None
      (json \ "calcType").asOpt[Int] shouldBe None
      (json \ "terminationDate").asOpt[String] shouldBe None
      (json \ "reevaluationDate").asOpt[String] shouldBe None
      (json \ "reevaluationRate").asOpt[Int] shouldBe None
      (json \ "dualCalc").as[Int] shouldBe 1
      (json \ "memberIsInScheme").as[Boolean] shouldBe false

    }
  }

  "deseralise Json to CalculationRequestLine" in {
    val json = Json.obj("scon" -> "S2730000B", "nino" -> "CR000001A" , "firstForename" -> "Chris", "surname" -> "Samuels", "dualCalc" -> 1, "memberIsInScheme" -> false)
    val calculationRequestLine = json.as[CalculationRequestLine]
    calculationRequestLine shouldBe CalculationRequestLine("S2730000B", "CR000001A", "Chris", "Samuels", None, None, None, None, None, 1)
  }

  "BulkCalculationRequestLine Json format" should {
    "serialise BulkCalculationRequestLine to Json" in {
      val bulkCalculationRequestLine = BulkCalculationRequestLine(3, None, None, None)
      val json = Json.toJson(bulkCalculationRequestLine)
      (json \ "lineId").as[Int] shouldBe 3
      (json \ "validCalculationRequest").asOpt[CalculationRequestLine] shouldBe None
      (json \ "globalError").asOpt[String] shouldBe None
      (json \ "validationErrors").asOpt[Map[String,String]] shouldBe None
    }

    "deserialise Json to BulkCalculationRequestLine" in {
      val json = Json.obj("lineId" -> 3)
      val bulkCalculationRequestLine = json.as[BulkCalculationRequestLine]
      bulkCalculationRequestLine shouldBe BulkCalculationRequestLine(3, None, None, None)
    }
  }

  "BulkCalculationRequest Json format" should {
    "serialise BulkCalculationRequest to Json" in {
      val bulkCalculationRequestLine = BulkCalculationRequestLine(3, None, None, None)
      val bulkCalculationRequest = BulkCalculationRequest("upload", "test@gmail.com", "reference", List(bulkCalculationRequestLine), "", LocalDateTime.now().withNano(0))
      val json = Json.toJson(bulkCalculationRequest)
      (json \ "uploadReference").as[String] shouldBe "upload"
      (json \ "email").as[String] shouldBe "test@gmail.com"
      (json \ "reference").as[String] shouldBe "reference"
      (json \ "calculationRequests").as[List[BulkCalculationRequestLine]] shouldBe List(bulkCalculationRequestLine)
      (json \ "userId").as[String] shouldBe ""
      (json \ "timestamp").as[LocalDateTime] shouldBe LocalDateTime.now().withNano(0)
    }

    "deserialise Json to BulkCalculationRequest" in {
      val bulkCalculationRequestLine = BulkCalculationRequestLine(3, None, None, None)

      val json = Json.obj("uploadReference" -> "upload", "email" -> "test@gmail.com", "reference" -> "reference", "calculationRequests" -> List(bulkCalculationRequestLine), "userId" -> "", "timestamp" -> LocalDateTime.now().withNano(0))
      val bulkCalculationRequest = json.as[BulkCalculationRequest]
      bulkCalculationRequest shouldBe BulkCalculationRequest("upload", "test@gmail.com", "reference", List(bulkCalculationRequestLine), "", LocalDateTime.now().withNano(0))
    }
  }
}
