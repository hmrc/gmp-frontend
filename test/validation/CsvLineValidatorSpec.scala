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
import org.scalatestplus.play.OneAppPerSuite
import play.api.i18n.Messages
import services.BulkRequestCsvColumn

/**
  * Created by stevenhobbs on 12/05/2016.
  */
class CsvLineValidatorSpec extends FlatSpec with Matchers with OneAppPerSuite {

  object CsvLine extends CalculationRequestLine(
    "S1301234T", // SCON
    RandomNino.generate, // NINO
    "Joe", // Forename
    "Bloggs", // Surname
    Some("Ref1"), // Member reference
    Some(0), // Calc type - Date of leaving
    Some("20/01/2012"), // Termination date
    Some("20/01/2014"), // Revaluation date
    Some(1), // Revaluation rate - between 0 and 3
    Some(1)) {
  }

  "The CSV line validator should" should "report no errors in a valid line" in {

    val errors = CsvLineValidator.validateLine(CsvLine.toString)

    errors shouldBe None
  }

  it should "report a missing SCON" in {

    val errors = CsvLineValidator.validateLine(CsvLine.copy(scon = "").toString)

    errors shouldBe defined
    errors.get should contain(BulkRequestCsvColumn.SCON -> Messages("gmp.error.mandatory", Messages("gmp.scon")))
  }

  it should "report an invalid SCON" in {

    val errors = CsvLineValidator.validateLine(CsvLine.copy(scon = "S24300 12").toString)

    errors shouldBe defined
    errors.get should contain(BulkRequestCsvColumn.SCON -> Messages("gmp.error.scon.invalid"))
  }

  it should "report a missing NINO" in {

    val errors = CsvLineValidator.validateLine(CsvLine.copy(nino = "").toString)

    errors shouldBe defined
    errors.get should contain(BulkRequestCsvColumn.NINO -> Messages("gmp.error.mandatory", Messages("gmp.nino")))
  }

  it should "report an invalid NINO" in {
    val errors = CsvLineValidator.validateLine(CsvLine.copy(nino = "C E0 00 00 -A").toString)

    errors shouldBe defined
    errors.get should contain(BulkRequestCsvColumn.NINO -> Messages("gmp.error.nino.invalid"))
  }

  it should "report a missing first name" in {
    val errors = CsvLineValidator.validateLine(CsvLine.copy(firstForename = "").toString)

    errors shouldBe defined
    errors.get should contain(BulkRequestCsvColumn.FORENAME -> Messages("gmp.error.firstnameorinitial"))
  }

  it should "report a first name that is longer than 98 characters" in {
    val errors = CsvLineValidator.validateLine(CsvLine.copy(firstForename = "A" * 99).toString)

    errors shouldBe defined
    errors.get should contain(BulkRequestCsvColumn.FORENAME -> Messages("gmp.error.firstname.toolong"))
  }

  it should "not report a first name that contains valid characters" in {
    val errors = CsvLineValidator.validateLine(CsvLine.copy(firstForename = "This should be a 'valid-name'").toString)

    errors shouldBe empty
  }

  it should "report a first name that contains invalid characters" in {
    val errors = CsvLineValidator.validateLine(CsvLine.copy(firstForename = "Joe Bloggs 123%^&{}").toString)

    errors shouldBe defined
    errors.get should contain(BulkRequestCsvColumn.FORENAME -> Messages("gmp.error.name.invalid", Messages("gmp.lowercase.firstname")))
  }

  it should "report a missing last name" in {
    val errors = CsvLineValidator.validateLine(CsvLine.copy(surname = "").toString)

    errors shouldBe defined
    errors.get should contain(BulkRequestCsvColumn.SURNAME -> Messages("gmp.error.mandatory", Messages("gmp.lowercase.lastname")))
  }

  it should "report a last name that is too long" in {
    val errors = CsvLineValidator.validateLine(CsvLine.copy(surname = "A" * 99).toString)

    errors shouldBe defined
    errors.get should contain(BulkRequestCsvColumn.SURNAME -> Messages("gmp.error.lastname.toolong"))
  }

  it should "report a last name that only has one character" in {
    val errors = CsvLineValidator.validateLine(CsvLine.copy(surname = "A").toString)

    errors shouldBe defined
    errors.get should contain(BulkRequestCsvColumn.SURNAME -> Messages("gmp.error.surname.invalid"))
  }

