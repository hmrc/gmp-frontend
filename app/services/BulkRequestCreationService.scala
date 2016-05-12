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

trait BulkRequestCreationService extends BulkEntityProcessor[BulkCalculationRequestLine] with ServicesConfig {

  val LINE_FEED: Int = 10

  private val SCON: Int = 0
  private val NINO: Int = 1
  private val FORENAME: Int = 2
  private val SURNAME: Int = 3
  private val MEMBER_REF: Int = 4
  private val CALC_TYPE: Int = 5
  private val TERMINATION_DATE: Int = 6
  private val REVAL_DATE: Int = 7
  private val REVAL_RATE: Int = 8
  private val DUAL_CALC: Int = 9

  private val DATE_FORMAT: String = "yyyy-MM-dd"

  val inputDateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy")

  private def enterLineNumbers(bulkCalculationRequestLines: List[BulkCalculationRequestLine]): List[BulkCalculationRequestLine] = {
    for ((x, i) <- bulkCalculationRequestLines.zipWithIndex) yield x.copy(lineId = i + 1)
  }

  def createBulkRequest(collection: String, id: String, email: String, reference: String): BulkCalculationRequest = {


    val attachmentUrl = s"${baseUrl("attachments")}/attachments-internal/$collection/$id"

    val bulkCalculationRequestLines: List[BulkCalculationRequestLine] = list(sourceData(attachmentUrl), LINE_FEED.toByte.toChar, constructBulkCalculationRequestLine _)

    val req = BulkCalculationRequest(id, email, reference, enterLineNumbers(bulkCalculationRequestLines))

    Logger.debug(s"[BulkRequestCreationService][createBulkRequest] size : ${req.calculationRequests.size}")

    req

  }


  private def constructCalculationRequestLine(line: String): CalculationRequestLine = {

    val lineArray = line.split(",", -1)

    CalculationRequestLine(lineArray(SCON), lineArray(NINO), lineArray(FORENAME), lineArray(SURNAME),
      emptyStringsToNone(lineArray(MEMBER_REF), { e: String => Some(e) }),
      emptyStringsToNone(lineArray(CALC_TYPE), { e: String => Some(e.toInt) }),
      emptyStringsToNone(lineArray(TERMINATION_DATE), { e: String => Some(LocalDate.parse(e, inputDateFormatter).toString(DATE_FORMAT)) }),
      emptyStringsToNone(lineArray(REVAL_DATE), { e: String => Some(LocalDate.parse(e, inputDateFormatter).toString(DATE_FORMAT)) }),
      emptyStringsToNone(lineArray(REVAL_RATE), { e: String => Some(e.toInt) }),
      emptyStringsToNone(lineArray(DUAL_CALC), { e: String => Some(e.toInt) })
    )
  }

  private def constructBulkCalculationRequestLine(line: String): BulkCalculationRequestLine = {
    BulkCalculationRequestLine(1, Some(constructCalculationRequestLine(line)),None)
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
