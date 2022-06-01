/*
 * Copyright 2022 HM Revenue & Customs
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

import helpers.RandomNino
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class NinoSpec extends AnyWordSpec with Matchers {

  "Creating a Nino" should {
    "fail if the nino is not valid" in {
      an[IllegalArgumentException] should be thrownBy Nino("INVALID_NINO")
    }
  }

  "Formatting a Nino" should {
    "produce a formatted nino" in {
      val nino = RandomNino.generate
      val formattedNino = nino.grouped(2).mkString(" ")

      Nino(nino).formatted shouldBe formattedNino
    }
  }
}
