/*
 * Copyright 2023 HM Revenue & Customs
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

import helpers.BaseSpec
import models.upscan._
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.libs.json.{JsResultException, Json}

class UpscanCallbackSpec extends BaseSpec {

  "Upscan Callback" should {

    "be read as UpscanFailedCallback when status is FAILED" in {
      val json =
        s"""{
    "reference" : "ref1",
    "fileStatus" : "FAILED",
    "failureDetails": {
        "failureReason": "QUARANTINE",
        "message": "This file has a virus"
    }
}""".stripMargin
      val expectedResponse = UpscanFailedCallback("ref1", ErrorDetails("QUARANTINE", "This file has a virus"))
      Json.parse(json).as[UpscanFailedCallback] shouldBe expectedResponse
    }

    "Throw a JSError if discriminator type is not recognized" in {
      val json =
        s"""{
    "reference" : "ref1",
    "fileStatus" : "UNKNOWN",
    "failureDetails": {
        "failureReason": "QUARANTINE",
        "message": "This file has a virus"
    }
}""".stripMargin

      val result = intercept[JsResultException] {
        Json.parse(json).as[UpscanCallback] }
      result.errors.mkString.contains("Invalid type discriminator") shouldBe true

    }

    "Throw a JSError if discriminator type is missing" in {
      val json =
        s"""{
    "reference" : "ref1",
    "failureDetails": {
        "failureReason": "QUARANTINE",
        "message": "This file has a virus"
    }
}""".stripMargin

      val result = intercept[JsResultException] {
        Json.parse(json).as[UpscanCallback] }
      result.errors.mkString.contains("Missing type discriminator") shouldBe true
    }

    "Throw an error when download URL is not a valid URL" in {
      val json = """{
                       "reference" : "11370e18-6e24-453e-b45a-76d3e32ea33d",
                       "fileStatus" : "READY",
                       "downloadUrl" : "httpDownload",
                       "uploadDetails": {
                           "uploadTimestamp": "2018-04-24T09:30:00Z",
                           "checksum": "396f101dd52e8b2ace0dcf5ed09b1d1f030e608938510ce46e7a5c7a4e775100",
                           "fileName": "test.pdf",
                           "fileMimeType": "application/pdf"
                       }
                   }"""

      val result = intercept[JsResultException] {
        Json.parse(json).as[UpscanCallback] }
      result.errors.mkString.contains("error.expected.url") shouldBe true
    }
  }

}
