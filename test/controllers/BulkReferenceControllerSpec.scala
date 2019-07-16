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

import controllers.auth.{AuthAction, GmpAuthConnector}
import models._
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.{OneServerPerSuite, PlaySpec}
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SessionService
import uk.gov.hmrc.domain.PsaId
import uk.gov.hmrc.play.frontend.auth.AuthContext
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.frontend.auth.connectors.domain._
import play.api.i18n.Messages.Implicits._

import scala.concurrent.Future
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.logging.SessionId
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

class BulkReferenceControllerSpec extends PlaySpec with OneServerPerSuite with MockitoSugar with GmpUsers {

  val mockAuthConnector: GmpAuthConnector = mock[GmpAuthConnector]
  val mockSessionService: SessionService = mock[SessionService]
  val mockAuditConnector: AuditConnector = mock[AuditConnector]

  implicit val user = AuthContext(authority = Authority("1234", Accounts(psa = Some(PsaAccount("link", PsaId("B1234567")))),
                                            None, None, CredentialStrength.None, ConfidenceLevel.L50, None, None,None, legacyOid= ""))

  implicit val hc = new HeaderCarrier(sessionId = Some(SessionId(s"session-${UUID.randomUUID}")))

  object TestBulkReferenceController extends BulkReferenceController(mock[AuthAction],mockAuthConnector, mockAuditConnector) {
    override val sessionService = mockSessionService
    override val context = FakeGmpContext
  }

  "BulkRerefenceController" must {

    "bulk reference GET " must {

      "authenticated users" must {
        "respond with ok" in {
          withAuthorisedUser { user =>
            getBulkReference(user) { result =>
              status(result) must equal(OK)
              contentAsString(result) must include(Messages("gmp.bulk_reference.header"))
              contentAsString(result) must include(Messages("gmp.reference.calcname"))
              contentAsString(result) must include(Messages("gmp.back.link"))
            }
          }
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
        withAuthorisedUser { request =>
          val result = TestBulkReferenceController.post()(request.withJsonBody(Json.toJson(emptyRequest)))
          status(result) must equal(BAD_REQUEST)
          contentAsString(result) must include(Messages("gmp.error.mandatory", Messages("gmp.reference")))
        }
      }

      "throw an exception when can't cache email and reference" in {
        when(mockSessionService.cacheEmailAndReference(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
        withAuthorisedUser { request =>
          val result = TestBulkReferenceController.post()(request.withJsonBody(Json.toJson(validRequest)))
          intercept[RuntimeException]{
            status(result) must equal(BAD_REQUEST)
          }

        }
      }

      "validate email and reference, cache and redirect" in {
        when(mockSessionService.cacheEmailAndReference(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpBulkSession)))
        withAuthorisedUser { request =>
          val result = TestBulkReferenceController.post()(request.withJsonBody(Json.toJson(validRequest)))
          status(result) must equal(SEE_OTHER)
          redirectLocation(result).get must include("/request-received")
        }
      }

      "validate email and reference with spaces, cache and redirect" in {
        when(mockSessionService.cacheEmailAndReference(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpBulkSession)))
        withAuthorisedUser { request =>
          val result = TestBulkReferenceController.post()(request.withJsonBody(Json.toJson(validRequestWithSpaces)))
          status(result) must equal(SEE_OTHER)
          redirectLocation(result).get must include("/request-received")
        }
      }
    }

    "BACK" must {

      "authorised users redirect" in {

        withAuthorisedUser { request =>
          val result = TestBulkReferenceController.back.apply(request)
          status(result) must equal(SEE_OTHER)
          redirectLocation(result).get must include("/upload-csv")
        }
      }

    }
  }

  def getBulkReference(request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest())(handler: Future[Result] => Any): Unit = {
    handler(TestBulkReferenceController.get.apply(request))
  }

  def postBulkReference(request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest())(handler: Future[Result] => Any): Unit = {
    handler(TestBulkReferenceController.post.apply(request))
  }
}
