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
import play.api.test.Helpers._
import uk.gov.hmrc.domain.PsaId
import uk.gov.hmrc.play.frontend.auth.AuthContext
import uk.gov.hmrc.play.frontend.auth.connectors.domain._
import uk.gov.hmrc.play.http.{HttpResponse, HttpPost, HeaderCarrier}
import uk.gov.hmrc.play.http.logging.SessionId

import scala.concurrent.Future

class GmpBulkConnectorSpec extends PlaySpec with OneServerPerSuite with MockitoSugar with BeforeAndAfter {

  val mockHttpPost = mock[HttpPost]
  val psaId = "B1234567"

  object testGmpBulkConnector extends GmpBulkConnector {
    override val httpPost: HttpPost = mockHttpPost

  }


  "The GMP Bulk Connector" must {

    implicit val hc = new HeaderCarrier(sessionId = Some(SessionId(s"session-${UUID.randomUUID}")))

    "send a bulk request with valid data" in {
      implicit val user = AuthContext(authority = Authority("1234", Accounts(psa = Some(PsaAccount("link", PsaId(psaId)))), None, None, CredentialStrength.None, ConfidenceLevel.L50))
      when(mockHttpPost.POST[BulkCalculationRequest, HttpResponse](Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful((HttpResponse(responseStatus = OK))))

      val bcr = BulkCalculationRequest("upload1", "jim@jarmusch.com", "idreference",
        List(BulkCalculationRequestLine(1, Some(CalculationRequestLine("S1234567C", RandomNino.generate,
          "bob", "bobbleton", Some("bobby"), Some(0), Some("2012-02-02"), None, None, None)),
          None)))
      val result = testGmpBulkConnector.sendBulkRequest(bcr)
      (await(result)).status must be(OK)

    }
  }

}