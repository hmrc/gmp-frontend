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

package services

import com.google.inject.Inject
import models.upscan.UploadedSuccessfully
import models.{BulkCalculationRequest, BulkCalculationRequestLine, CalculationRequestLine}
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import play.api.i18n.{Messages, MessagesImpl}
import play.api.mvc.MessagesControllerComponents
import play.api.{Configuration, Environment, Logging, Mode}
import uk.gov.hmrc.time.TaxYear
import validation.{CsvLineValidator, DateValidate, SMValidate}
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

case object DataLimitExceededException extends Throwable
case object IncorrectlyEncodedException extends Throwable

class BulkRequestCreationService @Inject()(environment: Environment,
                                           val runModeConfiguration: Configuration, mcc: MessagesControllerComponents,
                                           val messagesApi : play.api.i18n.MessagesApi
                                          ) extends BulkEntityProcessor[BulkCalculationRequestLine] with Logging {

  implicit lazy val messages: Messages = MessagesImpl(mcc.langs.availables.head, messagesApi)

  protected def mode: Mode = environment.mode

  private class LimitingEnumerator(limit: Int, delimiter: Char, iterator: Iterator[Char]) extends Iterator[Char] {

    private var count = 0

    override def hasNext: Boolean = iterator.hasNext && count <= limit

    override def next(): Char = {
      val c = iterator.next()
      if (c == delimiter) {
        count += 1
      }
      c
    }

    def getCount = count

    def hasDataBeyondLimit = !hasNext && iterator.hasNext
  }

  val LINE_FEED: Int = 10
  val MAX_LINES = 25001     // Limit is 25,000 rows, but we add one to account for the header

  private val DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd")

  val inputDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

  private def enterLineNumbers(bulkCalculationRequestLines: List[BulkCalculationRequestLine]): List[BulkCalculationRequestLine] = {
    for ((x, i) <- bulkCalculationRequestLines.zipWithIndex) yield x.copy(lineId = i + 1)
  }

  def createBulkRequest(upscanCallback: UploadedSuccessfully, email: String, reference: String): Either[Throwable, BulkCalculationRequest] = {
    val enumerator = new LimitingEnumerator(MAX_LINES, LINE_FEED.toByte.toChar, sourceData(upscanCallback.downloadUrl))

    Try(list(enumerator, LINE_FEED.toByte.toChar, constructBulkCalculationRequestLine(_))) match {
      case Success(bulkCalculationRequestLines) =>

        if (enumerator.hasDataBeyondLimit) {
          logger.debug(s"[BulkRequestCreationService][createBulkRequest] size: ${enumerator.getCount} (too large, throwing DataLimitExceededException)")
          Left(DataLimitExceededException)
        } else {
          if (bulkCalculationRequestLines.size == 1) {
            val emptyFileLines = List(BulkCalculationRequestLine(1, None, None,
              Some(Map(BulkRequestCsvColumn.LINE_ERROR_EMPTY.toString -> messages("gmp.error.parsing.empty_file")))))
            val req = BulkCalculationRequest(upscanCallback.reference, email, reference, enterLineNumbers(emptyFileLines))
            logger.debug(s"[BulkRequestCreationService][createBulkRequest] size : empty")
            Right(req)
          }
          else {
            val req = BulkCalculationRequest(upscanCallback.reference, email, reference, enterLineNumbers(bulkCalculationRequestLines.drop(1)))
            logger.debug(s"[BulkRequestCreationService][createBulkRequest] size : ${req.calculationRequests.size}")
            Right(req)
          }
        }


      case Failure(_) => Left(IncorrectlyEncodedException
      )
    }
  }

  private def constructCalculationRequestLine(line: String): CalculationRequestLine = {

    val lineArray = line.replaceAll("’", "'").split(",", -1) map {
      _.trim
    }

    val calculationRequestLine = CalculationRequestLine(
      lineArray(BulkRequestCsvColumn.SCON).toUpperCase,
      lineArray(BulkRequestCsvColumn.NINO).replaceAll("\\s", "").toUpperCase,
      lineArray(BulkRequestCsvColumn.FORENAME).toUpperCase,
      lineArray(BulkRequestCsvColumn.SURNAME).toUpperCase,
      emptyStringsToNone(lineArray(BulkRequestCsvColumn.MEMBER_REF), { (e: String) => Some(e) }),
      emptyStringsToNone(lineArray(BulkRequestCsvColumn.CALC_TYPE), { (e: String) => protectedToInt(e) }),
      determineTerminationDate(lineArray(BulkRequestCsvColumn.TERMINATION_DATE),
        lineArray(BulkRequestCsvColumn.REVAL_DATE), lineArray(BulkRequestCsvColumn.CALC_TYPE)),
      emptyStringsToNone(lineArray(BulkRequestCsvColumn.REVAL_DATE), { (e: String) => protectedDateConvert(e) }),
      emptyStringsToNone(lineArray(BulkRequestCsvColumn.REVAL_RATE), { (e: String) => protectedToInt(e) }),
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

  private def protectedToInt(number: String): Option[Int] = {
    val tryConverting = Try(number.toInt)

    tryConverting match {
      case Success(n) => Some(n)
      case Failure(e) =>
        logger.warn(s"[BulkCreationService][protectedToInt] ${e.getMessage}", e)
        None
    }
  }

  private def protectedDateConvert(date: String): Option[String] = {
    val tryConverting = Try(LocalDate.parse(date, inputDateFormatter).format(DATE_FORMAT))

    tryConverting match {
      case Success(convertedDate) => Some(convertedDate)
      case Failure(e) =>
        logger.warn(s"[BulkCreationService][protectedDateConvert] ${e.getMessage}", e)
        None
    }
  }

  private def determineTerminationDate(termDate: String, revalDate: String, calcType: String): Option[String] = {
    termDate match {
      case "" => None
      case sm if SMValidate matches sm => calcType match {
        case "3" => None
        case _ => emptyStringsToNone(revalDate, { (date: String) => protectedDateConvert(date) })
      }
      case d if !DateValidate.isValid(d) => None
      case _ => {
        val convertedDate = LocalDate.parse(termDate, inputDateFormatter)
        val thatDate = TaxYear(2016).starts.minusDays(1)
        if (convertedDate.isAfter(thatDate)) {
          Some(convertedDate.format(DATE_FORMAT))
        } else {
          None
        }
      }
    }

  }

  private def constructBulkCalculationRequestLine(line: String): BulkCalculationRequestLine = {

    val validationErrors = CsvLineValidator.validateLine(line)(messages) match {
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
