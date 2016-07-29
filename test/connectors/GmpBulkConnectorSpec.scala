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

package connectors

import java.util.UUID

import helpers.RandomNino
import models._
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfter
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.{OneServerPerSuite, PlaySpec}
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.domain.PsaId
import uk.gov.hmrc.play.frontend.auth.AuthContext
import uk.gov.hmrc.play.frontend.auth.connectors.domain._
import uk.gov.hmrc.play.http._
import uk.gov.hmrc.play.http.logging.SessionId
import scala.concurrent.Future

class GmpBulkConnectorSpec extends PlaySpec with OneServerPerSuite with MockitoSugar with BeforeAndAfter {

  val mockHttpPost = mock[HttpPost]
  val mockHttpGet = mock[HttpGet]
  val link = "some-link"
  val psaId = "B1234567"

  object testGmpBulkConnector extends GmpBulkConnector {
    override val httpPost: HttpPost = mockHttpPost
    override val httpGet: HttpGet = mockHttpGet
  }


  "The GMP Bulk Connector" must {

    implicit val hc = new HeaderCarrier(sessionId = Some(SessionId(s"session-${UUID.randomUUID}")))

    "send a bulk request with valid data" in {
      implicit val user = AuthContext(authority = Authority("1234", Accounts(psa = Some(PsaAccount(link, PsaId(psaId)))), None, None, CredentialStrength.None, ConfidenceLevel.L50))
      when(mockHttpPost.POST[BulkCalculationRequest, HttpResponse](Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(responseStatus = OK)))

      val bcr = BulkCalculationRequest("upload1", "jim@jarmusch.com", "idreference",
        List(BulkCalculationRequestLine(1, Some(CalculationRequestLine("S1234567C", RandomNino.generate,
          "bob", "bobbleton", Some("bobby"), Some(0), Some("2012-02-02"), None, None, 0)),
          None, None)))

      val result = testGmpBulkConnector.sendBulkRequest(bcr)
      (await(result)) must be(OK)

    }

    "send a bulk request with valid data but there is a duplicate" in {
      implicit val user = AuthContext(authority = Authority("1234", Accounts(psa = Some(PsaAccount(link, PsaId(psaId)))), None, None, CredentialStrength.None, ConfidenceLevel.L50))
      when(mockHttpPost.POST[BulkCalculationRequest, HttpResponse](Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(responseStatus = CONFLICT)))

      val bcr = BulkCalculationRequest("upload1", "jim@jarmusch.com", "idreference",
        List(BulkCalculationRequestLine(1, Some(CalculationRequestLine("S1234567C", RandomNino.generate,
          "bob", "bobbleton", Some("bobby"), Some(0), Some("2012-02-02"), None, None, 0)),
          None, None)))

      val result = testGmpBulkConnector.sendBulkRequest(bcr)
      (await(result)) must be(CONFLICT)
    }

    "send a bulk request with valid data but the file is too large" in {
      implicit val user = AuthContext(authority = Authority("1234", Accounts(psa = Some(PsaAccount(link, PsaId(psaId)))), None, None, CredentialStrength.None, ConfidenceLevel.L50))
      when(mockHttpPost.POST[BulkCalculationRequest, HttpResponse](Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(responseStatus = REQUEST_ENTITY_TOO_LARGE)))

      val bcr = BulkCalculationRequest("upload1", "jim@jarmusch.com", "idreference",
        List(BulkCalculationRequestLine(1, Some(CalculationRequestLine("S1234567C", RandomNino.generate,
          "bob", "bobbleton", Some("bobby"), Some(0), Some("2012-02-02"), None, None, 0)),
          None, None)))

      val result = testGmpBulkConnector.sendBulkRequest(bcr)
      (await(result)) must be(REQUEST_ENTITY_TOO_LARGE)
    }

    "send a bulk request with valid data but bulk fails" in {
      implicit val user = AuthContext(authority = Authority("1234", Accounts(psa = Some(PsaAccount(link, PsaId(psaId)))), None, None, CredentialStrength.None, ConfidenceLevel.L50))
      when(mockHttpPost.POST[BulkCalculationRequest, HttpResponse](Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.failed(new Upstream5xxResponse("Failed generically", 500, 500)))

      val bcr = BulkCalculationRequest("upload1", "jim@jarmusch.com", "idreference",
        List(BulkCalculationRequestLine(1, Some(CalculationRequestLine("S1234567C", RandomNino.generate,
          "bob", "bobbleton", Some("bobby"), Some(0), Some("2012-02-02"), None, None, 0)),
          None, None)))

      val result = testGmpBulkConnector.sendBulkRequest(bcr)
      (await(result)) must be(500)
    }

    "retrieve bulk requests associated with the user " in {

      implicit val user = AuthContext(authority = Authority("1234", Accounts(psa =
        Some(PsaAccount(link, PsaId(psaId)))), None, None, CredentialStrength.None, ConfidenceLevel.L50))

      val bulkPreviousRequest = Json.parse(
        """[{"uploadReference":"uploadRef","reference":"ref","timestamp":"2016-04-27T14:53:18.308","processedDateTime":"2016-05-18T17:50:55.511"}]"""
      )

      when(mockHttpGet.GET[List[BulkPreviousRequest]]( Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(bulkPreviousRequest.as[List[BulkPreviousRequest]]))

      val result = testGmpBulkConnector.getPreviousBulkRequests
      val resolvedResult = await(result)

      resolvedResult.head.uploadReference must be("uploadRef")

    }

    "return a bulk results summary" in {
      implicit val user = AuthContext(authority = Authority("1234", Accounts(psa = Some(PsaAccount(link, PsaId(psaId)))), None, None, CredentialStrength.None, ConfidenceLevel.L50))
      when(mockHttpGet.GET[BulkResultsSummary](Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(BulkResultsSummary("test",1,1)))

      val result = testGmpBulkConnector.getBulkResultsSummary("")
      (await(result)).reference must be("test")
    }

    "return all bulk request as csv" in {

      implicit val user = AuthContext(authority = Authority("1234", Accounts(psa = Some(PsaAccount(link, PsaId(psaId)))), None, None, CredentialStrength.None, ConfidenceLevel.L50))
      when(mockHttpGet.GET[HttpResponse](Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(HttpResponse(responseStatus = OK,responseString = Some("THIS IS A CSV STRING"))))

      val result = testGmpBulkConnector.getResultsAsCsv("","")
      val resolvedResult = await(result)

      resolvedResult.body must be("THIS IS A CSV STRING")
    }

    "return all contributions and earnings as a csv" in {

      implicit val user = AuthContext(authority = Authority("1234", Accounts(psa = Some(PsaAccount(link, PsaId(psaId)))), None, None, CredentialStrength.None, ConfidenceLevel.L50))
      when(mockHttpGet.GET[HttpResponse](Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(HttpResponse(responseStatus = OK,responseString = Some("THIS IS A CSV STRING"))))

      val result = testGmpBulkConnector.getContributionsAndEarningsAsCsv("")
      val resolvedResult = await(result)

      resolvedResult.body must be("THIS IS A CSV STRING")

    }

  }

}
