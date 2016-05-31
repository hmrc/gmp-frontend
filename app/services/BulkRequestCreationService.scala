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

package services

import models.{GmpDate, BulkCalculationRequest, BulkCalculationRequestLine, CalculationRequestLine}
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import play.api.Logger
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.stream.BulkEntityProcessor
import validation.{DateValidate, CsvLineValidator}

import scala.util.{Failure, Success, Try}

object BulkRequestCsvColumn {
  val SCON = 0
  val NINO = 1
  val FORENAME = 2
  val SURNAME = 3
  val MEMBER_REF = 4
  val CALC_TYPE = 5
  val TERMINATION_DATE = 6
  val REVAL_DATE = 7
  val REVAL_RATE = 8
  val DUAL_CALC = 9
  val LINE_ERROR_TOO_FEW = -1
  val LINE_ERROR_TOO_MANY = -2
}

trait BulkRequestCreationService extends BulkEntityProcessor[BulkCalculationRequestLine] with ServicesConfig {

  val LINE_FEED: Int = 10


  private val DATE_FORMAT: String = "yyyy-MM-dd"

  val inputDateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy")

  private def enterLineNumbers(bulkCalculationRequestLines: List[BulkCalculationRequestLine]): List[BulkCalculationRequestLine] = {
    for ((x, i) <- bulkCalculationRequestLines.zipWithIndex) yield x.copy(lineId = i + 1)
  }

  def createBulkRequest(collection: String, id: String, email: String, reference: String): BulkCalculationRequest = {

    val attachmentUrl = s"${baseUrl("attachments")}/attachments-internal/$collection/$id"

    val bulkCalculationRequestLines: List[BulkCalculationRequestLine] = generateBulkCalculationRequestList(sourceData(attachmentUrl))

    val req = BulkCalculationRequest(id, email, reference, enterLineNumbers(bulkCalculationRequestLines))

    Logger.debug(s"[BulkRequestCreationService][createBulkRequest] size : ${req.calculationRequests.size}")

    req
  }

  private def generateBulkCalculationRequestList(data: Iterator[Char]): List[BulkCalculationRequestLine] = {
    data.map{ c =>
      c match {
        case '’' => '''
        case _ => c
      }
    }.mkString.split(LINE_FEED.toByte.toChar).drop(1).map {
      constructBulkCalculationRequestLine
    }.toList
  }

  private def constructCalculationRequestLine(line: String): CalculationRequestLine = {

    val lineArray = line.split(",", -1)

    CalculationRequestLine(
      lineArray(BulkRequestCsvColumn.SCON).toUpperCase.trim,
      lineArray(BulkRequestCsvColumn.NINO).replaceAll("\\s", "").toUpperCase.trim,
      lineArray(BulkRequestCsvColumn.FORENAME).toUpperCase.trim,
      lineArray(BulkRequestCsvColumn.SURNAME).toUpperCase.trim,
      emptyStringsToNone(lineArray(BulkRequestCsvColumn.MEMBER_REF).trim, { e: String => Some(e) }),
      emptyStringsToNone(lineArray(BulkRequestCsvColumn.CALC_TYPE).trim, { e: String => protectedToInt(e) }),
      determineTerminationDate(lineArray(BulkRequestCsvColumn.TERMINATION_DATE).trim, lineArray(BulkRequestCsvColumn.REVAL_DATE)),
      emptyStringsToNone(lineArray(BulkRequestCsvColumn.REVAL_DATE).trim, { e: String => protectedDateConvert(e) }),
      emptyStringsToNone(lineArray(BulkRequestCsvColumn.REVAL_RATE).trim, { e: String => protectedToInt(e) }),
      lineArray(BulkRequestCsvColumn.DUAL_CALC).trim.toUpperCase match {
        case "Y" => 1
        case "YES" => 1
        case _ => 0
      }
    )
  }

  private def protectedToInt(number: String): Option[Int] ={
    val tryConverting = Try(number.toInt)

    tryConverting match {
      case Success(number) => Some(number)
      case Failure(f) =>
        Logger.debug(s"[BulkCreationService][protectedToInt : ${f.getMessage}]")
        None
    }
  }

  private def protectedDateConvert(date: String): Option[String] ={
    val tryConverting = Try(LocalDate.parse(date, inputDateFormatter).toString(DATE_FORMAT))

    tryConverting match {
      case Success(convertedDate) => Some(convertedDate)
      case Failure(f) =>
        Logger.debug(s"[BulkCreationService][protectedDateConvert : ${f.getMessage}]")
        None
    }
  }



  private def determineTerminationDate(termDate: String, revalDate: String): Option[String] =
  {
    termDate match {
      case "" => emptyStringsToNone(revalDate, { e: String => protectedDateConvert(e) })
      case d if !DateValidate.isValid(d) => None
      case _ => {
        val convertedDate = LocalDate.parse(termDate, inputDateFormatter)
        val thatDate = new LocalDate(2016, 4, 5)
        if (convertedDate.isAfter(thatDate))
          Some(convertedDate.toString(DATE_FORMAT))
        else
          None
      }
    }

  }

  private def constructBulkCalculationRequestLine(line: String): BulkCalculationRequestLine = {

    val validationErrors = CsvLineValidator.validateLine(line) match {
      case Some(m) => Some(m.collect {
        case (k, v) => (k.toString, v)
      })
      case _ => None
    }

    validationErrors match {
      case Some(x) if (x.keySet.contains(BulkRequestCsvColumn.LINE_ERROR_TOO_FEW.toString) || x.keySet.contains(BulkRequestCsvColumn.LINE_ERROR_TOO_MANY.toString)) => BulkCalculationRequestLine(1, None, None, validationErrors)
      case _ => BulkCalculationRequestLine(1, Some(constructCalculationRequestLine(line)), None, validationErrors)
    }
  }

  private def emptyStringsToNone[T](entry: String, s: (String => Option[T])): Option[T] = {
    entry match {
      case "" => None
      case _ => s(entry)
    }
  }

  def sourceData(resourceLocation: String): Iterator[Char] = scala.io.Source.fromURL(resourceLocation).iter
}

object BulkRequestCreationService extends BulkRequestCreationService
