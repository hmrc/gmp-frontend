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

class EqualiseSpec extends AnyWordSpec with Matchers {

  "Equalise Json formats" should {
    "serialise Equalise to Json" in {
      val equalise = Equalise(Some(2))
      val json = Json.toJson(equalise)
      (json \ "equalise").asOpt[Int] shouldBe Some(2)
    }

    "deserialise Json to Equalise" in {
      val json = Json.obj("equalise" -> Some(2))
      val equalise = json.as[Equalise]
      equalise shouldBe Equalise(Some(2))
    }
  }
}
