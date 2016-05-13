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
import models.CalculationRequestLine
import org.scalatest.{Entry, Matchers, FlatSpec}
import play.api.i18n.Messages

/**
  * Created by stevenhobbs on 12/05/2016.
  */
class CsvLineValidatorSpec extends FlatSpec with Matchers {

   object CsvLine extends CalculationRequestLine(
    "S1301234T",            // SCON
    RandomNino.generate,    // NINO
    "Joe",                  // Forename
    "Bloggs",               // Surname
    Some("Ref1"),                 // Member reference
    Some(0),                      // Calc type - Date of leaving
    Some("20/01/2012"),           // Termination date
    Some("20/01/2014"),           // Revaluation date
    Some(1),                      // Revaluation rate - between 0 and 3
    Some(1)) {
  }

  "The CSV line validator should" should "report no errors in a valid line" in {

    val errors = CsvLineValidator.validateLine(CsvLine.toString)

    errors shouldBe None
  }

  it should "report a missing SCON" in {

    val errors = CsvLineValidator.validateLine(CsvLine.copy(scon = "").toString)

    errors shouldBe defined
    errors.get should contain(0 -> Messages("gmp.error.mandatory", Messages("gmp.scon")))
  }

}
