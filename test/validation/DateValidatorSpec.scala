/*
 * Copyright 2019 HM Revenue & Customs
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

import org.scalatest.{FlatSpec, Matchers}

class DateValidatorSpec extends FlatSpec with Matchers {

  "Validate a date" should "pass with a valid date 1/1/2015" in {
    DateValidate.isValid("1/1/2015") should be(true)
  }

  it should "pass with a valid date 01/01/2015" in {
    DateValidate.isValid("01/01/2015") should be(true)
  }

  it should "fail with a date that doesn't exist 32/01/2015" in {
    DateValidate.isValid("32/01/2015") should be(false)
  }

  it should "fail with a leap year date that doesn't exist 29/02/2015" in {
    DateValidate.isValid("29/02/2015") should be(false)
  }

  it should "pass with a leap year date that does exist 29/02/2016" in {
    DateValidate.isValid("29/02/2016") should be(true)
  }

  it should "fail with a date outside the range before 1900 31/12/1899" in {
    DateValidate.isValid("31/12/1899") should be(false)
  }

  it should "fail with a date outside the ranfe after 2100 1/1/2100" in {
    DateValidate.isValid("1/1/2100") should be(false)
  }

  it should "fail with a leading space 31/1/2015" in {
    DateValidate.isValid(" 31/1/2015") should be(false)
  }

  it should "fail with a trailing space 31/1/2015" in {
    DateValidate.isValid("31/1/2015 ") should be(false)
  }

  it should "fail with a date with invalid characters aa/bb/cccc" in {
    DateValidate.isValid("aa/bb/cccc ") should be(false)
  }

  it should "fail with a date with invalid characters 01 01 2018" in {
    DateValidate.isValid("01 01 2018") should be(false)
  }

}
