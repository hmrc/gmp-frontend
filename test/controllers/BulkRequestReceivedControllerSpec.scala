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
import helpers.RandomNino
import models._
import org.joda.time.{LocalDateTime, LocalDate}
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
import uk.gov.hmrc.emailaddress.EmailAddress
import uk.gov.hmrc.play.frontend.auth.AuthContext
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.frontend.auth.connectors.domain._
import uk.gov.hmrc.play.http.{HttpResponse, HeaderCarrier}
import uk.gov.hmrc.play.http.logging.SessionId

import scala.concurrent.Future

class BulkRequestReceivedControllerSpec extends PlaySpec with OneServerPerSuite with MockitoSugar with GmpUsers {
  val mockAuthConnector = mock[AuthConnector]
  val mockSessionService = mock[SessionService]
  val mockBulkRequestCreationService = mock[BulkRequestCreationService]
  val mockGmpBulkConnector = mock[GmpBulkConnector]

  implicit val user = AuthContext(authority = Authority("1234", Accounts(psa = Some(PsaAccount("link", PsaId("B1234567")))), None, None, CredentialStrength.None, ConfidenceLevel.L50))
  implicit val hc = new HeaderCarrier(sessionId = Some(SessionId(s"session-${UUID.randomUUID}")))

  val callBackData = CallBackData("AAAAA", "11111", 1L, Some("Ted"), Some("application/json"), "YYYYYYY", None)
  val gmpBulkSession = GmpBulkSession(Some(callBackData), Some(EmailAddress("somebody@somewhere.com")), Some("reference"))

  val calcLine1 = BulkCalculationRequestLine(1, Some(CalculationRequestLine("S1301234T", RandomNino.generate, "Isambard", "Brunell", Some("IB"), Some(1), Some("2010-02-02"), Some("2010-01-01"), Some(1), 0)),None, None)


  val inputLine1 = lineListFromCalculationRequestLine(calcLine1)
  val bulkRequest1 = BulkCalculationRequest("1", "bill@bixby.com", "uploadRef1", List(calcLine1), "userid", LocalDateTime.now() )

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

          when(mockSessionService.fetchGmpBulkSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpBulkSession)))
          when(mockBulkRequestCreationService.createBulkRequest(Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any())).thenReturn(bulkRequest1)
          when(mockGmpBulkConnector.sendBulkRequest(Matchers.any())(Matchers.any(),Matchers.any())).thenReturn(Future.successful(HttpResponse(200)))
          withAuthorisedUser { user =>
            getBulkRequestReceived(user) { result =>
              status(result) must equal(OK)
              contentAsString(result) must include(Messages("gmp.bulk_request_received.title"))
              contentAsString(result) must include(Messages("gmp.bulk_request_received.banner"))
              contentAsString(result) must include(Messages("gmp.bulk_request_received.header"))
              contentAsString(result) must include(Messages("gmp.bulk_request_received.text", bulkRequest1.reference))
              contentAsString(result) must include(Messages("gmp.bulk_request_received.button"))
              contentAsString(result) must include(Messages("gmp.bulk_request_received_dashboard_link"))
            }
          }
        }

        "throw exception when fails to get session" in {

          when(mockSessionService.fetchGmpBulkSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))

          withAuthorisedUser { user =>
            getBulkRequestReceived(user) { result =>
              intercept[RuntimeException] {
                status(result) must equal(OK)
              }

            }
          }
        }

      }
    }
  }

  def getBulkRequestReceived(request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest())(handler: Future[Result] => Any): Unit = {
    handler(TestBulkRequestReceivedController.get.apply(request))
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
        case x: Int => x.toString + ","
      }
    }
    {
      for (p <- l) yield process(p)
    }.flatten :+ 10.toByte.toChar
  }
}
