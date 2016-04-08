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

package controllers

import java.util.UUID

import connectors.GmpBulkConnector
import models.{CallBackData, GmpBulkSession}
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.{OneServerPerSuite, PlaySpec}
import play.api.i18n.Messages
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{BulkRequestCreationService, SessionService}
import uk.gov.hmrc.domain.PsaId
import uk.gov.hmrc.play.frontend.auth.AuthContext
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.frontend.auth.connectors.domain._
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.http.logging.SessionId

import scala.concurrent.Future

class BulkRequestReceivedControllerSpec extends PlaySpec with OneServerPerSuite with MockitoSugar with GmpUsers {
  val mockAuthConnector = mock[AuthConnector]
  val mockSessionService = mock[SessionService]
  val mockBulkRequestCreationService = mock[BulkRequestCreationService]
  val mockGmpBulkConnector = mock[GmpBulkConnector]

  implicit val user = AuthContext(authority = Authority("1234", Accounts(psa = Some(PsaAccount("link", PsaId("B1234567")))), None, None, CredentialStrength.None, ConfidenceLevel.L50))
  implicit val hc = new HeaderCarrier(sessionId = Some(SessionId(s"session-${UUID.randomUUID}")))

  object TestBulkRequestReceivedController extends BulkRequestReceivedController {
    val authConnector = mockAuthConnector
    override val sessionService = mockSessionService
    override val bulkRequestCreationService = mockBulkRequestCreationService
    override val gmpBulkConnector = mockGmpBulkConnector
  }

  "BulkRequestReceivedController" must {

    "respond to GET /guaranteed-minimum-pension/request-received" in {
      val result = route(FakeRequest(GET, "/guaranteed-minimum-pension/request-received"))
      status(result.get) must not equal (NOT_FOUND)
    }

    "request recevied GET " must {

      "be authorised" in {
        getBulkRequestReceived() { result =>
          status(result) must equal(SEE_OTHER)
          redirectLocation(result).get must include("/account/sign-in")
        }
      }

      "authenticated users" must {

        "respond with ok" in {
          withAuthorisedUser { user =>
            getBulkRequestReceived(user) { result =>
              status(result) must equal(OK)
              contentAsString(result) must include(Messages("gmp.bulk_request_received.title"))
              contentAsString(result) must include(Messages("gmp.bulk_request_received.banner"))
              contentAsString(result) must include(Messages("gmp.bulk_request_received.header"))
              contentAsString(result) must include(Messages("gmp.bulk_request_received.text"))
              contentAsString(result) must include(Messages("gmp.bulk_request_received.button"))
              contentAsString(result) must include(Messages("gmp.bulk_request_received_dashboard_link"))
            }
          }
        }
        
      }
    }
  }

  def getBulkRequestReceived(request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest())(handler: Future[Result] => Any): Unit = {
    handler(TestBulkRequestReceivedController.get.apply(request))
  }
}
