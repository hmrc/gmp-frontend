/*
 * Copyright 2019 HM Revenue & Customs
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

import config.GmpFrontendAuditConnector
import controllers.auth.FakeAuthAction
import models._
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.{OneServerPerSuite, PlaySpec}
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SessionService
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.logging.SessionId

import scala.concurrent.Future

class BulkReferenceControllerSpec extends PlaySpec with OneServerPerSuite with MockitoSugar {

  val mockAuthConnector: AuthConnector = mock[AuthConnector]
  val mockSessionService: SessionService = mock[SessionService]
  val mockAuditConnector: GmpFrontendAuditConnector = mock[GmpFrontendAuditConnector]

  implicit val hc = new HeaderCarrier(sessionId = Some(SessionId(s"session-${UUID.randomUUID}")))

  object TestBulkReferenceController extends BulkReferenceController(FakeAuthAction, mockAuthConnector, mockAuditConnector) {
    override val sessionService = mockSessionService
    override val context = FakeGmpContext

  }

  "BulkRerefenceController" must {

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
      val validRequestWithSpacesNoEmail = BulkReference("", "Reference  ")
      val emptyRequest = BulkReference("", "")
      val gmpBulkSession = GmpBulkSession(None, None, None)

      "respond with bad request missing email and reference" in {

          val result = TestBulkReferenceController.post()(FakeRequest().withJsonBody(Json.toJson(emptyRequest)))
          status(result) must equal(BAD_REQUEST)
          contentAsString(result) must include(Messages("gmp.error.mandatory", Messages("gmp.reference")))
      }

      "throw an exception when can't cache email and reference" in {
        when(mockSessionService.cacheEmailAndReference(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))

          val result = TestBulkReferenceController.post()(FakeRequest().withJsonBody(Json.toJson(validRequest)))
          intercept[RuntimeException]{
            status(result) must equal(BAD_REQUEST)
        }
      }

      "validate email and reference, cache and redirect" in {
        when(mockSessionService.cacheEmailAndReference(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpBulkSession)))

          val result = TestBulkReferenceController.post()(FakeRequest().withJsonBody(Json.toJson(validRequest)))
          status(result) must equal(SEE_OTHER)
          redirectLocation(result).get must include("/request-received")
      }

      "validate email and reference with spaces, cache and redirect" in {
        when(mockSessionService.cacheEmailAndReference(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpBulkSession)))

          val result = TestBulkReferenceController.post()(FakeRequest().withJsonBody(Json.toJson(validRequestWithSpaces)))
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
