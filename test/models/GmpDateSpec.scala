/*
 * Copyright 2021 HM Revenue & Customs
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

import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec

class GmpDateSpec extends PlaySpec with MockitoSugar{

  "isOnOrAfter06042016" must {

    "return true when the date is after the 06 04 2016" in {

      val after = GmpDate(Some("10"),Some("04"),Some("2016"))
      after.isOnOrAfter06042016 must be(true)
    }

    "return true when the date is on the 06 04 2016" in {

      val after = GmpDate(Some("06"),Some("04"),Some("2016"))
      after.isOnOrAfter06042016 must be(true)
    }

    "return false when the date is before the 06 04 2016" in {

      val after = GmpDate(Some("05"),Some("04"),Some("2016"))
      after.isOnOrAfter06042016 must be(false)
    }

    "return false when not a valid date" in {

      val after = GmpDate(None,Some("04"),Some("2016"))
      after.isOnOrAfter06042016 must be(false)
    }

  }

  "isBefore05042046" must {

    "return true when the date is before the 05 04 2046" in {

      val before = GmpDate(Some("4"),Some("4"),Some("2046"))
      before.isBefore05042046 must be(true)
    }

    "return false when the date is on the 06 04 2046" in {

      val after = GmpDate(Some("6"),Some("4"),Some("2046"))
      after.isBefore05042046 must be(false)
    }

    "return false when not a valid date" in {

      val after = GmpDate(None,Some("04"),Some("2016"))
      after.isBefore05042046 must be(false)
    }

  }

  "getAsText" must {
    "return the date in the correct format" in {
      val date = GmpDate(Some("06"),Some("04"),Some("2016"))
      date.getAsText must be("06 April 2016")
    }

    "return empty string if no gmpdate" in {
      val date = GmpDate(None, None, None)
      date.getAsText must be ("")
    }
  }

  "isBefore" must {
    "return true if date1 before date2" in {
      val date = GmpDate(Some("06"),Some("04"),Some("2016"))
      date.isBefore(GmpDate(Some("07"),Some("04"),Some("2016"))) must be(true)
    }

    "return false if date1 after date2" in {
      val date = GmpDate(Some("06"),Some("04"),Some("2016"))
      date.isBefore(GmpDate(Some("05"),Some("04"),Some("2016"))) must be(false)
    }

    "return false if no date2" in {
      val date = GmpDate(Some("06"),Some("04"),Some("2016"))
      date.isBefore(GmpDate(None,None,None)) must be(false)
    }
  }

}
