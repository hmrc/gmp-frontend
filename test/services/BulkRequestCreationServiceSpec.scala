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

import helpers.RandomNino
import models.{BulkCalculationRequestLine, CalculationRequestLine, BulkCalculationRequest}
import org.joda.time.LocalDate
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.{OneServerPerSuite, PlaySpec}
import play.api.i18n.Messages

class BulkRequestCreationServiceSpec extends PlaySpec with ScalaFutures with MockitoSugar with OneServerPerSuite {


  val nino1 = RandomNino.generate
  val nino2 = RandomNino.generate

  val calcLine1 = BulkCalculationRequestLine(1, Some(CalculationRequestLine("S1301234T", nino1, "Isambard", "Brunell", Some("IB"), Some(1), Some("2010-02-02"), Some("2010-01-01"), Some(1), 1)),None,None)
  val calcLine2 = BulkCalculationRequestLine(1, Some(CalculationRequestLine("S1301234T", nino2, "George", "Stephenson", Some("GS"), Some(1), Some("2010-02-02"), Some("2010-01-01"), Some(1), 1)),None,None)
  val calcLine3 = BulkCalculationRequestLine(1, Some(CalculationRequestLine("S1301234T", nino2, "George", "Stephenson", Some("GS"), Some(1), None, None, Some(1), 0)),None,None)
  val calcLine4 = BulkCalculationRequestLine(1, Some(CalculationRequestLine("S1301234T", nino2, "George", "Stephenson", None, None, None, None, None, 0)),None,None)
  val calcLine6 = BulkCalculationRequestLine(1, Some(CalculationRequestLine("S1301234T", nino1, "Isambard", "Brunell", Some("IB"), Some(1), Some("2010-02-02"), Some("2010-01-01"), Some(1), 1)),None,None)
  val invalidCalcLine = BulkCalculationRequestLine(1, Some(CalculationRequestLine("invalid_scon", nino1, "Isambard", "Brunell", Some("IB"), Some(1), Some("2010-02-02"), Some("2010-01-01"), Some(1), 1)),None,None)
  val anotherCalcLine = BulkCalculationRequestLine(1, Some(CalculationRequestLine("S2730012K", "GY000002A", "PARIS", "HILTON", Some("THISISATEST SCENARIO"), Some(0), Some("2016-07-07"), None, None, 1)),None,None)

  val inputLine1 = (Messages("gmp.upload_csv_column_headers") + "\n" + lineListFromCalculationRequestLine(calcLine1).mkString).toList
  val inputLine2 = (Messages("gmp.upload_csv_column_headers") + "\n" + lineListFromCalculationRequestLine(calcLine2).mkString).toList
  val inputLine3 = (Messages("gmp.upload_csv_column_headers") + "\n" + lineListFromCalculationRequestLine(calcLine3).mkString).toList
  val inputLine4 = (Messages("gmp.upload_csv_column_headers") + "\n" + lineListFromCalculationRequestLine(calcLine4).mkString).toList
  val inputLine6 = (Messages("gmp.upload_csv_column_headers") + "\n" + lineListFromCalculationRequestLine(calcLine6).mkString).toList
  val invalidInputLine = (Messages("gmp.upload_csv_column_headers") + "\n" + lineListFromCalculationRequestLine(invalidCalcLine).mkString).toList
  val anotherInputLine = (Messages("gmp.upload_csv_column_headers") + "\n" + "S2730012K,GY000002A,PARIS,HILTON,THISISATEST SCENARIO1,0,07/07/2016,,,Yes").toList
  val anotherInputLineNoDualCalc = (Messages("gmp.upload_csv_column_headers") + "\n" + "S2730012K,GY000002A,PARIS,HILTON,THISISATEST SCENARIO1,0,07/07/2016,,,No").toList

  val inputLineWithoutHeader = lineListFromCalculationRequestLine(calcLine2)

  object TestBulkRequestCreationService extends BulkRequestCreationService {

    override def sourceData(resourceLocation: String): Iterator[Char] = {

      resourceLocation.substring(resourceLocation.lastIndexOf('/') + 1) match {
        case "1" => inputLine1.iterator
        case "2" => inputLine2.iterator
        case "3" => (inputLine1 ::: inputLineWithoutHeader).iterator
        case "4" => inputLine3.iterator
        case "5" => inputLine4.iterator
        case "6" => inputLine6.iterator
        case "7" => invalidInputLine.iterator
        case "8" => anotherInputLine.iterator
        case "9" => anotherInputLineNoDualCalc.iterator
      }
    }

  }

