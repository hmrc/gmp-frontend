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

class BulkReferenceSpec extends AnyWordSpec with Matchers {
  
  "BulkReference Json formats" should {
    "serialise BulkReference to Json" in {
      val bulkReference = BulkReference("test@gmail.com", "example")
      val json = Json.toJson(bulkReference)
      (json \ "email").as[String] shouldBe "test@gmail.com"
      (json \ "reference").as[String] shouldBe "example"
    }
    
    "deserialise Json to BulkReference" in {
      val json = Json.obj("email" -> "test@gmail.com", "reference" -> "example")
      val bulkReference = json.as[BulkReference]
      bulkReference shouldBe BulkReference("test@gmail.com", "example")
    }
    
  }
}
