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
import controllers.auth.FakeAuthAction
import forms.BulkReferenceForm
import models._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{GMPSessionService, SessionService}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.SessionId
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import views.Views

import scala.concurrent.{ExecutionContext, Future}

class BulkReferenceControllerSpec extends PlaySpec  with MockitoSugar with GuiceOneAppPerSuite{

  val mockAuthConnector: AuthConnector = mock[AuthConnector]
  val mockSessionService: GMPSessionService = mock[GMPSessionService]
  val mockAuditConnector: AuditConnector = mock[AuditConnector]

  implicit lazy val hc: HeaderCarrier = new HeaderCarrier(sessionId = Some(SessionId(s"session-${UUID.randomUUID}")))
  implicit val mcc: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]
  implicit val ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]
  implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit val messagesProvider: MessagesImpl = MessagesImpl(Lang("en"), messagesApi)
  implicit val ac: ApplicationConfig = app.injector.instanceOf[ApplicationConfig]
  implicit val gmpSessionCache: GmpSessionCache = app.injector.instanceOf[GmpSessionCache]
  lazy val bulkReferenceForm = new BulkReferenceForm(mcc)
  lazy val views = app.injector.instanceOf[Views]

  object TestBulkReferenceController extends BulkReferenceController(
    FakeAuthAction, mockAuthConnector, mockAuditConnector,mockSessionService,
    FakeGmpContext,bulkReferenceForm,mcc,ec,ac,gmpSessionCache, views) {}

  "BulkReferenceController" must {

    "bulk reference GET " must {

      "authenticated users" must {
        "respond with ok" in {
          val result = TestBulkReferenceController.get(FakeRequest())
          status(result) must equal(OK)
              contentAsString(result) must include(Messages("gmp.bulk_reference.header"))
              contentAsString(result) must include(Messages("gmp.reference.calcname"))
              contentAsString(result) must include(Messages("gmp.back.link"))


        }
      }
    }

    "bulk reference POST " must {

      val validRequest = BulkReference("dan@hmrc.com", "Reference")
      val validRequestWithSpaces = BulkReference("dan@hmrc.com   ", "Reference   ")
      val gmpBulkSession = GmpBulkSession(None, None, None)

      "respond with bad request missing email and reference" in {

          val result = TestBulkReferenceController.post(FakeRequest().withMethod("POST")
            .withFormUrlEncodedBody("email" -> "", "reference" -> ""))
          status(result) mustBe BAD_REQUEST
          contentAsString(result) must include(Messages("gmp.error.mandatory", Messages("gmp.reference")))
      }

      "throw an exception when can't cache email and reference" in {
        when(mockSessionService.cacheEmailAndReference(any(), any())(any())).thenReturn(Future.successful(None))

          val result = TestBulkReferenceController.post(FakeRequest().withMethod("POST")
            .withFormUrlEncodedBody("email" -> validRequest.email, "reference" -> validRequest.reference))
          intercept[RuntimeException]{
            status(result) must equal(BAD_REQUEST)
        }
      }

      "validate email and reference, cache and redirect" in {
        when(mockSessionService.cacheEmailAndReference(any(), any())(any())).thenReturn(Future.successful(Some(gmpBulkSession)))

          val result = TestBulkReferenceController.post(FakeRequest().withMethod("POST")
            .withFormUrlEncodedBody("email" -> validRequest.email, "reference" -> validRequest.reference))
          status(result) must equal(SEE_OTHER)
          redirectLocation(result).get must include("/request-received")
      }

      "validate email and reference with spaces, cache and redirect" in {
        when(mockSessionService.cacheEmailAndReference(any(), any())(any())).thenReturn(Future.successful(Some(gmpBulkSession)))

          val result = TestBulkReferenceController.post()(FakeRequest().withMethod("POST")
            .withFormUrlEncodedBody("email" -> validRequestWithSpaces.email,
              "reference" -> validRequestWithSpaces.reference))
          status(result) must equal(SEE_OTHER)
          redirectLocation(result).get must include("/request-received")
      }
    }

    "BACK" must {

      "authorised users redirect" in {

          val result = TestBulkReferenceController.back(FakeRequest())
          status(result) must equal(SEE_OTHER)
          redirectLocation(result).get must include("/upload-csv")
      }

    }
  }

}
