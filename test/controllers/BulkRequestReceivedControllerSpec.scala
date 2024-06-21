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

package controllers

import java.util.UUID
import config.{ApplicationConfig, GmpSessionCache}
import connectors.GmpBulkConnector
import controllers.auth.{AuthAction, FakeAuthAction}
import helpers.RandomNino
import models._
import models.upscan.UploadedSuccessfully

import java.time.{LocalDate, LocalDateTime}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{BulkRequestCreationService, DataLimitExceededException, SessionService}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.emailaddress.EmailAddress
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.SessionId
import views.Views

import java.time.format.DateTimeFormatter
import scala.concurrent.{ExecutionContext, Future}

class BulkRequestReceivedControllerSpec extends PlaySpec with GuiceOneServerPerSuite with MockitoSugar {
  val mockAuthConnector = mock[AuthConnector]
  val mockSessionService = mock[SessionService]
  val mockBulkRequestCreationService = mock[BulkRequestCreationService]
  val mockGmpBulkConnector = mock[GmpBulkConnector]
  val mockAuthAction = mock[AuthAction]

  implicit val hc: HeaderCarrier = new HeaderCarrier(sessionId = Some(SessionId(s"session-${UUID.randomUUID}")))
  implicit val mcc: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]
  implicit val ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]
  implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit val messagesProvider: MessagesImpl = MessagesImpl(Lang("en"), messagesApi)
  implicit val ac: ApplicationConfig = app.injector.instanceOf[ApplicationConfig]
  implicit val sc: GmpSessionCache = app.injector.instanceOf[GmpSessionCache]
  lazy val views = app.injector.instanceOf[Views]

  val callBackData = UploadedSuccessfully("ref1", "name1", "download1")
  val gmpBulkSession = GmpBulkSession(Some(callBackData), Some(EmailAddress("somebody@somewhere.com")), Some("reference"))

  val calcLine1 = BulkCalculationRequestLine(1, Some(CalculationRequestLine("S1301234T", RandomNino.generate, "Isambard", "Brunell", Some("IB"), Some(1), Some("2010-02-02"), Some("2010-01-01"), Some(1), 0)),None, None)

  val inputLine1 = lineListFromCalculationRequestLine(calcLine1)
  val bulkRequest1 = BulkCalculationRequest("1", "bill@bixby.com", "uploadRef1", List(calcLine1), "userid", LocalDateTime.now() )

  object TestBulkRequestReceivedController extends BulkRequestReceivedController(
    FakeAuthAction,
    mockAuthConnector,
    mockSessionService,
    mockBulkRequestCreationService,
    mockGmpBulkConnector,ac,FakeGmpContext,mcc,ec,sc,views){
  }

  "BulkRequestReceivedController" must {

    "request recevied GET " must {

      "authenticated users" must {

        "respond with ok" in {

          when(mockSessionService.fetchGmpBulkSession()(any())).thenReturn(Future.successful(Some(gmpBulkSession)))
          when(mockBulkRequestCreationService.createBulkRequest(any(),any(),any())).thenReturn(Right(bulkRequest1))
          when(mockGmpBulkConnector.sendBulkRequest(any(),any())(any())).thenReturn(Future.successful(OK))

          val result = TestBulkRequestReceivedController.get(FakeRequest())
              status(result) must equal(OK)
              contentAsString(result) must include(Messages("gmp.bulk_request_received.banner"))
              contentAsString(result) must include(Messages("gmp.bulk_request_received.banner"))
              contentAsString(result) must include(Messages("gmp.bulk_request_received.header"))
              contentAsString(result) must include(Messages("gmp.bulk_request_received.text", bulkRequest1.reference))
              contentAsString(result) must include(Messages("gmp.bulk_request_received.button"))
        }

        "respond with ok and failure page if conflict received usually for a duplicate record trying to be inserted" in {

          when(mockSessionService.fetchGmpBulkSession()(any())).thenReturn(Future.successful(Some(gmpBulkSession)))
          when(mockBulkRequestCreationService.createBulkRequest(any(),any(),any())).thenReturn(Right(bulkRequest1))
          when(mockGmpBulkConnector.sendBulkRequest(any(),any())(any())).thenReturn(Future.successful(CONFLICT))

          val result = TestBulkRequestReceivedController.get(FakeRequest())
              status(result) must equal(OK)
              contentAsString(result) must include(Messages("gmp.bulk.problem.header"))
              contentAsString(result) must include(Messages("gmp.bulk.problem.header"))
        }

        "respond with ok and failure page if file too large" in {

          when(mockSessionService.fetchGmpBulkSession()(any())).thenReturn(Future.successful(Some(gmpBulkSession)))
          when(mockBulkRequestCreationService.createBulkRequest(any(),any(),any())).thenReturn(Right(bulkRequest1))
          when(mockGmpBulkConnector.sendBulkRequest(any(),any())(any())).thenReturn(Future.successful(REQUEST_ENTITY_TOO_LARGE))

          val result = TestBulkRequestReceivedController.get(FakeRequest())
              status(result) must equal(OK)
              contentAsString(result) must include(Messages("gmp.bulk.failure.too_large"))
              contentAsString(result) must include(Messages("gmp.bulk.file_too_large.header"))
        }

        "respond with ok and failure page if file row limit exceeded" in {

          when(mockSessionService.fetchGmpBulkSession()(any())).thenReturn(Future.successful(Some(gmpBulkSession)))
          when(mockBulkRequestCreationService.createBulkRequest(any(),any(),any())).thenReturn(Left(DataLimitExceededException))

          val result = TestBulkRequestReceivedController.get(FakeRequest())
              status(result) must equal(OK)
              contentAsString(result) must include(Messages("gmp.bulk.failure.too_large"))
              contentAsString(result) must include(Messages("gmp.bulk.file_too_large.header"))
        }

        "generic failure page if bulk fails for 5XX reason" in {

          when(mockSessionService.fetchGmpBulkSession()(any())).thenReturn(Future.successful(Some(gmpBulkSession)))
          when(mockBulkRequestCreationService.createBulkRequest(any(),any(),any())).thenReturn(Right(bulkRequest1))
          when(mockGmpBulkConnector.sendBulkRequest(any(),any())(any())).thenReturn(Future.successful(500))

          val result = TestBulkRequestReceivedController.get(FakeRequest())
              status(result) must equal(OK)
              contentAsString(result) must include(Messages("gmp.bulk.failure.generic"))
              contentAsString(result) must include(Messages("gmp.bulk.problem.header"))
        }

        "throw exception when fails to get session" in {

          when(mockSessionService.fetchGmpBulkSession()(any())).thenReturn(Future.successful(None))

          val result = TestBulkRequestReceivedController.get(FakeRequest())
              contentAsString(result)replaceAll("&#x27;", "'") must include (Messages("gmp.cannot_calculate.gmp"))
              contentAsString(result) must include (Messages("gmp.error.session_parts_missing", "/guaranteed-minimum-pension/upload-csv"))
        }


        "redirect to an error page explaining they have uploaded an incorrectly encoded file when they upload an incorrectly encoded file" in {
          when(mockSessionService.fetchGmpBulkSession()(any())).thenReturn(Future.successful(Some(gmpBulkSession)))
          when(mockBulkRequestCreationService.createBulkRequest(any(),any(),any())).thenReturn(Left(new UnsupportedOperationException))

          val result = TestBulkRequestReceivedController.get(FakeRequest())

          status(result) must equal(SEE_OTHER)
          redirectLocation(result) must be (Some("/guaranteed-minimum-pension/incorrectly-encoded"))
        }
      }
    }
  }

  def lineListFromCalculationRequestLine(line: BulkCalculationRequestLine): List[Char] = {
    val l = line.validCalculationRequest.get.productIterator.toList

    def process(item: Any) = {
      val dateRegEx = """([0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9])""".r
      item match {
        case None => ","
        case s: String if s.matches(dateRegEx.regex) => LocalDate.parse(s).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ","
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
