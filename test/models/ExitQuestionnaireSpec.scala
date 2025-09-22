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

class ExitQuestionnaireSpec extends AnyWordSpec with Matchers {
  
  "ExitQuestionnaire Json formats" should {
    "serialise ExitQuestionnaire to Json" in {
      val exitQuestionnaire = ExitQuestionnaire(Some("easy"), Some("very_easy"), None, Some("John Mcgin"), Some("johnMcgin@aol.com"), None)
      val json = Json.toJson(exitQuestionnaire)
      (json \ "serviceDifficulty").asOpt[String] shouldBe Some("easy")
      (json \ "serviceFeel").asOpt[String] shouldBe Some("very_easy")
      (json \ "comments").asOpt[String] shouldBe None
      (json \ "fullName").asOpt[String] shouldBe Some("John Mcgin")
      (json \ "email").asOpt[String] shouldBe Some("johnMcgin@aol.com")
      (json \ "phoneNumber").asOpt[String] shouldBe None
    }
    
    "deserialise Json to ExitQuestionnaire" in {
      val json = Json.obj("serviceDifficulty" -> Some("easy"), "serviceFeel" -> Some("very_easy"), "fullName" -> Some("John Mcgin"), "email" -> Some("johnMcgin@aol.com"))
      val exitQuestionnaire = json.as[ExitQuestionnaire]
      exitQuestionnaire shouldBe ExitQuestionnaire(Some("easy"), Some("very_easy"), None, Some("John Mcgin"), Some("johnMcgin@aol.com"), None)
    }
  }
}