  "Bulk Request Creation Service" must {

    val collection = "gmp"

    val bulkRequest1 = BulkCalculationRequest("1", "bill@bixby.com", "uploadRef1", List(calcLine1))
    val bulkRequest2 = BulkCalculationRequest("2", "timburton@scary.com", "uploadRef2", List(calcLine2))
    val bulkRequest3 = BulkCalculationRequest("3", "lou@ferrigno.com", "uploadRef3", List(calcLine1, calcLine2.copy(lineId = 2)))
    val bulkRequest4 = BulkCalculationRequest("4", "bill@bixby.com", "uploadRef1", List(calcLine3))
    val bulkRequest6 = BulkCalculationRequest("6", "bill@bixby.com", "uploadRef1", List(calcLine6))
    val invalidBulkRequest = BulkCalculationRequest("7", "bill@bixbt.com", "uploadRef1", List(invalidCalcLine))
    val anotherBulkRequest = BulkCalculationRequest("8", "somebody@mail.com", "uploadRef22", List(anotherCalcLine))

    "return Bulk Request 1" in {

      val bulkRequest: BulkCalculationRequest = TestBulkRequestCreationService.createBulkRequest(collection, "1", bulkRequest1.email, bulkRequest1.reference)

      bulkRequest mustBe bulkRequest1

    }

    "return Bulk Request 2" in {

      val bulkRequest: BulkCalculationRequest = TestBulkRequestCreationService.createBulkRequest(collection, "2", bulkRequest2.email, bulkRequest2.reference)

      bulkRequest mustBe bulkRequest2

    }

    "return Bulk Request with multiple calcs" in {
      val bulkRequest: BulkCalculationRequest = TestBulkRequestCreationService.createBulkRequest(collection, "3", bulkRequest3.email, bulkRequest3.reference)

      bulkRequest mustBe bulkRequest3
    }

    "contain Nones when dates are left blank" in {

      val bulkRequest: BulkCalculationRequest = TestBulkRequestCreationService.createBulkRequest(collection, "4", bulkRequest4.email, bulkRequest4.reference)

      bulkRequest.calculationRequests.head.validCalculationRequest.get.revaluationDate mustBe None
      bulkRequest.calculationRequests.head.validCalculationRequest.get.terminationDate mustBe None
    }

    "contain Nones for other options" in {

      val bulkRequest: BulkCalculationRequest = TestBulkRequestCreationService.createBulkRequest(collection, "5", bulkRequest4.email, bulkRequest4.reference)

      bulkRequest.calculationRequests.head.validCalculationRequest.get.revaluationDate mustBe None
      bulkRequest.calculationRequests.head.validCalculationRequest.get.terminationDate mustBe None
      bulkRequest.calculationRequests.head.validCalculationRequest.get.calctype mustBe None
      bulkRequest.calculationRequests.head.validCalculationRequest.get.memberReference mustBe None
      bulkRequest.calculationRequests.head.validCalculationRequest.get.revaluationRate mustBe None
    }

    "return Bulk Request with correct line numbers" in {
      val bulkRequest: BulkCalculationRequest = TestBulkRequestCreationService.createBulkRequest(collection, "3", bulkRequest3.email, bulkRequest3.reference)

      bulkRequest.calculationRequests.head.lineId mustBe 1
      bulkRequest.calculationRequests.tail.head.lineId mustBe 2
    }

    "return Bulk Request 6" in {

      val bulkRequest: BulkCalculationRequest = TestBulkRequestCreationService.createBulkRequest(collection, "6", bulkRequest6.email, bulkRequest6.reference)

      bulkRequest mustBe bulkRequest6

    }

    "return bulk requests with field validation messages" in {
      val bulkRequest = TestBulkRequestCreationService.createBulkRequest(collection, "7", invalidBulkRequest.email, invalidBulkRequest.reference)
      val errors = bulkRequest.calculationRequests.head.validationErrors

      errors mustBe defined
      errors.get.isDefinedAt(BulkRequestCsvColumn.SCON.toString) mustBe true
    }

    "return bulk request with dual calc Yes" in {
      val bulkRequest = TestBulkRequestCreationService.createBulkRequest(collection, "8", anotherBulkRequest.email, anotherBulkRequest.reference)
      bulkRequest.calculationRequests.head.validCalculationRequest.head.dualCalc must be (1)
    }

    "return bulk request with dual calc No" in {
      val bulkRequest = TestBulkRequestCreationService.createBulkRequest(collection, "9", "somebody@email.com", "a-ref-123")
      bulkRequest.calculationRequests.head.validCalculationRequest.head.dualCalc must be (0)
    }
  }

  def lineListFromCalculationRequestLine(line: BulkCalculationRequestLine): List[Char] = {
    val l = line.validCalculationRequest.get.productIterator.toList.zipWithIndex

    def process(item: Any) = {
      val dateRegEx = """([0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9])""".r

      item match {
        case (None, i) => ""
        case (Some(dateRegEx(s)), i) => new LocalDate(s).toString("dd/MM/yyyy")
        case (x: Int, BulkRequestCsvColumn.DUAL_CALC) => x match {
          case 1 => "Y";
          case _ => "N"
        }
        case (Some(x), i) => s"$x"
        case (x: String, i) => x
      }
    }

    { (l map process).mkString(",") + "\n" }.toCharArray.toList

  }
}