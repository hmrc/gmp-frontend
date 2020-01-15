/*
 * Copyright 2020 HM Revenue & Customs
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

import java.io.ByteArrayInputStream
import java.net.URL
import java.nio.charset.Charset
import java.time.Instant

import helpers.RandomNino
import models.upscan.{UploadDetails, UploadedSuccessfully, UpscanReadyCallback}
import models.{BulkCalculationRequest, BulkCalculationRequestLine, CalculationRequestLine}
import org.joda.time.{LocalDate, LocalDateTime}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.{OneServerPerSuite, PlaySpec}
import play.api.Environment
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.io.Source

class BulkRequestCreationServiceSpec extends PlaySpec with ScalaFutures with MockitoSugar with OneServerPerSuite {
  implicit val messages=app.injector.instanceOf[MessagesControllerComponents]
  implicit val servicesConfig=app.injector.instanceOf[ServicesConfig]
  implicit val messagesAPI=app.injector.instanceOf[MessagesApi]
  implicit val messagesProvider=MessagesImpl(Lang("en"), messagesAPI)

  val nino1 = RandomNino.generate
  val nino2 = RandomNino.generate

  def upscanCallback(ref: String) = UploadedSuccessfully("ref1", "file1", "http://download1")

  val calcLine1 = BulkCalculationRequestLine(1, Some(CalculationRequestLine("S1301234T", nino1, "Isambard", "Brunell", Some("IB"), Some(1), Some("2017-02-02"), Some("2010-01-01"), Some(1), 1)), None, None)
  val calcLine2 = BulkCalculationRequestLine(1, Some(CalculationRequestLine("S1301234T", nino2, "Tim", "O’Brien", Some("GS"), Some(1), Some("2017-02-02"), Some("2010-01-01"), Some(1), 1)), None, None)
  val calcLine3 = BulkCalculationRequestLine(1, Some(CalculationRequestLine("S1301234T", nino2, "George", "Stephenson", Some("GS"), Some(1), None, None, Some(1), 0)), None, None)
  val calcLine4 = BulkCalculationRequestLine(1, Some(CalculationRequestLine("S1301234T", nino2, "George", "Stephenson", None, None, None, None, None, 0)), None, None)
  val invalidCalcLine = BulkCalculationRequestLine(1, Some(CalculationRequestLine("invalid_scon", nino1, "Isambard", "Brunell", Some("IB"), Some(1), Some("2010-02-02"), Some("2010-01-01"), Some(1), 1)), None, None)
  val calcLine8 = BulkCalculationRequestLine(1, Some(CalculationRequestLine("S2730012K", "GY000002A", "PARIS", "HILTON", Some("THISISATEST SCENARIO"), Some(0), Some("2016-07-07"), None, None, 1)), None, None)
  val calcLine10 = BulkCalculationRequestLine(1, Some(CalculationRequestLine("S2730012K", "GY000002A", "PARIS", "HILTON", Some("THISISATEST SCENARIO"), Some(0), None, Some("2016-07-07"), None, 1)), None, None)
  val calcLine11 = BulkCalculationRequestLine(1, Some(CalculationRequestLine("S2730012K", "GY000002A", "PARIS", "HILTON", Some("THISISATEST SCENARIO"), Some(1), Some("SM"), Some("2016-07-07"), None, 1)), None, None)
  val calcLine12 = BulkCalculationRequestLine(1, Some(CalculationRequestLine("S2730012K", "gy 00 00 02 a", "PARIS", "HILTON", Some("THISISATEST SCENARIO"), Some(1), Some("2016-07-07"), Some("2017-07-07"), None, 1)), None, None)
  val calcLine14 = BulkCalculationRequestLine(1, Some(CalculationRequestLine("S1301234T", " GY000002A ", " Tim ", " O’Brien ", Some(" GS "), Some(1), Some("2017-02-02"), Some("2010-01-01"), Some(1), 1)), None, None)
  val calcLine16 = BulkCalculationRequestLine(1, Some(CalculationRequestLine("S1301234T", " GY000002A ", " Tim ", " O’Brien ", Some(" GS "), Some(1), Some("02 02 2017"), Some("2010-01-01"), Some(1), 1)), None, None)
  val calcLine17 = BulkCalculationRequestLine(1, Some(CalculationRequestLine("s1301234t", nino2, "Tim", "O’Brien", Some("GS"), Some(1), Some("2017-02-02"), Some("2010-01-01"), Some(1), 1)), None, None)
  val calcLine18 = BulkCalculationRequestLine(1, Some(CalculationRequestLine("S1301234T", nino1, "Isambard", "Brunell", Some("IB"), Some(0), Some("2017-02-02"), Some("2010-01-01"), Some(1), 1)), None, None)
  val calcLine19 = BulkCalculationRequestLine(1, Some(CalculationRequestLine("S1301234T", nino1, "Isambard", "Brunell", Some("IB"), Some(3), Some("2017-02-02"), Some("2010-01-01"), Some(1), 1)), None, None)
  val calcLine20 = BulkCalculationRequestLine(1, Some(CalculationRequestLine("S1301234T", nino1, "Isambard", "Brunell", Some("IB"), Some(2), Some("2017-02-02"), Some("2010-01-01"), Some(1), 1)), None, None)
  val calcLine21 = BulkCalculationRequestLine(1, Some(CalculationRequestLine("S1301234T", nino1, "Isambard", "Brunell", Some("IB"), Some(4), Some("2017-02-02"), Some("2010-01-01"), Some(1), 1)), None, None)
  val calcLine22 = BulkCalculationRequestLine(1, Some(CalculationRequestLine("S1301234T", nino1, "Isambard", "Brunell", Some("IB"), Some(3), Some("2016-04-06"), None, Some(1), 0)), None, None)
  val calcLine23 = BulkCalculationRequestLine(1, Some(CalculationRequestLine("S1301234T", nino1, "Isambard", "Brunell", Some("IB"), Some(3), Some("2016-04-05"), None, Some(1), 0)), None, None)
  val calcLine24 = BulkCalculationRequestLine(1, Some(CalculationRequestLine("S2730012K", "GY000002A", "PARIS", "HILTON", Some("THISISATEST SCENARIO 2"), Some(3), Some("SM"), Some("2016-07-07"), None, 1)), None, None)
  val calcLine25 = BulkCalculationRequestLine(1, None, None, None)

  val inputLine1 = (Messages("gmp.upload_csv_column_headers") + "\n" + lineListFromCalculationRequestLine(calcLine1).mkString).toList
  val inputLine2 = (Messages("gmp.upload_csv_column_headers") + "\n" + lineListFromCalculationRequestLine(calcLine2).mkString).toList
  val inputLine3 = (Messages("gmp.upload_csv_column_headers") + "\n" + lineListFromCalculationRequestLine(calcLine3).mkString).toList
  val inputLine4 = (Messages("gmp.upload_csv_column_headers") + "\n" + lineListFromCalculationRequestLine(calcLine4).mkString).toList
  val invalidInputLine = (Messages("gmp.upload_csv_column_headers") + "\n" + lineListFromCalculationRequestLine(invalidCalcLine).mkString).toList
  val anotherInputLine = (Messages("gmp.upload_csv_column_headers") + "\n" + "S2730012K,GY000002A,PARIS,HILTON,THISISATEST SCENARIO1,0,07/07/2016,,,Yes").toList
  val anotherInputLineNoDualCalc = (Messages("gmp.upload_csv_column_headers") + "\n" + "S2730012K,GY000002A,PARIS,HILTON,THISISATEST SCENARIO1,0,07/07/2016,,,No").toList
  val inputLine10 = (Messages("gmp.upload_csv_column_headers") + "\n" + lineListFromCalculationRequestLine(calcLine10).mkString).toList
  val inputLine11 = (Messages("gmp.upload_csv_column_headers") + "\n" + lineListFromCalculationRequestLine(calcLine11).mkString).toList
  val inputLine12 = (Messages("gmp.upload_csv_column_headers") + "\n" + lineListFromCalculationRequestLine(calcLine12).mkString).toList
  val inputLine14 = (Messages("gmp.upload_csv_column_headers") + "\n" + lineListFromCalculationRequestLine(calcLine14).mkString).toList
  val inputLine16 = (Messages("gmp.upload_csv_column_headers") + "\n" + lineListFromCalculationRequestLine(calcLine16).mkString).toList
  val inputLine17 = (Messages("gmp.upload_csv_column_headers") + "\n" + lineListFromCalculationRequestLine(calcLine17).mkString).toList
  val inputLine18 = (Messages("gmp.upload_csv_column_headers") + "\n" + lineListFromCalculationRequestLine(calcLine18).mkString).toList
  val inputLine19 = (Messages("gmp.upload_csv_column_headers") + "\n" + lineListFromCalculationRequestLine(calcLine19).mkString).toList
  val inputLine20 = (Messages("gmp.upload_csv_column_headers") + "\n" + lineListFromCalculationRequestLine(calcLine20).mkString).toList
  val inputLine21 = (Messages("gmp.upload_csv_column_headers") + "\n" + lineListFromCalculationRequestLine(calcLine21).mkString).toList
  val inputLine22 = (Messages("gmp.upload_csv_column_headers") + "\n" + lineListFromCalculationRequestLine(calcLine22).mkString).toList
  val inputLine23 = (Messages("gmp.upload_csv_column_headers") + "\n" + lineListFromCalculationRequestLine(calcLine23).mkString).toList
  val inputLine24 = (Messages("gmp.upload_csv_column_headers") + "\n" + lineListFromCalculationRequestLine(calcLine24).mkString).toList
  val inputLine25 = (Messages("gmp.upload_csv_column_headers") + "\n").toList

  val line26Data: String = Seq(calcLine1, calcLine2, calcLine3) map { line =>
    lineListFromCalculationRequestLine(line).mkString
  } mkString

  val dataLineWith3Calcs = (Messages("gmp.upload_csv_column_headers") + "\n" + line26Data).toList

  val inputLineWithoutData = (Messages("gmp.upload_csv_column_headers") + "\n" + ",,,").toList


  val inputLineWithInvalidNumber = List('g', 'm', 'p', '.', 'u', 'p', 'l', 'o', 'a', 'd', '_', 'c', 's', 'v', '_', 'c', 'o', 'l', 'u', 'm', 'n', '_', 'h', 'e',
    'a', 'd', 'e', 'r', 's', '\n', 'S', '2', '7', '3', '0', '0', '1', '2', 'K', ',', 'g', 'y', ' ', '0', '0', ' ', '0', '0', ' ', '0', '2', ' ', 'a', ',', 'P',
    'A', 'R', 'I', 'S', ',', 'H', 'I', 'L', 'T', 'O', 'N', ',', 'T', 'H', 'I', 'S', 'I', 'S', 'A', 'T', 'E', 'S', 'T', ' ', 'S', 'C', 'E', 'N', 'A', 'R', 'I',
    'O', ',', 'A', ',', '0', '7', '/', '0', '7', '/', '2', '0', '1', '6', ',', '0', '7', '/', '0', '7', '/', '2', '0', '1', 'A', ',', ',', 'Y')

  val inputLineWithoutHeader = lineListFromCalculationRequestLine(calcLine2)

  implicit class BulkRequestHelpers(request: BulkCalculationRequest) {
    def getFirstValid = request.calculationRequests.head.validCalculationRequest.get
  }

  object TestBulkRequestCreationService extends BulkRequestCreationService(app.injector.instanceOf[Environment], app.configuration,messages,servicesConfig,messagesAPI) {

    override val MAX_LINES: Int = 2

    override def sourceData(resourceLocation: String): Iterator[Char] = {

      resourceLocation.substring(resourceLocation.lastIndexOf('/') + 1) match {
        case "1" => inputLine1.iterator
        case "2" => inputLine2.iterator
        case "3" => (inputLine1 ::: inputLineWithoutHeader).iterator
        case "4" => inputLine3.iterator
        case "5" => inputLine4.iterator
        case "7" => invalidInputLine.iterator
        case "8" => anotherInputLine.iterator
        case "9" => anotherInputLineNoDualCalc.iterator
        case "10" => inputLine10.iterator
        case "11" => inputLine11.iterator
        case "12" => inputLine12.iterator
        case "13" => inputLineWithoutData.iterator
        case "14" => inputLine14.iterator
        case "15" => inputLineWithInvalidNumber.iterator
        case "16" => inputLine16.iterator
        case "17" => inputLine17.iterator
        case "18" => inputLine18.iterator
        case "19" => inputLine19.iterator
        case "20" => inputLine20.iterator
        case "21" => inputLine21.iterator
        case "22" => inputLine22.iterator
        case "23" => inputLine23.iterator
        case "24" => inputLine24.iterator
        case "25" => inputLine25.iterator
        case "26" => dataLineWith3Calcs.iterator
      }
    }

  }

  "Bulk Request Creation Service" must {

    val localDateTime = new LocalDateTime(2016, 5, 18, 17, 50, 55, 511)
    val collection = "gmp"

    val bulkRequest1 = BulkCalculationRequest("1", "bill@bixby.com", "uploadRef1", List(calcLine1), "", localDateTime)
    val bulkRequest2 = BulkCalculationRequest("2", "timburton@scary.com", "uploadRef2", List(calcLine2))
    val bulkRequest3 = BulkCalculationRequest("3", "lou@ferrigno.com", "uploadRef3", List(calcLine1, calcLine2.copy(lineId = 2)))
    val bulkRequest4 = BulkCalculationRequest("4", "bill@bixby.com", "uploadRef1", List(calcLine3))
    val invalidBulkRequest = BulkCalculationRequest("7", "bill@bixbt.com", "uploadRef1", List(invalidCalcLine))
    val anotherBulkRequest = BulkCalculationRequest("8", "somebody@mail.com", "uploadRef22", List(calcLine8))
    val bulkRequest10 = BulkCalculationRequest("10", "somebody@mail.com", "uploadRef22", List(calcLine10))
    val bulkRequest11 = BulkCalculationRequest("11", "somebody@mail.com", "uploadRef22", List(calcLine11))
    val bulkRequest12 = BulkCalculationRequest("12", "somebody@mail.com", "uploadRef22", List(calcLine12))
    val bulkRequest14 = BulkCalculationRequest("14", "timburton@scary.com", "uploadRef14", List(calcLine14))
    val bulkRequest16 = BulkCalculationRequest("16", "timburton@scary.com", "uploadRef16", List(calcLine16))
    val bulkRequest17 = BulkCalculationRequest("17", "timburton@scary.com", "uploadRef2", List(calcLine17))
    val bulkRequest18 = BulkCalculationRequest("18", "timburton@scary.com", "uploadRef2", List(calcLine18))
    val bulkRequest19 = BulkCalculationRequest("19", "timburton@scary.com", "uploadRef2", List(calcLine19))
    val bulkRequest20 = BulkCalculationRequest("20", "timburton@scary.com", "uploadRef2", List(calcLine20))
    val bulkRequest21 = BulkCalculationRequest("21", "timburton@scary.com", "uploadRef2", List(calcLine21))
    val bulkRequest22 = BulkCalculationRequest("22", "timburton@scary.com", "uploadRef2", List(calcLine22))
    val bulkRequest23 = BulkCalculationRequest("23", "timburton@scary.com", "uploadRef2", List(calcLine23))
    val bulkRequest24 = BulkCalculationRequest("24", "timburton@scary.com", "uploadRef", List(calcLine24))

    "return Bulk Request 1" in {

      val bulkRequest: BulkCalculationRequest = TestBulkRequestCreationService.createBulkRequest(upscanCallback("1"), bulkRequest1.email, bulkRequest1.reference).right.get

      bulkRequest.getFirstValid.firstForename mustBe "ISAMBARD"
      bulkRequest.getFirstValid.surname mustBe "BRUNELL"
    }

    "a request without SM as the termination date, should set memberIsInScheme to false" in {
      val request = TestBulkRequestCreationService.createBulkRequest(upscanCallback("1"), bulkRequest1.email, bulkRequest1.reference).right.get

      request.getFirstValid.memberIsInScheme mustBe false
    }

    "return Bulk Request 2" in {

      val bulkRequest: BulkCalculationRequest = TestBulkRequestCreationService.createBulkRequest(upscanCallback("2"), bulkRequest2.email, bulkRequest2.reference).right.get

      bulkRequest.getFirstValid.surname mustBe "O'BRIEN"
    }

    "return Bulk Request with multiple calcs" in {
      val bulkRequest: BulkCalculationRequest = TestBulkRequestCreationService.createBulkRequest(upscanCallback("3"), bulkRequest3.email, bulkRequest3.reference).right.get

      bulkRequest.calculationRequests.size mustBe 2
    }

    "contain Nones when dates are left blank" in {

      val bulkRequest: BulkCalculationRequest = TestBulkRequestCreationService.createBulkRequest(upscanCallback("4"), bulkRequest4.email, bulkRequest4.reference).right.get

      bulkRequest.getFirstValid.revaluationDate mustBe None
      bulkRequest.getFirstValid.terminationDate mustBe None
    }

    "termination date for person who is still in scheme should be same as reval date" in {

      val bulkRequest: BulkCalculationRequest = TestBulkRequestCreationService.createBulkRequest(upscanCallback("11"), bulkRequest11.email, bulkRequest11.reference).right.get

      bulkRequest.getFirstValid.revaluationDate mustBe bulkRequest.getFirstValid.terminationDate
    }

    "termination date for person with blank input termination date should be None" in {

      val bulkRequest: BulkCalculationRequest = TestBulkRequestCreationService.createBulkRequest(upscanCallback("11"), bulkRequest10.email, bulkRequest10.reference).right.get

      bulkRequest.getFirstValid.terminationDate mustBe None
    }

    "termination date for person who left scheme post-2016 should be correct date entered" in {

      val bulkRequest: BulkCalculationRequest = TestBulkRequestCreationService.createBulkRequest(upscanCallback("12"), bulkRequest12.email, bulkRequest12.reference).right.get

      bulkRequest.getFirstValid.revaluationDate mustBe Some("2017-07-07")
      bulkRequest.getFirstValid.terminationDate mustBe Some("2016-07-07")
      bulkRequest.getFirstValid.nino mustBe "GY000002A"
    }

    "contain Nones for other options" in {

      val bulkRequest: BulkCalculationRequest = TestBulkRequestCreationService.createBulkRequest(upscanCallback("5"), bulkRequest4.email, bulkRequest4.reference).right.get

      bulkRequest.getFirstValid.revaluationDate mustBe None
      bulkRequest.getFirstValid.terminationDate mustBe None
      bulkRequest.getFirstValid.calctype mustBe None
      bulkRequest.getFirstValid.memberReference mustBe None
      bulkRequest.getFirstValid.revaluationRate mustBe None
    }

    "return Bulk Request with correct line numbers" in {
      val bulkRequest: BulkCalculationRequest = TestBulkRequestCreationService.createBulkRequest(upscanCallback("3"), bulkRequest3.email, bulkRequest3.reference).right.get

      bulkRequest.calculationRequests.head.lineId mustBe 1
      bulkRequest.calculationRequests.tail.head.lineId mustBe 2
    }

    "return bulk requests with field validation messages" in {
      val bulkRequest = TestBulkRequestCreationService.createBulkRequest(upscanCallback("7"), invalidBulkRequest.email, invalidBulkRequest.reference).right.get
      val errors = bulkRequest.calculationRequests.head.validationErrors

      errors mustBe defined
      errors.get.isDefinedAt(BulkRequestCsvColumn.SCON.toString) mustBe true
    }

    "cope with invalid number and date" in {
      val bulkRequest = TestBulkRequestCreationService.createBulkRequest(upscanCallback("15"), invalidBulkRequest.email, invalidBulkRequest.reference).right.get
      val errors = bulkRequest.calculationRequests.head.validationErrors

      errors mustBe defined
      errors.get.isDefinedAt(BulkRequestCsvColumn.CALC_TYPE.toString) mustBe true
    }

    "return bulk requests with general line error" in {
      val bulkRequest = TestBulkRequestCreationService.createBulkRequest(upscanCallback("13"), invalidBulkRequest.email, invalidBulkRequest.reference).right.get
      val errors = bulkRequest.calculationRequests.head.validationErrors

      errors mustBe defined
      errors.get.isDefinedAt(BulkRequestCsvColumn.LINE_ERROR_TOO_FEW.toString) mustBe true
    }

    "return bulk request with dual calc Yes" in {
      val bulkRequest = TestBulkRequestCreationService.createBulkRequest(upscanCallback("8"), anotherBulkRequest.email, anotherBulkRequest.reference).right.get
      bulkRequest.calculationRequests.head.validCalculationRequest.head.dualCalc must be(1)
    }

    "return bulk request with dual calc No" in {
      val bulkRequest = TestBulkRequestCreationService.createBulkRequest(upscanCallback("9"), "somebody@email.com", "a-ref-123").right.get
      bulkRequest.calculationRequests.head.validCalculationRequest.head.dualCalc must be(0)
    }

    "trim spaces around fields" in {
      val bulkRequest = TestBulkRequestCreationService.createBulkRequest(upscanCallback("14"), bulkRequest14.email, bulkRequest14.reference).right.get
      bulkRequest.getFirstValid.firstForename mustBe "TIM"
      bulkRequest.getFirstValid.surname mustBe "O'BRIEN"
      bulkRequest.getFirstValid.nino mustBe "GY000002A"
      bulkRequest.getFirstValid.memberReference mustBe Some("GS")
      bulkRequest.getFirstValid.revaluationDate mustBe Some("2010-01-01")

    }

    "cope with termination date with wrong format dd MM yyyy with no slashes" in {
      val bulkRequest = TestBulkRequestCreationService.createBulkRequest(upscanCallback("16"), bulkRequest16.email, bulkRequest16.reference).right.get
      bulkRequest.getFirstValid.firstForename mustBe "TIM"
    }

    "convert scon to uppercase" in {
      val bulkRequest = TestBulkRequestCreationService.createBulkRequest(upscanCallback("17"), bulkRequest17.email, bulkRequest17.reference).right.get
      bulkRequest.getFirstValid.scon mustBe "S1301234T"
    }

    "not send the revaluation date if provided when calculation type is leaving" in {
      val bulkRequest = TestBulkRequestCreationService.createBulkRequest(upscanCallback("18"), bulkRequest18.email, bulkRequest18.reference).right.get
      bulkRequest.getFirstValid.revaluationDate mustBe None
      bulkRequest.getFirstValid.revaluationRate mustBe None
    }

    "not send the revaluation date if provided when calculation type is PA" in {
      val bulkRequest = TestBulkRequestCreationService.createBulkRequest(upscanCallback("20"), bulkRequest20.email, bulkRequest20.reference).right.get
      bulkRequest.getFirstValid.revaluationDate mustBe None
    }

    "not send the revaluation date if provided when calculation type is SPA" in {
      val bulkRequest = TestBulkRequestCreationService.createBulkRequest(upscanCallback("21"), bulkRequest21.email, bulkRequest21.reference).right.get
      bulkRequest.getFirstValid.revaluationDate mustBe None
    }

    "not send the termination date if SM is provided, when calculation type is survivor" in {
      val bulkRequest = TestBulkRequestCreationService.createBulkRequest(upscanCallback("24"), bulkRequest24.email, bulkRequest24.reference).right.get
      bulkRequest.getFirstValid.terminationDate mustBe None
    }

    "should set the schemeMember property to true" in {
      val bulkRequest = TestBulkRequestCreationService.createBulkRequest(upscanCallback("24"), bulkRequest24.email, bulkRequest24.reference).right.get
      bulkRequest.getFirstValid.memberIsInScheme mustBe true
    }

    "not send dual calc when calculation type is survivor" in {
      val bulkRequest = TestBulkRequestCreationService.createBulkRequest(upscanCallback("19"), bulkRequest19.email, bulkRequest19.reference).right.get
      bulkRequest.getFirstValid.dualCalc mustBe 0
    }

    "send term date when term date is on or after 06/04/2016" in {
      val bulkRequest = TestBulkRequestCreationService.createBulkRequest(upscanCallback("22"), bulkRequest22.email, bulkRequest22.reference).right.get
      bulkRequest.getFirstValid.terminationDate mustBe Some("2016-04-06")
    }

    "not send term date when term date is on or before 05/04/2016" in {
      val bulkRequest = TestBulkRequestCreationService.createBulkRequest(upscanCallback("23"), "23", bulkRequest23.email).right.get
      bulkRequest.getFirstValid.terminationDate mustBe None
    }

    "should send through a LINE_EMPTY validation error when there are no calculations to perform" in {
      val bulkRequest = TestBulkRequestCreationService.createBulkRequest(upscanCallback("25"), "25", "tim@burton.com").right.get
      val errors = bulkRequest.calculationRequests.head.validationErrors

      errors mustBe defined
      errors.get.isDefinedAt(BulkRequestCsvColumn.LINE_ERROR_EMPTY.toString) mustBe true
    }

    "return an exception when uploading more than the max number of lines" in {

      object TestService extends BulkRequestCreationService(app.injector.instanceOf[Environment], app.configuration,messages,servicesConfig,messagesAPI) {
        override val MAX_LINES = 2
        override def sourceData(resourceLocation: String): Iterator[Char] = dataLineWith3Calcs.iterator
      }

      val result = TestService.createBulkRequest(upscanCallback("ref26"), "26", "tim@burton.com")

      result.isLeft mustBe true
    }

    "allow uploading right up to the line limit" in {

      object TestService extends BulkRequestCreationService(app.injector.instanceOf[Environment], app.configuration,messages,servicesConfig,messagesAPI) {
        override val MAX_LINES = 3
        override def sourceData(resourceLocation: String): Iterator[Char] = dataLineWith3Calcs.iterator
      }

      val result = TestService.createBulkRequest(upscanCallback("26"), "26", "tim@burton.com")

      result.isRight mustBe true
    }

    "IncorrectlyEncodedException should be thrown when Incorrectly encoded file is uploaded" in {

      object TestService extends BulkRequestCreationService(app.injector.instanceOf[Environment], app.configuration,messages,servicesConfig,messagesAPI) {
        override def sourceData(resourceLocation: String): Iterator[Char] = {
          val is = new ByteArrayInputStream(Charset.forName("utf-16").encode("test-data").array())
          Source.fromInputStream(is, "utf-8")
        }

      }

      val bulkRequest = TestService.createBulkRequest(upscanCallback("ref26"), "26", "tim@burton.com")
      assert(bulkRequest == Left(IncorrectlyEncodedException))
    }
  }

  def lineListFromCalculationRequestLine(line: BulkCalculationRequestLine): List[Char] = {
    val l = line.validCalculationRequest.get.productIterator.toList.zipWithIndex
    val dateRegEx = """([0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9])""".r

    {
      (l collect {
        case (None, i) => ""
        case (Some(dateRegEx(s)), i) => new LocalDate(s).toString("dd/MM/yyyy")
        case (x: Int, BulkRequestCsvColumn.DUAL_CALC) => x match {
          case 1 => "Y";
          case _ => "N"
        }
        case (Some(x), i) => s"$x"
        case (x: String, i) => x
      })
        .mkString(",") + "\n"
    }
      .toCharArray.toList

  }
}
