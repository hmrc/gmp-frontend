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

package validation

import helpers.RandomNino
import org.scalatest.{WordSpec, Matchers}
import validation.NinoValidate

class NinoValidatorSpec extends WordSpec with Matchers {

  "The validation of a nino" should {

    "pass with valid NINO" in {validateNino(RandomNino.generate) should equal(true)}
    "fail with empty string" in { validateNino("") should equal (false) }
    "fail with only space" in { validateNino("    ") should equal (false) }
    "fail with total garbage" in {
      validateNino("XXX") should equal (false)
      validateNino("werionownadefwe") should equal (false)
      validateNino("@£%!)(*&^") should equal (false)
      validateNino("123456") should equal (false)
    }
    "fail with only one starting letter" in {
      validateNino("A123456C") should equal (false)
      validateNino("A1234567C") should equal (false)
    }
    "fail with three starting letters" in {
      validateNino("ABC12345C") should equal (false)
      validateNino("ABC123456C") should equal (false)
    }
    "fail with lowercase letters" in {
      validateNino("ab123456c") should equal (false)
    }
    "fail with less than 6 middle digits" in { validateNino("AB12345C") should equal (false) }
    "fail with more than 6 middle digits" in { validateNino("AB1234567C") should equal (false) }

    "fail if we start with invalid characters" in {

      val invalidPrefixes = List("BG", "GB", "NK", "KN", "TN", "NT", "ZZ", "CC")
      for (v <- invalidPrefixes) {
        validateNino(v + "123456C") should equal (false)
      }
    }

    "fail if the second letter O" in {
      validateNino("AO123456C") should equal (false)
    }

    "fail if the suffix is E" in {
      validateNino("AB123456E") should equal (false)
    }

    "fail with missing suffix" in {
      validateNino("AB123456") should equal (false)
    }
  }

  def validateNino(nino: String) = NinoValidate.isValid(nino)

}