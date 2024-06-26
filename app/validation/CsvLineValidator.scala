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

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import play.api.i18n.Messages
import services.BulkRequestCsvColumn
import uk.gov.hmrc.time.TaxYear

trait FieldValidator {

  val MAX_NAME_LENGTH = 99
  val DATE_FORMAT_PATTERN = "dd/MM/yyyy"
  val DATE_FORMAT = DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN)

  def validateScon(scon: String)(implicit messages: Messages): Option[String] = {
    scon match {
      case "" => Some(Messages("gmp.error.mandatory", Messages("gmp.scon")))
      case x if !SconValidate.isValid(x) => Some(Messages("gmp.error.scon.bulk.invalid"))
      case _ => None
    }
  }

  def validateNino(nino: String)(implicit messages: Messages): Option[String] = {
    val TEMP_NINO = "TN"
    nino match {
      case "" => Some(Messages("gmp.error.mandatory", Messages("gmp.nino")))
      case x if x.toUpperCase().startsWith(TEMP_NINO) => Some(Messages("gmp.error.nino.temporary"))
      case x if !NinoValidate.isValid(x) => Some(Messages("gmp.error.nino.invalid"))
      case _ => None
    }
  }

  def validateFirstName(name: String)(implicit messages: Messages) = {
    name match {
      case "" => Some(Messages("gmp.error.firstnameorinitial"))
      case x if x.length >= MAX_NAME_LENGTH => Some(Messages("gmp.error.firstname.toolong"))
      case x if !NameValidate.isValid(x) => {
        val message = Messages("gmp.error.name.invalid", Messages("gmp.lowercase.firstname"))
        Some(s""""$message"""")
      }
      case _ => None
    }
  }

  def validateLastName(name: String)(implicit messages: Messages) = {
    name match {
      case "" => Some(Messages("gmp.error.mandatory", Messages("gmp.lowercase.lastname")))
      case x if x.length >= MAX_NAME_LENGTH => Some(Messages("gmp.error.lastname.toolong"))
      case x if x.length == 1 => Some(Messages("gmp.error.surname.invalid"))
      case x if !NameValidate.isValid(x) => {
        val message = Messages("gmp.error.name.invalid", Messages("gmp.lowercase.lastname"))
        Some(s""""$message"""")
      }
      case _ => None
    }
  }

  def validateCalcType(calcType: String)(implicit messages: Messages) = {
    calcType match {
      case "" => Some(Messages("gmp.error.calctype.out_of_range"))
      case x if !(x matches """\d+""") => Some(Messages("gmp.error.calctype.invalid"))
      case x if x.toInt > 4 => Some(Messages("gmp.error.calctype.out_of_range"))
      case _ => None
    }
  }

  def validateDate(value: String)(implicit messages: Messages) = {
    value match {
      case "" => None
      case x if !DateValidate.isValid(value) => Some(Messages("gmp.error.csv.date.invalid"))
      case _ => None
    }
  }

  def validateTerminationDate(value: String)(implicit messages: Messages) = value match {
    case "" => None
    case sm if SMValidate.isValid(sm) => None
    case x => tryParseDate(x) match {
      case Some(validDate) if validDate.isBefore(TaxYear(2016).starts) => Some(Messages("gmp.error.csv.termination.oob"))
      case None => Some(Messages("gmp.error.csv.termination.invalid"))
      case _ => None
    }
  }

  def validateRevalRate(value: String)(implicit messages: Messages) = {
    value match {
      case "" => None
      case x if !(x matches """\d+""") => Some(Messages("gmp.error.revaluation_rate.invalid"))
      case x if x.toInt > 3 => Some(Messages("gmp.error.revaluation_rate.invalid"))
      case _ => None
    }
  }

  def validateDualCalc(value: String)(implicit messages: Messages) = {
    value match {
      case "" => Some(Messages("gmp.error.csv.dual_calc.invalid"))
      case x if !(x matches "(?i)y(es)?|no?") => Some(Messages("gmp.error.csv.dual_calc.invalid"))
      case _ => None
    }
  }

  private def tryParseDate(date: String): Option[LocalDate] =
    if (DateValidate.isValid(date)) {
      Some(LocalDate.parse(date, DATE_FORMAT))
    } else {
      None
    }
}

object FieldValidator extends FieldValidator

object CsvLineValidator extends FieldValidator {

  val CSV_COLUMN_COUNT = 10

  def validateLine(line: String)(implicit messages: Messages) = {

    val columns = line.split(",", -1).map {
      _.trim
    }.toList

    columns.length match {
      case x if x < CSV_COLUMN_COUNT => Some(Map(BulkRequestCsvColumn.LINE_ERROR_TOO_FEW -> Messages("gmp.error.parsing.too_few_columns")))
      case x if x > CSV_COLUMN_COUNT => Some(Map(BulkRequestCsvColumn.LINE_ERROR_TOO_MANY -> Messages("gmp.error.parsing.too_many_columns")))
      case _ => {
        columns.zipWithIndex.map {
          case (value, BulkRequestCsvColumn.SCON) => (BulkRequestCsvColumn.SCON, validateScon(value))
          case (value, BulkRequestCsvColumn.NINO) => (BulkRequestCsvColumn.NINO, validateNino(value))
          case (value, BulkRequestCsvColumn.FORENAME) => (BulkRequestCsvColumn.FORENAME, validateFirstName(value))
          case (value, BulkRequestCsvColumn.SURNAME) => (BulkRequestCsvColumn.SURNAME, validateLastName(value))
          case (value, BulkRequestCsvColumn.CALC_TYPE) => (BulkRequestCsvColumn.CALC_TYPE, validateCalcType(value))
          case (value, BulkRequestCsvColumn.TERMINATION_DATE) => (BulkRequestCsvColumn.TERMINATION_DATE, validateTerminationDate(value))
          case (value, BulkRequestCsvColumn.REVAL_DATE) => (BulkRequestCsvColumn.REVAL_DATE, validateDate(value))
          case (value, BulkRequestCsvColumn.REVAL_RATE) => (BulkRequestCsvColumn.REVAL_RATE, validateRevalRate(value))
          case (value, BulkRequestCsvColumn.DUAL_CALC) => (BulkRequestCsvColumn.DUAL_CALC, validateDualCalc(value))
          case (value, key) => (key, None)
        }.toMap.collect {
          case v if v._2.isDefined => (v._1, v._2.get)
        } match {
          case map if map.nonEmpty => Some(map)
          case _ => None
        }
      }
    }
  }
}
