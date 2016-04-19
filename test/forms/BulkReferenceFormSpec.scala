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

import forms.BulkReferenceForm._
import org.scalatestplus.play.PlaySpec
import play.api.data.FormError
import play.api.libs.json.Json

class BulkReferenceFormSpec extends PlaySpec {

  "BulkReferenceForm" must {
    "return no errors with valid data" in {

      val postData = Json.obj(
        "email" -> "dan@hmrc.com",
        "reference" -> "Reference"
      )
      val validatedForm = bulkReferenceForm.bind(postData)

      assert(validatedForm.errors.isEmpty)
    }

    "email" must {
      "return an error if email missing" in {

        val postData = Json.obj(
          "email" -> "",
          "reference" -> "Reference"
        )
        val validatedForm = bulkReferenceForm.bind(postData)

        assert(validatedForm.errors.contains(FormError("email", List("gmp.error.mandatory.an"))))
      }

      "return an error if email invalid" in {
        val postData = Json.obj(
          "email" -> "dantathmrcdotcom",
          "reference" -> "Reference"
        )
        val validatedForm = bulkReferenceForm.bind(postData)

        assert(validatedForm.errors.contains(FormError("email", List("gmp.error.email.invalid"))))
      }
    }

    "reference" must {
      "return an error if missing" in {
        val postData = Json.obj(
          "email" -> "dan@hmrc.com",
          "reference" -> ""
        )
        val validatedForm = bulkReferenceForm.bind(postData)

        assert(validatedForm.errors.contains(FormError("reference", List("gmp.error.mandatory"))))
      }

      "return an error if more than 40 chars" in {
        val postData = Json.obj(
          "email" -> "dan@hmrc.com",
          "reference" -> "ABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZ"
        )
        val validatedForm = bulkReferenceForm.bind(postData)

        assert(validatedForm.errors.contains(FormError("reference", List("gmp.error.invalid"))))
      }

      "return an error if contains special chars" in {
        val postData = Json.obj(
          "email" -> "dan@hmrc.com",
          "reference" -> "ABCDEFGHIJKLMNOPQRSTUVWXYZ*&^%$£"
        )
        val validatedForm = bulkReferenceForm.bind(postData)

        assert(validatedForm.errors.contains(FormError("reference", List("gmp.error.invalid"))))
      }
    }

    "email and reference" must {
      "return 2 errors if both missing" in {
        val postData = Json.obj(
          "email" -> "",
          "reference" -> ""
        )
        val validatedForm = bulkReferenceForm.bind(postData)

        assert(validatedForm.errors.size == 2)
      }
    }

  }
}
