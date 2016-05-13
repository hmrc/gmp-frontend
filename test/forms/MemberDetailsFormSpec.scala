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

import forms.MemberDetailsForm._
import helpers.RandomNino
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatestplus.play.PlaySpec
import play.api.data.FormError
import play.api.libs.json.Json
import play.api.test.Helpers._

import scala.concurrent.Future

class MemberDetailsFormSpec extends PlaySpec {


  "Member details form" must {

    "return no errors with valid data" in {

      val postData = Json.obj(
        "nino" -> RandomNino.generate,
        "firstForename" -> "Bob",
        "surname" -> "Jones"
      )
      val validatedForm = form.bind(postData)

      assert(validatedForm.errors.isEmpty)
    }

    "nino" must {

      "return an error when empty" in {

        val postData = Json.obj(
          "nino" -> "",
          "firstForename" -> "Bob",
          "surname" -> "Jones"
        )
        val validatedForm = form.bind(postData)

        assert(validatedForm.errors.contains(FormError("nino", List("gmp.error.mandatory"))))
      }

      "return an error when invalid" in {

        val postData = Json.obj(
          "nino" -> "QQ322312B", // Invalid NINO
          "firstForename" -> "Bob",
          "surname" -> "Jones"
        )
        val validatedForm = form.bind(postData)

        assert(validatedForm.errors.contains(FormError("nino", List("gmp.error.nino.invalid"))))
        assert(!validatedForm.errors.contains(FormError("nino", List("gmp.error.mandatory"))))
      }

      "return an error when invalid suffix" in {

        val nino = s"${RandomNino.generate.substring(0,8)}Z"
        val postData = Json.obj(
          "nino" -> nino,
          "firstForename" -> "Bob",
          "surname" -> "Jones"
        )
        val validatedForm = form.bind(postData)

        assert(validatedForm.errors.contains(FormError("nino", List("gmp.error.nino.invalid"))))
        assert(validatedForm.errors.length == 1)
      }

      "return an error when missing suffix" in {

        val postData = Json.obj(
          "nino" -> RandomNino.generate.substring(0,8),
          "firstForename" -> "Bob",
          "surname" -> "Jones"
        )
        val validatedForm = form.bind(postData)

        assert(validatedForm.errors.contains(FormError("nino", List("gmp.error.nino.invalid"))))
        assert(validatedForm.errors.length == 1)
      }

      "return no errors when non capitals in nino" in {

        val postData = Json.obj(
          "nino" -> RandomNino.generate.toLowerCase,
          "firstForename" -> "Bob",
          "surname" -> "Jones"
        )
        val validatedForm = form.bind(postData)

        assert(validatedForm.errors.isEmpty)
      }

      "return an error when temporary nino" in {

        val postData = Json.obj(
          "nino" -> "TN000001A", //Invalid NINO
          "firstForename" -> "Bob",
          "surname" -> "Jones"
        )
        val validatedForm = form.bind(postData)

        assert(validatedForm.errors.contains(FormError("nino", List("gmp.error.nino.temporary"))))
        assert(validatedForm.errors.length == 1)
      }

      "return no errors when 13 character nino with spaces" in {
        val nino = RandomNino.generate.grouped(2).mkString(" ")

        val postData = Json.obj(
          "nino" -> nino,
          "firstForename" -> "Bob",
          "surname" -> "Jones"
        )
        val validatedForm = form.bind(postData)

        assert(validatedForm.errors.isEmpty)
      }

      "return no errors when 13 character nino with spaces, kerry special" in {

        val nino = RandomNino.generate.reverse.grouped(2).mkString(" ").reverse

        val postData = Json.obj(
          "nino" -> nino,
          "firstForename" -> "Bob",
          "surname" -> "Jones"
        )
        val validatedForm = form.bind(postData)

        assert(validatedForm.errors.isEmpty)
      }

      "return error when 13 character nino" in {
        val postData = Json.obj(
          "nino" -> "AA000000001BB", //Invalid NINO
          "firstForename" -> "Bob",
          "surname" -> "Jones"
        )
        val validatedForm = form.bind(postData)

        assert(validatedForm.errors.contains(FormError("nino", List("gmp.error.nino.invalid"))))
        assert(validatedForm.errors.length == 1)
      }

      "return error when 12 character nino" in {
        val postData = Json.obj(
          "nino" -> "AA000000001B", //Invalid NINO
          "firstForename" -> "Bob",
          "surname" -> "Jones"
        )
        val validatedForm = form.bind(postData)

        assert(validatedForm.errors.contains(FormError("nino", List("gmp.error.nino.invalid"))))
        assert(validatedForm.errors.length == 1)
      }
    }

    "firstForename" must {

      "return one error when empty" in {

        val postData = Json.obj(
          "nino" -> RandomNino.generate,
          "firstForename" -> "",
          "surname" -> "Jones"
        )
        val validatedForm = form.bind(postData)

        assert(validatedForm.errors.contains(FormError("firstForename", List("gmp.error.firstnameorinitial"))))
        assert(validatedForm.errors.length == 1)
      }

      "return no errors with length of MAX_LENGTH" in {

        val postData = Json.obj(
          "nino" -> RandomNino.generate,
          "firstForename" -> "a" * MAX_LENGTH,
          "surname" -> "Jones"
        )
        val validatedForm = form.bind(postData)

        assert(validatedForm.errors.isEmpty)
      }

      "return an error when too long" in {

        val postData = Json.obj(
          "nino" -> RandomNino.generate,
          "firstForename" -> "a" * (MAX_LENGTH + 1),
          "surname" -> "Jones"
        )
        val validatedForm = form.bind(postData)

        assert(validatedForm.errors.contains(FormError("firstForename", List("gmp.error.length"))))
      }

      "return an error when contains a digit" in {
        val postData = Json.obj(
          "nino" -> RandomNino.generate,
          "firstForename" -> "Bob2",
          "surname" -> "aaaa"
        )
        val validatedForm = form.bind(postData)

        assert(validatedForm.errors.contains(FormError("firstForename", List("gmp.error.name.invalid"))))
      }

      "allow apostrophes" in {
        val postData = Json.obj(
          "nino" -> RandomNino.generate,
          "firstForename" -> "Bo'ob",
          "surname" -> "aaaa"
        )
        val validatedForm = form.bind(postData)

        assert(!validatedForm.errors.contains(FormError("firstForename", List("gmp.error.name.invalid"))))
      }

      "disallow apostrophes as the first character" in {
        val postData = Json.obj(
          "nino" -> RandomNino.generate,
          "firstForename" -> "'Bob",
          "surname" -> "aaaa"
        )
        val validatedForm = form.bind(postData)

        assert(validatedForm.errors.contains(FormError("firstForename", List("gmp.error.name.invalid"))))
      }

      "allow hyphens" in {
        val postData = Json.obj(
          "nino" -> RandomNino.generate,
          "firstForename" -> "Bo-ob",
          "surname" -> "aaaa"
        )
        val validatedForm = form.bind(postData)

        assert(!validatedForm.errors.contains(FormError("firstForename", List("gmp.error.name.invalid"))))
      }

      "disallow hyphens as the first character" in {
        val postData = Json.obj(
          "nino" -> RandomNino.generate,
          "firstForename" -> "-Bob",
          "surname" -> "aaaa"
        )
        val validatedForm = form.bind(postData)

        assert(validatedForm.errors.contains(FormError("firstForename", List("gmp.error.name.invalid"))))
      }

      "disallow other special characters" in {
        val postData1 = Json.obj(
          "nino" -> RandomNino.generate,
          "firstForename" -> "Bo$ob",
          "surname" -> "aaaa"
        )
        val validatedForm1 = form.bind(postData1)

        val postData2 = Json.obj(
          "nino" -> RandomNino.generate,
          "firstForename" -> "Bo@ob",
          "surname" -> "aaaa"
        )
        val validatedForm2 = form.bind(postData2)

        assert(validatedForm1.errors.contains(FormError("firstForename", List("gmp.error.name.invalid"))))
        assert(validatedForm2.errors.contains(FormError("firstForename", List("gmp.error.name.invalid"))))
      }

      "allow whitespace" in {
        val postData = Json.obj(
          "nino" -> RandomNino.generate,
          "firstForename" -> "Bobby Rae",
          "surname" -> "'aaaa"
        )
        val validatedForm = form.bind(postData)

        assert(!validatedForm.errors.contains(FormError("firstForename", List("gmp.error.name.invalid"))))
      }
    }
  }

