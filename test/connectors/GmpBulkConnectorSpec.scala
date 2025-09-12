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

package connectors

import java.util.UUID
import helpers.RandomNino
import models._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfter
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Environment
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.http._
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.net.URL
import scala.concurrent.Future

class GmpBulkConnectorSpec extends HttpClientV2Helper with GuiceOneServerPerSuite with BeforeAndAfter {

  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  val link = "some-link"
  val psaId = "B1234567"

  object testGmpBulkConnector extends GmpBulkConnector(
    app.injector.instanceOf[Environment],
    app.configuration,
    mockHttp,
    app.injector.instanceOf[ServicesConfig])

  when(mockHttp.get(any[URL])(any[HeaderCarrier])).thenReturn(requestBuilder)
  when(mockHttp.post(any[URL])(any[HeaderCarrier])).thenReturn(requestBuilder)

  "The GMP Bulk Connector" must {

    implicit val hc = new HeaderCarrier(sessionId = Some(SessionId(s"session-${UUID.randomUUID}")))

    "send a bulk request with valid data" in {

      requestBuilderExecute[HttpResponse](Future.successful(HttpResponse(OK, "200")))

      val bcr = BulkCalculationRequest("upload1", "jim@jarmusch.com", "idreference",
        List(BulkCalculationRequestLine(1, Some(CalculationRequestLine("S1234567C", RandomNino.generate,
          "bob", "bobbleton", Some("bobby"), Some(0), Some("2012-02-02"), None, None, 0)),
          None, None)))

      val result = testGmpBulkConnector.sendBulkRequest(bcr, link)
      result.futureValue must be(OK)

    }

    "send a bulk request with valid data but there is a duplicate" in {

      requestBuilderExecute[HttpResponse](Future.failed(UpstreamErrorResponse("Tried to insert duplicate", 409, 409)))

      val bcr = BulkCalculationRequest("upload1", "jim@jarmusch.com", "idreference",
        List(BulkCalculationRequestLine(1, Some(CalculationRequestLine("S1234567C", RandomNino.generate,
          "bob", "bobbleton", Some("bobby"), Some(0), Some("2012-02-02"), None, None, 0)),
          None, None)))

      val result = testGmpBulkConnector.sendBulkRequest(bcr, link).futureValue
      (result) must be(CONFLICT)
    }

    "send a bulk request with valid data but the file is too large" in {

      requestBuilderExecute[HttpResponse](Future.failed(UpstreamErrorResponse("File too large", 413, 413)))

      val bcr = BulkCalculationRequest("upload1", "jim@jarmusch.com", "idreference",
        List(BulkCalculationRequestLine(1, Some(CalculationRequestLine("S1234567C", RandomNino.generate,
          "bob", "bobbleton", Some("bobby"), Some(0), Some("2012-02-02"), None, None, 0)),
          None, None)))

      val result = testGmpBulkConnector.sendBulkRequest(bcr, link).futureValue
      (result) must be(REQUEST_ENTITY_TOO_LARGE)
    }

    "send a bulk request with valid data but bulk fails" in {

      requestBuilderExecute[HttpResponse](Future.failed(UpstreamErrorResponse("Failed generically", 500, 500)))

      val bcr = BulkCalculationRequest("upload1", "jim@jarmusch.com", "idreference",
        List(BulkCalculationRequestLine(1, Some(CalculationRequestLine("S1234567C", RandomNino.generate,
          "bob", "bobbleton", Some("bobby"), Some(0), Some("2012-02-02"), None, None, 0)),
          None, None)))

      val result = await(testGmpBulkConnector.sendBulkRequest(bcr, link))
      (result) must be(500)
    }

    "retrieve bulk requests associated with the user " in {

      val bulkPreviousRequest = Json.parse(
        """[{"uploadReference":"uploadRef","reference":"ref","timestamp":"2016-04-27T14:53:18.308","processedDateTime":"2016-05-18T17:50:55.511"}]"""
      )

      requestBuilderExecute[List[BulkPreviousRequest]](Future.successful(bulkPreviousRequest.as[List[BulkPreviousRequest]]))

      val result = testGmpBulkConnector.getPreviousBulkRequests(link)
      val resolvedResult = (result).futureValue

      resolvedResult.head.uploadReference must be("uploadRef")

    }

    "return a bulk results summary" in {

      requestBuilderExecute[BulkResultsSummary](Future.successful(BulkResultsSummary("test", 1, 1)))

      val result = testGmpBulkConnector.getBulkResultsSummary("", link).futureValue
      (result).reference must be("test")
    }

    "return all bulk request as csv" in {

      requestBuilderExecute[HttpResponse](Future.successful(HttpResponse(OK, "THIS IS A CSV STRING")))

      val result = testGmpBulkConnector.getResultsAsCsv("", "", link)
      val resolvedResult = (result).futureValue

      resolvedResult.body must be("THIS IS A CSV STRING")
    }

    "return all contributions and earnings as a csv" in {

      requestBuilderExecute[HttpResponse](Future.successful(HttpResponse(OK, "THIS IS A CSV STRING")))

      val result = testGmpBulkConnector.getContributionsAndEarningsAsCsv("", link)
      val resolvedResult = (result).futureValue

      resolvedResult.body must be("THIS IS A CSV STRING")

    }

  }
}
