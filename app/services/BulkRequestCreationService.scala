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

import java.io.InputStreamReader
import java.nio.charset.Charset

import models.{GmpDate, BulkCalculationRequest, BulkCalculationRequestLine, CalculationRequestLine}
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import play.api.Logger
import play.api.i18n.Messages
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.stream.BulkEntityProcessor
import uk.gov.hmrc.time.TaxYear
import validation.{SMValidate, DateValidate, CsvLineValidator}

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
  val LINE_ERROR_EMPTY = -3
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
    //val fileData = sourceData(attachmentUrl).toList.mkString
    //val bulkCalculationRequestLines: List[BulkCalculationRequestLine] = generateBulkCalculationRequestList(fileData)
    val bulkCalculationRequestLines: List[BulkCalculationRequestLine] = list(sourceData(attachmentUrl), LINE_FEED.toByte.toChar, constructBulkCalculationRequestLine _)

    if (bulkCalculationRequestLines.size == 1){
      val emptyFileLines = List(BulkCalculationRequestLine(1, None, None, Some(Map(BulkRequestCsvColumn.LINE_ERROR_EMPTY.toString -> Messages("gmp.error.parsing.empty_file")))))
      val req = BulkCalculationRequest(id, email, reference, enterLineNumbers(emptyFileLines))
      Logger.debug(s"[BulkRequestCreationService][createBulkRequest] size : empty")
      req
    }
    else {
      val req = BulkCalculationRequest(id, email, reference, enterLineNumbers(bulkCalculationRequestLines.drop(1)))
      Logger.debug(s"[BulkRequestCreationService][createBulkRequest] size : ${req.calculationRequests.size}")
      req
    }
  }

//  private def generateBulkCalculationRequestList(data: String): List[BulkCalculationRequestLine] = {
//    data.map {
//      case '’' => '''
//      case c => c
//    }.split(LINE_FEED.toByte.toChar).drop(1).map {
//      constructBulkCalculationRequestLine
//    }.toList match {
//        // If there is nothing in the list, produce a failed calc in the response which shows this
//      case Nil => List(BulkCalculationRequestLine(1, None, None, Some(Map(BulkRequestCsvColumn.LINE_ERROR_EMPTY.toString -> Messages("gmp.error.parsing.empty_file")))))
//      case x => x
//    }
//  }

  private def constructCalculationRequestLine(line: String): CalculationRequestLine = {

    val lineArray = line.replaceAll("’", "'").split(",", -1) map { _.trim }

    val calculationRequestLine = CalculationRequestLine(
      lineArray(BulkRequestCsvColumn.SCON).toUpperCase,
      lineArray(BulkRequestCsvColumn.NINO).replaceAll("\\s", "").toUpperCase,
      lineArray(BulkRequestCsvColumn.FORENAME).toUpperCase,
      lineArray(BulkRequestCsvColumn.SURNAME).toUpperCase,
      emptyStringsToNone(lineArray(BulkRequestCsvColumn.MEMBER_REF), { e: String => Some(e) }),
      emptyStringsToNone(lineArray(BulkRequestCsvColumn.CALC_TYPE), { e: String => protectedToInt(e) }),
      determineTerminationDate(lineArray(BulkRequestCsvColumn.TERMINATION_DATE), lineArray(BulkRequestCsvColumn.REVAL_DATE), lineArray(BulkRequestCsvColumn.CALC_TYPE)),
      emptyStringsToNone(lineArray(BulkRequestCsvColumn.REVAL_DATE), { e: String => protectedDateConvert(e) }),
      emptyStringsToNone(lineArray(BulkRequestCsvColumn.REVAL_RATE), { e: String => protectedToInt(e) }),
      lineArray(BulkRequestCsvColumn.DUAL_CALC).toUpperCase match {
        case "Y" => 1
        case "YES" => 1
        case _ => 0
      },
      lineArray(BulkRequestCsvColumn.TERMINATION_DATE) match {
        case sm if SMValidate.isValid(sm) => true
        case _ => false
      }
    )

    calculationRequestLine.calctype match {
      case Some(0) => calculationRequestLine.copy(revaluationDate = None, revaluationRate = None)
      case Some(2) | Some(4) => calculationRequestLine.copy(revaluationDate = None)
      case Some(3) => calculationRequestLine.copy(dualCalc = 0)
      case _ => calculationRequestLine
    }
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



  private def determineTerminationDate(termDate: String, revalDate: String, calcType: String): Option[String] = {
    termDate match {
      case "" => None
      case sm if SMValidate matches sm => calcType match {
        case "3" => None
        case _ => emptyStringsToNone(revalDate, { date: String => protectedDateConvert(date) })
      }
      case d if !DateValidate.isValid(d) => None
      case _ => {
        val convertedDate = LocalDate.parse(termDate, inputDateFormatter)
        val thatDate = TaxYear(2016).starts.minusDays(1)
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
      case Some(x) if x.keySet.contains(BulkRequestCsvColumn.LINE_ERROR_TOO_FEW.toString)
        || x.keySet.contains(BulkRequestCsvColumn.LINE_ERROR_TOO_MANY.toString)
        => BulkCalculationRequestLine(1, None, None, validationErrors)
      case _ => BulkCalculationRequestLine(1, Some(constructCalculationRequestLine(line)), None, validationErrors)
    }
  }

  private def emptyStringsToNone[T](entry: String, s: (String => Option[T])): Option[T] = {
    entry match {
      case "" => None
      case _ => s(entry)
    }
  }

  def sourceData(resourceLocation: String): Iterator[Char] = scala.io.Source.fromURL(resourceLocation, "UTF-8").iter
}

object BulkRequestCreationService extends BulkRequestCreationService
