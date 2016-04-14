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

class BulkRequestCreationServiceSpec extends PlaySpec with ScalaFutures with MockitoSugar with OneServerPerSuite {


  val nino1 = RandomNino.generate
  val nino2 = RandomNino.generate

  val calcLine1 = BulkCalculationRequestLine(1, Some(CalculationRequestLine("S1301234T", nino1, "Isambard", "Brunell", Some("IB"), Some(1), Some("2010-02-02"), Some("2010-01-01"), Some(1), Some(0))),None)
  val calcLine2 = BulkCalculationRequestLine(1, Some(CalculationRequestLine("S1301234T", nino2, "George", "Stephenson", Some("GS"), Some(1), Some("2010-02-02"), Some("2010-01-01"), Some(1), Some(0))),None)
  val calcLine3 = BulkCalculationRequestLine(1, Some(CalculationRequestLine("S1301234T", nino2, "George", "Stephenson", Some("GS"), Some(1), None, None, Some(1), Some(0))),None)
  val calcLine4 = BulkCalculationRequestLine(1, Some(CalculationRequestLine("S1301234T", nino2, "George", "Stephenson", None, None, None, None, None, None)),None)

  val inputLine1 = lineListFromCalculationRequestLine(calcLine1)
  val inputLine2 = lineListFromCalculationRequestLine(calcLine2)
  val inputLine3 = lineListFromCalculationRequestLine(calcLine3)
  val inputLine4 = lineListFromCalculationRequestLine(calcLine4)

  object TestBulkRequestCreationService extends BulkRequestCreationService {

    override def sourceData(resourceLocation: String): Iterator[Char] = {

      resourceLocation.substring(resourceLocation.lastIndexOf('/') + 1) match {
        case "1" => inputLine1.iterator
        case "2" => inputLine2.iterator
        case "3" => (inputLine1 ::: inputLine2).iterator
        case "4" => inputLine3.iterator
        case "5" => inputLine4.iterator

      }
    }

  }

  "Bulk Request Creation Service" must {

    val collection = "gmp"

    val bulkRequest1 = BulkCalculationRequest("1", "bill@bixby.com", "uploadRef1", List(calcLine1))
    val bulkRequest2 = BulkCalculationRequest("2", "timburton@scary.com", "uploadRef2", List(calcLine2))
    val bulkRequest3 = BulkCalculationRequest("3", "lou@ferrigno.com", "uploadRef3", List(calcLine1, calcLine2.copy(lineId = 2)))
    val bulkRequest4 = BulkCalculationRequest("4", "bill@bixby.com", "uploadRef1", List(calcLine3))

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
      bulkRequest.calculationRequests.head.validCalculationRequest.get.dualCalc mustBe None
      bulkRequest.calculationRequests.head.validCalculationRequest.get.memberReference mustBe None
      bulkRequest.calculationRequests.head.validCalculationRequest.get.revaluationRate mustBe None
    }

    "return Bulk Request with correct line numbers" in {
      val bulkRequest: BulkCalculationRequest = TestBulkRequestCreationService.createBulkRequest(collection, "3", bulkRequest3.email, bulkRequest3.reference)

      bulkRequest.calculationRequests.head.lineId mustBe 1
      bulkRequest.calculationRequests.tail.head.lineId mustBe 2
    }

  }

  def lineListFromCalculationRequestLine(line: BulkCalculationRequestLine): List[Char] = {
    val l = line.validCalculationRequest.get.productIterator.toList

    def process(item: Any) = {
      val dateRegEx = """([0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9])""".r
      item match {
        case None => ","
        case Some(dateRegEx(s)) => new LocalDate(s).toString("dd/MM/yyyy") + ","
        case Some(x) => s"$x,"
        case x: String => x + ","
      }
    }
    {
      for (p <- l) yield process(p)
    }.flatten :+ 10.toByte.toChar
  }
}
