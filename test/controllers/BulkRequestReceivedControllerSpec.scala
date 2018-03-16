/*
 * Copyright 2018 HM Revenue & Customs
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
import org.joda.time.{LocalDate, LocalDateTime}
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.{OneServerPerSuite, PlaySpec}
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{BulkRequestCreationService, DataLimitExceededException, SessionService}
import uk.gov.hmrc.domain.PsaId
import uk.gov.hmrc.emailaddress.EmailAddress
import uk.gov.hmrc.play.frontend.auth.AuthContext
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.frontend.auth.connectors.domain._

import scala.concurrent.Future
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.logging.SessionId

class BulkRequestReceivedControllerSpec extends PlaySpec with OneServerPerSuite with MockitoSugar with GmpUsers {
  val mockAuthConnector = mock[AuthConnector]
  val mockSessionService = mock[SessionService]
  val mockBulkRequestCreationService = mock[BulkRequestCreationService]
  val mockGmpBulkConnector = mock[GmpBulkConnector]

  implicit val user = AuthContext(authority = Authority("1234", Accounts(psa = Some(PsaAccount("link", PsaId("B1234567")))), None, None, CredentialStrength.None, ConfidenceLevel.L50, None, None, None, ""))
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
    override val context = FakeGmpContext()
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
          redirectLocation(result).get must include("/gg/sign-in")
        }
      }

      "authenticated users" must {

        "respond with ok" in {

          when(mockSessionService.fetchGmpBulkSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpBulkSession)))
          when(mockBulkRequestCreationService.createBulkRequest(Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any())).thenReturn(Left(bulkRequest1))
          when(mockGmpBulkConnector.sendBulkRequest(Matchers.any())(Matchers.any(),Matchers.any())).thenReturn(Future.successful(OK))
          withAuthorisedUser { user =>
            getBulkRequestReceived(user) { result =>
              status(result) must equal(OK)
              contentAsString(result) must include(Messages("gmp.bulk_request_received.title"))
              contentAsString(result) must include(Messages("gmp.bulk_request_received.banner"))
              contentAsString(result) must include(Messages("gmp.bulk_request_received.header"))
              contentAsString(result) must include(Messages("gmp.bulk_request_received.text", bulkRequest1.reference))
              contentAsString(result) must include(Messages("gmp.bulk_request_received.button"))
              contentAsString(result) must include(Messages("gmp.back_to_dashboard"))
            }
          }
        }

        "respond with ok and failure page if conflict received usually for a duplicate record trying to be inserted" in {

          when(mockSessionService.fetchGmpBulkSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpBulkSession)))
          when(mockBulkRequestCreationService.createBulkRequest(Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any())).thenReturn(Left(bulkRequest1))
          when(mockGmpBulkConnector.sendBulkRequest(Matchers.any())(Matchers.any(),Matchers.any())).thenReturn(Future.successful(CONFLICT))
          withAuthorisedUser { user =>
            getBulkRequestReceived(user) { result =>
              status(result) must equal(OK)
              contentAsString(result) must include(Messages("gmp.bulk.failure.duplicate_upload"))
              contentAsString(result) must include(Messages("gmp.bulk_failure_duplicate.title"))
            }
          }
        }

        "respond with ok and failure page if file too large" in {

          when(mockSessionService.fetchGmpBulkSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpBulkSession)))
          when(mockBulkRequestCreationService.createBulkRequest(Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any())).thenReturn(Left(bulkRequest1))
          when(mockGmpBulkConnector.sendBulkRequest(Matchers.any())(Matchers.any(),Matchers.any())).thenReturn(Future.successful(REQUEST_ENTITY_TOO_LARGE))
          withAuthorisedUser { user =>
            getBulkRequestReceived(user) { result =>
              status(result) must equal(OK)
              contentAsString(result) must include(Messages("gmp.bulk.failure.too_large"))
              contentAsString(result) must include(Messages("gmp.bulk_failure_file_too_large.title"))
            }
          }
        }

        "respond with ok and failure page if file row limit exceeded" in {

          when(mockSessionService.fetchGmpBulkSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpBulkSession)))
          when(mockBulkRequestCreationService.createBulkRequest(Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any())).thenReturn(Right(new DataLimitExceededException))


          withAuthorisedUser { user =>
            getBulkRequestReceived(user) { result =>
              status(result) must equal(OK)
              contentAsString(result) must include(Messages("gmp.bulk.failure.too_large"))
              contentAsString(result) must include(Messages("gmp.bulk_failure_file_too_large.title"))
            }
          }
        }

        "generic failure page if bulk fails for 5XX reason" in {

          when(mockSessionService.fetchGmpBulkSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpBulkSession)))
          when(mockBulkRequestCreationService.createBulkRequest(Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any())).thenReturn(Left(bulkRequest1))
          when(mockGmpBulkConnector.sendBulkRequest(Matchers.any())(Matchers.any(),Matchers.any())).thenReturn(Future.successful(500))
          withAuthorisedUser { user =>
            getBulkRequestReceived(user) { result =>
              status(result) must equal(OK)
              contentAsString(result) must include(Messages("gmp.bulk.failure.generic"))
              contentAsString(result) must include(Messages("gmp.bulk_failure_generic.title"))
            }
          }
        }

        "throw exception when fails to get session" in {

          when(mockSessionService.fetchGmpBulkSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))

          withAuthorisedUser { user =>
            getBulkRequestReceived(user) { result =>
              contentAsString(result)replaceAll("&#x27;", "'") must include (Messages("gmp.cannot_calculate.gmp"))
              contentAsString(result) must include (Messages("gmp.error.session_parts_missing", "/guaranteed-minimum-pension/upload-csv"))
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
        case x: Boolean => x.toString + ","
      }
    }
    {
      for (p <- l) yield process(p)
    }.flatten :+ 10.toByte.toChar
  }
}