  it should "not report a last name that contains valid characters" in {
    val errors = CsvLineValidator.validateLine(CsvLine.copy(surname = "Jerry 'one shot' McJerry-Face").toString)

    errors shouldBe empty
  }

  it should "report a last name that contains invalid characters" in {
    val errors = CsvLineValidator.validateLine(CsvLine.copy(surname = "Jerry Bloggs $$%34839 4").toString)

    errors.get should contain(BulkRequestCsvColumn.SURNAME -> Messages("gmp.error.name.invalid", Messages("gmp.lowercase.lastname")))
  }

  it should "not report a missing member reference" in {
    val errors = CsvLineValidator.validateLine(CsvLine.copy(memberReference = None).toString)

    errors shouldBe empty
  }

  it should "report a missing calculation type" in {
    val errors = CsvLineValidator.validateLine(CsvLine.copy(calctype = None).toString)

    errors shouldBe defined
    errors.get should contain(BulkRequestCsvColumn.CALC_TYPE -> Messages("gmp.error.calctype.invalid"))
  }

  it should "report calculation types that are not numbers" in {
    val line = CsvLine.copy(calctype = Some(-99)).toString replace("-99", "invalid calc type")
    val errors = CsvLineValidator.validateLine(line)

    errors shouldBe defined
    errors.get should contain(BulkRequestCsvColumn.CALC_TYPE -> Messages("gmp.error.calctype.invalid"))
  }

  it should "report a calculation type that is out of bounds" in {
    val errors = CsvLineValidator.validateLine(CsvLine.copy(calctype = Some(5)).toString)

    errors shouldBe defined
    errors.get should contain(BulkRequestCsvColumn.CALC_TYPE -> Messages("gmp.error.calctype.invalid"))
  }

  it should "not report a missing termination date" in {
    val errors = CsvLineValidator.validateLine(CsvLine.copy(terminationDate = None).toString)

    errors shouldBe empty
  }

  it should "report a termination date that is not in the correct format" in {
    val errors = CsvLineValidator.validateLine(CsvLine.copy(terminationDate = Some("07 07 2016")).toString)

    errors shouldBe defined
    errors.get should contain(BulkRequestCsvColumn.TERMINATION_DATE -> Messages("gmp.error.csv.date.invalid"))
  }

  it should "not report a missing GMP relevant date" in {
    val errors = CsvLineValidator.validateLine(CsvLine.copy(revaluationDate = None).toString)

    errors shouldBe empty
  }

  it should "report an invalid GMP relevant date" in {
    val errors = CsvLineValidator.validateLine(CsvLine.copy(revaluationDate = Some("09384'3094'3249")).toString)

    errors shouldBe defined
    errors.get should contain(BulkRequestCsvColumn.REVAL_DATE -> Messages("gmp.error.csv.date.invalid"))
  }

  it should "report a missing revaluation rate" in {
    val errors = CsvLineValidator.validateLine(CsvLine.copy(revaluationRate = None).toString)

    errors shouldBe defined
    errors.get should contain(BulkRequestCsvColumn.REVAL_RATE -> Messages("gmp.error.revaluation_rate.invalid"))
  }

  it should "report a revaluation rate that is not a number" in {
    val line = CsvLine.copy(revaluationRate = Some(-99)).toString replace("-99", "87erewrkjkdf£$389")
    val errors = CsvLineValidator.validateLine(line)

    errors shouldBe defined
    errors.get should contain(BulkRequestCsvColumn.REVAL_RATE -> Messages("gmp.error.revaluation_rate.invalid"))
  }

  it should "report a revaluation rate that is out of bounds" in {
    val errors = CsvLineValidator.validateLine(CsvLine.copy(revaluationRate = Some(10)).toString)

    errors shouldBe defined
    errors.get should contain(BulkRequestCsvColumn.REVAL_RATE -> Messages("gmp.error.revaluation_rate.invalid"))
  }

  it should "not report a missing dual calculation value" in {
    val errors = CsvLineValidator.validateLine(CsvLine.copy(dualCalc = None).toString)

    errors shouldBe empty
  }

  it should "report an invalid dual calculation value" in {
    val errors = CsvLineValidator.validateLine(CsvLine.copy(dualCalc = Some(-999)).toString replace("-999", "ifdugh"))

    errors shouldBe defined
    errors.get should contain(BulkRequestCsvColumn.DUAL_CALC -> Messages("gmp.error.csv.dual_calc.invalid"))
  }
}
