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

import models.{BulkCalculationRequest, BulkCalculationRequestLine, CalculationRequestLine}
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import play.api.Logger
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.stream.BulkEntityProcessor
import validation.CsvLineValidator

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
  val LINE_ERROR = -1
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
    data.mkString.split(LINE_FEED.toByte.toChar).drop(1).map {
      constructBulkCalculationRequestLine
    }.toList
  }

  private def constructCalculationRequestLine(line: String): CalculationRequestLine = {

    val lineArray = line.split(",", -1)

    CalculationRequestLine(
      lineArray(BulkRequestCsvColumn.SCON).trim,
      lineArray(BulkRequestCsvColumn.NINO).trim,
      lineArray(BulkRequestCsvColumn.FORENAME),
      lineArray(BulkRequestCsvColumn.SURNAME),
      emptyStringsToNone(lineArray(BulkRequestCsvColumn.MEMBER_REF), { e: String => Some(e) }),
      emptyStringsToNone(lineArray(BulkRequestCsvColumn.CALC_TYPE).trim, { e: String => Some(e.toInt) }),
      emptyStringsToNone(lineArray(BulkRequestCsvColumn.TERMINATION_DATE), { e: String => Some(LocalDate.parse(e, inputDateFormatter).toString(DATE_FORMAT)) }),
      emptyStringsToNone(lineArray(BulkRequestCsvColumn.REVAL_DATE).trim, { e: String => Some(LocalDate.parse(e, inputDateFormatter).toString(DATE_FORMAT)) }),
      emptyStringsToNone(lineArray(BulkRequestCsvColumn.REVAL_RATE).trim, { e: String => Some(e.toInt) }),
      lineArray(BulkRequestCsvColumn.DUAL_CALC).trim.toUpperCase match {
        case "Y" => Some(1)
        case "YES" => Some(1)
        case _ => None
      }
    )
  }

  private def constructBulkCalculationRequestLine(line: String): BulkCalculationRequestLine = {

    val validationErrors = CsvLineValidator.validateLine(line) match {
      case Some(m) => Some(m.collect {
        case (k, v) => (k.toString, v)
      })
      case _ => None
    }

    BulkCalculationRequestLine(1, Some(constructCalculationRequestLine(line)), None, validationErrors)
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