  "surname" must {

    "return one error when empty" in {

      val postData = Json.obj(
        "nino" -> RandomNino.generate,
        "firstForename" -> "Bob",
        "surname" -> ""
      )
      val validatedForm = form.bind(postData)

      assert(validatedForm.errors.contains(FormError("surname", List("gmp.error.mandatory"))))
      assert(validatedForm.errors.length == 1)
    }

    "return no errors with length of MAX_LENGTH" in {

      val postData = Json.obj(
        "nino" -> RandomNino.generate,
        "firstForename" -> "Bob",
        "surname" -> "a" * MAX_LENGTH
      )
      val validatedForm = form.bind(postData)

      assert(validatedForm.errors.isEmpty)
    }


    "return an error when too long" in {

      val postData = Json.obj(
        "nino" -> RandomNino.generate,
        "firstForename" -> "Bob",
        "surname" -> "a" * (MAX_LENGTH + 1)
      )
      val validatedForm = form.bind(postData)

      assert(validatedForm.errors.contains(FormError("surname", List("gmp.error.length"))))
    }

    "return an error when contains a digit" in {
      val postData = Json.obj(
        "nino" -> RandomNino.generate,
        "firstForename" -> "Bob",
        "surname" -> "a2"
      )
      val validatedForm = form.bind(postData)

      assert(validatedForm.errors.contains(FormError("surname", List("gmp.error.name.invalid"))))
    }

    "return an error when only one character" in {
      val postData = Json.obj(
        "nino" -> RandomNino.generate,
        "firstForename" -> "Bob",
        "surname" -> "a"
      )
      val validatedForm = form.bind(postData)

      assert(validatedForm.errors.contains(FormError("surname", List("gmp.error.surname.invalid"))))
      assert(validatedForm.errors.length == 1)
    }

    "allow apostrophes" in {
      val postData = Json.obj(
        "nino" -> RandomNino.generate,
        "firstForename" -> "Boob",
        "surname" -> "a'aaa"
      )
      val validatedForm = form.bind(postData)

      assert(!validatedForm.errors.contains(FormError("surname", List("gmp.error.name.invalid"))))
    }

    "disallow apostrophes as the first character" in {
      val postData = Json.obj(
        "nino" -> RandomNino.generate,
        "firstForename" -> "Bob",
        "surname" -> "'aaaa"
      )
      val validatedForm = form.bind(postData)

      assert(validatedForm.errors.contains(FormError("surname", List("gmp.error.name.invalid"))))
    }

    "allow hyphens" in {
      val postData = Json.obj(
        "nino" -> RandomNino.generate,
        "firstForename" -> "Bob",
        "surname" -> "aa-aa"
      )
      val validatedForm = form.bind(postData)

      assert(!validatedForm.errors.contains(FormError("surname", List("gmp.error.name.invalid"))))
    }

    "disallow hyphens as the first character" in {
      val postData = Json.obj(
        "nino" -> RandomNino.generate,
        "firstForename" -> "Bob",
        "surname" -> "-aaaa"
      )
      val validatedForm = form.bind(postData)

      assert(validatedForm.errors.contains(FormError("surname", List("gmp.error.name.invalid"))))
    }

    "allow whitespace" in {
      val postData = Json.obj(
        "nino" -> RandomNino.generate,
        "firstForename" -> "Bob",
        "surname" -> "aaaa aaaaa"
      )
      val validatedForm = form.bind(postData)

      assert(!validatedForm.errors.contains(FormError("surname", List("gmp.error.name.invalid"))))
    }

    "disallow other special characters" in {
      val postData1 = Json.obj(
        "nino" -> RandomNino.generate,
        "firstForename" -> "Boob",
        "surname" -> "aaa~a"
      )
      val validatedForm1 = form.bind(postData1)

      val postData2 = Json.obj(
        "nino" -> RandomNino.generate,
        "firstForename" -> "Bob",
        "surname" -> "a$aaa"
      )
      val validatedForm2 = form.bind(postData2)

      assert(validatedForm1.errors.contains(FormError("surname", List("gmp.error.name.invalid"))))
      assert(validatedForm2.errors.contains(FormError("surname", List("gmp.error.name.invalid"))))
    }
  }
}
