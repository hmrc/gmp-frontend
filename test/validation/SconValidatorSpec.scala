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

package validation

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class SconValidatorSpec extends AnyFlatSpec with Matchers {

  "Validate a SCON" should "pass with a valid SCON S1301234T" in {
    SconValidate.isValid("S1301234T") should be(true)
  }

  it should "pass with a lower case SCON s1301234t" in {
    SconValidate.isValid("s1301234t") should be(false)
  }

  it should "fail with a leading space SCON S1301234T" in {
    SconValidate.isValid(" S1301234T") should be(false)
  }

  it should "fail should ail with a trailing space SCON S1301234T" in {
    SconValidate.isValid("11301234T ") should be(false)
  }

  it should "fail with an invalid first character SCON 11301234T" in {
    SconValidate.isValid("11301234T") should be(false)
  }

  it should "fail with an invalid number of digits SCON S130234T" in {
    SconValidate.isValid("S130234T") should be(false)
  }

  it should "fail with an invalid check digit SCON S1301234W" in {
    SconValidate.isValid("S1301234W") should be(false)
  }

  it should "fail with an invalid schema digit SCON S3301234W" in {
    SconValidate.isValid("S3301234W") should be(false)
  }

}
