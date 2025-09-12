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

class DashboardSpec extends AnyWordSpec with Matchers{

  "Dashboard Json formats" should {
    "serialise Dashboard to Json" in {
      val bulkPreviousRequest = BulkPreviousRequest("upload","reference", LocalDateTime.now().withNano(0),LocalDateTime.now().withNano(0))
      val dashboard = Dashboard(List(bulkPreviousRequest))
      val json = Json.toJson(dashboard)
      (json \ "recentBulkCalculations").as[List[BulkPreviousRequest]] shouldBe List(bulkPreviousRequest)
    }

    "deserialise Json to Dashboard" in {
      val bulkPreviousRequest = BulkPreviousRequest("upload", "reference", LocalDateTime.now().withNano(0), LocalDateTime.now().withNano(0))
      val json = Json.obj("recentBulkCalculations" -> List(bulkPreviousRequest))
      val dashboard = json.as[Dashboard]
      dashboard shouldBe Dashboard(List(bulkPreviousRequest))
    }
  }
}
