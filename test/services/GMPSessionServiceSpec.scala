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

package services

import config.ApplicationConfig
import models._
import models.upscan._
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

class GMPSessionServiceSpec extends PlaySpec with ScalaFutures with MockitoSugar {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  val mockGmpBulkSessionService = mock[GMPBulkSessionService]
  val mockSingleCalculationSessionService = mock[SingleCalculationSessionService]
  val mockSessionService = mock[SessionService]
  val mockAppConfig = mock[ApplicationConfig]

  val memberDetails = MemberDetails("AB123456C", "John", "Johnson")
  val scon = "S3123456A"
  val gmpSession = GmpSession(memberDetails, scon, CalculationType.DOL, None, None, Leaving(GmpDate(None, None, None), None), None)
  val gmpBulkSession = GmpBulkSession(Some(UploadedSuccessfully("ref1", "file1", "download1")), Some("email@example.com"), Some("reference"))
  val uploadStatus = UploadedSuccessfully("ref1", "file1", "download1")
  val leavingDetails = Leaving(GmpDate(Some("2024"), Some("10"), Some("31")), Some("No"))
  val revaluationDate = Some(GmpDate(Some("2024"), Some("10"), Some("31")))
  val revaluationRate = "rateType"

  implicit val hc: HeaderCarrier = HeaderCarrier()

  def createGmpSessionService: GMPSessionService =
    new GMPSessionService(mockGmpBulkSessionService, mockSingleCalculationSessionService, mockSessionService, mockAppConfig)

  "GMPSessionService" when {

    "fetchGmpBulkSession" should {
      "return the bulk session using gmpBulkSessionService if MongoDB cache is enabled" in {
        val service = createGmpSessionService
        when(mockAppConfig.isMongoDBCacheEnabled).thenReturn(true)
        when(mockGmpBulkSessionService.fetchGmpBulkSession()(hc)).thenReturn(Future.successful(Some(gmpBulkSession)))

        val result = Await.result(service.fetchGmpBulkSession(), 10.seconds)
        result mustBe Some(gmpBulkSession)
      }

      "return the bulk session using sessionService if MongoDB cache is disabled" in {
        val service = createGmpSessionService
        when(mockAppConfig.isMongoDBCacheEnabled).thenReturn(false)
        when(mockSessionService.fetchGmpBulkSession()(hc)).thenReturn(Future.successful(Some(gmpBulkSession)))

        val result = Await.result(service.fetchGmpBulkSession(), 10.seconds)
        result mustBe Some(gmpBulkSession)
      }
    }

    "resetGmpBulkSession" should {
      "reset the bulk session using gmpBulkSessionService if MongoDB cache is enabled" in {
        val service = createGmpSessionService
        when(mockAppConfig.isMongoDBCacheEnabled).thenReturn(true)
        when(mockGmpBulkSessionService.resetGmpBulkSession()(hc)).thenReturn(Future.successful(Some(gmpBulkSession)))

        val result = Await.result(service.resetGmpBulkSession(), 10.seconds)
        result mustBe Some(gmpBulkSession)
      }

      "reset the bulk session using sessionService if MongoDB cache is disabled" in {
        val service = createGmpSessionService
        when(mockAppConfig.isMongoDBCacheEnabled).thenReturn(false)
        when(mockSessionService.resetGmpBulkSession()(hc)).thenReturn(Future.successful(Some(gmpBulkSession)))

        val result = Await.result(service.resetGmpBulkSession(), 10.seconds)
        result mustBe Some(gmpBulkSession)
      }
    }

    "cacheCallBackData" should {
      "cache callback data using gmpBulkSessionService if MongoDB cache is enabled" in {
        val service = createGmpSessionService

        when(mockAppConfig.isMongoDBCacheEnabled).thenReturn(true)
        when(mockGmpBulkSessionService.cacheCallBackData(Some(uploadStatus))(hc)).thenReturn(Future.successful(Some(gmpBulkSession)))

        val result = Await.result(service.cacheCallBackData(Some(uploadStatus)), 10.seconds)
        result mustBe Some(gmpBulkSession)
      }

      "cache callback data using sessionService if MongoDB cache is disabled" in {
        val service = createGmpSessionService

        when(mockAppConfig.isMongoDBCacheEnabled).thenReturn(false)
        when(mockSessionService.cacheCallBackData(Some(uploadStatus))(hc)).thenReturn(Future.successful(Some(gmpBulkSession)))

        val result = Await.result(service.cacheCallBackData(Some(uploadStatus)), 10.seconds)
        result mustBe Some(gmpBulkSession)
      }
    }

    "cacheEmailAndReference" should {
      "cache email and reference using gmpBulkSessionService if MongoDB cache is enabled" in {
        val service = createGmpSessionService
        val email = Some("email@example.com")
        val reference = Some("reference")

        when(mockAppConfig.isMongoDBCacheEnabled).thenReturn(true)
        when(mockGmpBulkSessionService.cacheEmailAndReference(email, reference)(hc)).thenReturn(Future.successful(Some(gmpBulkSession)))

        val result = Await.result(service.cacheEmailAndReference(email, reference), 10.seconds)
        result mustBe Some(gmpBulkSession)
      }

      "cache email and reference using sessionService if MongoDB cache is disabled" in {
        val service = createGmpSessionService
        val email = Some("email@example.com")
        val reference = Some("reference")

        when(mockAppConfig.isMongoDBCacheEnabled).thenReturn(false)
        when(mockSessionService.cacheEmailAndReference(email, reference)(hc)).thenReturn(Future.successful(Some(gmpBulkSession)))

        val result = Await.result(service.cacheEmailAndReference(email, reference), 10.seconds)
        result mustBe Some(gmpBulkSession)
      }
    }

    "fetchGmpSession" should {
      "return the GmpSession from singleCalculationSessionService if MongoDB cache is enabled" in {
        val service = createGmpSessionService
        when(mockAppConfig.isMongoDBCacheEnabled).thenReturn(true)
        when(mockSingleCalculationSessionService.fetchGmpSession()(hc)).thenReturn(Future.successful(Some(gmpSession)))

        val result = Await.result(service.fetchGmpSession(), 10.seconds)
        result mustBe Some(gmpSession)
      }

      "return the GmpSession from sessionService if MongoDB cache is disabled" in {
        val service = createGmpSessionService
        when(mockAppConfig.isMongoDBCacheEnabled).thenReturn(false)
        when(mockSessionService.fetchGmpSession()(hc)).thenReturn(Future.successful(Some(gmpSession)))

        val result = Await.result(service.fetchGmpSession(), 10.seconds)
        result mustBe Some(gmpSession)
      }
    }

    "resetGmpSession" should {
      "reset the GmpSession using singleCalculationSessionService if MongoDB cache is enabled" in {
        val service = createGmpSessionService
        when(mockAppConfig.isMongoDBCacheEnabled).thenReturn(true)
        when(mockSingleCalculationSessionService.resetGmpSession()(hc)).thenReturn(Future.successful(Some(gmpSession)))

        val result = Await.result(service.resetGmpSession(), 10.seconds)
        result mustBe Some(gmpSession)
      }

      "reset the GmpSession using sessionService if MongoDB cache is disabled" in {
        val service = createGmpSessionService
        when(mockAppConfig.isMongoDBCacheEnabled).thenReturn(false)
        when(mockSessionService.resetGmpSession()(hc)).thenReturn(Future.successful(Some(gmpSession)))

        val result = Await.result(service.resetGmpSession(), 10.seconds)
        result mustBe Some(gmpSession)
      }
    }

    "resetGmpSessionWithScon" should {
      "reset the GmpSession with scon using singleCalculationSessionService if MongoDB cache is enabled" in {
        val service = createGmpSessionService
        when(mockAppConfig.isMongoDBCacheEnabled).thenReturn(true)
        when(mockSingleCalculationSessionService.resetGmpSessionWithScon()(hc)).thenReturn(Future.successful(Some(gmpSession)))

        val result = Await.result(service.resetGmpSessionWithScon(), 10.seconds)
        result mustBe Some(gmpSession)
      }

      "reset the GmpSession with scon using sessionService if MongoDB cache is disabled" in {
        val service = createGmpSessionService
        when(mockAppConfig.isMongoDBCacheEnabled).thenReturn(false)
        when(mockSessionService.resetGmpSessionWithScon()(hc)).thenReturn(Future.successful(Some(gmpSession)))

        val result = Await.result(service.resetGmpSessionWithScon(), 10.seconds)
        result mustBe Some(gmpSession)
      }
    }

    "cacheMemberDetails" should {
      "cache member details using singleCalculationSessionService if MongoDB cache is enabled" in {
        val service = createGmpSessionService

        when(mockAppConfig.isMongoDBCacheEnabled).thenReturn(true)
        when(mockSingleCalculationSessionService.cacheMemberDetails(memberDetails)(hc)).thenReturn(Future.successful(Some(gmpSession)))

        val result = Await.result(service.cacheMemberDetails(memberDetails), 10.seconds)
        result mustBe Some(gmpSession)
      }

      "cache member details using sessionService if MongoDB cache is disabled" in {
        val service = createGmpSessionService

        when(mockAppConfig.isMongoDBCacheEnabled).thenReturn(false)
        when(mockSessionService.cacheMemberDetails(memberDetails)(hc)).thenReturn(Future.successful(Some(gmpSession)))

        val result = Await.result(service.cacheMemberDetails(memberDetails), 10.seconds)
        result mustBe Some(gmpSession)
      }
    }

    "fetchMemberDetails" should {
      "fetch member details using singleCalculationSessionService if MongoDB cache is enabled" in {
        val service = createGmpSessionService
        when(mockAppConfig.isMongoDBCacheEnabled).thenReturn(true)
        when(mockSingleCalculationSessionService.fetchMemberDetails()(hc)).thenReturn(Future.successful(Some(memberDetails)))

        val result = Await.result(service.fetchMemberDetails(), 10.seconds)
        result mustBe Some(memberDetails)
      }

      "fetch member details using sessionService if MongoDB cache is disabled" in {
        val service = createGmpSessionService
        when(mockAppConfig.isMongoDBCacheEnabled).thenReturn(false)
        when(mockSessionService.fetchMemberDetails()(hc)).thenReturn(Future.successful(Some(memberDetails)))

        val result = Await.result(service.fetchMemberDetails(), 10.seconds)
        result mustBe Some(memberDetails)
      }
    }

    "cachePensionDetails" should {
      "cache pension details using singleCalculationSessionService if MongoDB cache is enabled" in {
        val service = createGmpSessionService
        val pensionScon = "S123456A"

        when(mockAppConfig.isMongoDBCacheEnabled).thenReturn(true)
        when(mockSingleCalculationSessionService.cachePensionDetails(pensionScon)(hc)).thenReturn(Future.successful(Some(gmpSession)))

        val result = Await.result(service.cachePensionDetails(pensionScon), 10.seconds)
        result mustBe Some(gmpSession)
      }

      "cache pension details using sessionService if MongoDB cache is disabled" in {
        val service = createGmpSessionService
        val pensionScon = "S123456A"

        when(mockAppConfig.isMongoDBCacheEnabled).thenReturn(false)
        when(mockSessionService.cachePensionDetails(pensionScon)(hc)).thenReturn(Future.successful(Some(gmpSession)))

        val result = Await.result(service.cachePensionDetails(pensionScon), 10.seconds)
        result mustBe Some(gmpSession)
      }
    }

    "fetchPensionDetails" should {
      "fetch pension details using singleCalculationSessionService if MongoDB cache is enabled" in {
        val service = createGmpSessionService

        when(mockAppConfig.isMongoDBCacheEnabled).thenReturn(true)
        when(mockSingleCalculationSessionService.fetchPensionDetails()(hc)).thenReturn(Future.successful(Some(scon)))

        val result = Await.result(service.fetchPensionDetails(), 10.seconds)
        result mustBe Some(scon)
      }

      "fetch pension details using sessionService if MongoDB cache is disabled" in {
        val service = createGmpSessionService

        when(mockAppConfig.isMongoDBCacheEnabled).thenReturn(false)
        when(mockSessionService.fetchPensionDetails()(hc)).thenReturn(Future.successful(Some(scon)))

        val result = Await.result(service.fetchPensionDetails(), 10.seconds)
        result mustBe Some(scon)
      }
    }

    "cacheScenario" should {
      "cache scenario using singleCalculationSessionService if MongoDB cache is enabled" in {
        val service = createGmpSessionService
        val scenario = "Scenario1"

        when(mockAppConfig.isMongoDBCacheEnabled).thenReturn(true)
        when(mockSingleCalculationSessionService.cacheScenario(scenario)(hc)).thenReturn(Future.successful(Some(gmpSession)))

        val result = Await.result(service.cacheScenario(scenario), 10.seconds)
        result mustBe Some(gmpSession)
      }

      "cache scenario using sessionService if MongoDB cache is disabled" in {
        val service = createGmpSessionService
        val scenario = "Scenario1"

        when(mockAppConfig.isMongoDBCacheEnabled).thenReturn(false)
        when(mockSessionService.cacheScenario(scenario)(hc)).thenReturn(Future.successful(Some(gmpSession)))

        val result = Await.result(service.cacheScenario(scenario), 10.seconds)
        result mustBe Some(gmpSession)
      }
    }

    "fetchScenario" should {
      "fetch scenario using singleCalculationSessionService if MongoDB cache is enabled" in {
        val service = createGmpSessionService

        when(mockAppConfig.isMongoDBCacheEnabled).thenReturn(true)
        when(mockSingleCalculationSessionService.fetchScenario()(hc)).thenReturn(Future.successful(Some("Scenario1")))

        val result = Await.result(service.fetchScenario(), 10.seconds)
        result mustBe Some("Scenario1")
      }

      "fetch scenario using sessionService if MongoDB cache is disabled" in {
        val service = createGmpSessionService

        when(mockAppConfig.isMongoDBCacheEnabled).thenReturn(false)
        when(mockSessionService.fetchScenario()(hc)).thenReturn(Future.successful(Some("Scenario1")))

        val result = Await.result(service.fetchScenario(), 10.seconds)
        result mustBe Some("Scenario1")
      }
    }

    "cacheRevaluationDate" should {
      "cache revaluation date using singleCalculationSessionService if MongoDB cache is enabled" in {
        val service = createGmpSessionService

        when(mockAppConfig.isMongoDBCacheEnabled).thenReturn(true)
        when(mockSingleCalculationSessionService.cacheRevaluationDate(revaluationDate)(hc)).thenReturn(Future.successful(Some(gmpSession)))

        val result = Await.result(service.cacheRevaluationDate(revaluationDate), 10.seconds)
        result mustBe Some(gmpSession)
      }

      "cache revaluation date using sessionService if MongoDB cache is disabled" in {
        val service = createGmpSessionService

        when(mockAppConfig.isMongoDBCacheEnabled).thenReturn(false)
        when(mockSessionService.cacheRevaluationDate(revaluationDate)(hc)).thenReturn(Future.successful(Some(gmpSession)))

        val result = Await.result(service.cacheRevaluationDate(revaluationDate), 10.seconds)
        result mustBe Some(gmpSession)
      }
    }

    "cacheRevaluationRate" should {
      "cache revaluation rate using singleCalculationSessionService if MongoDB cache is enabled" in {
        val service = createGmpSessionService

        when(mockAppConfig.isMongoDBCacheEnabled).thenReturn(true)
        when(mockSingleCalculationSessionService.cacheRevaluationRate(revaluationRate)(hc)).thenReturn(Future.successful(Some(gmpSession)))

        val result = Await.result(service.cacheRevaluationRate(revaluationRate), 10.seconds)
        result mustBe Some(gmpSession)
      }

      "cache revaluation rate using sessionService if MongoDB cache is disabled" in {
        val service = createGmpSessionService

        when(mockAppConfig.isMongoDBCacheEnabled).thenReturn(false)
        when(mockSessionService.cacheRevaluationRate(revaluationRate)(hc)).thenReturn(Future.successful(Some(gmpSession)))

        val result = Await.result(service.cacheRevaluationRate(revaluationRate), 10.seconds)
        result mustBe Some(gmpSession)
      }
    }

    "createCallbackRecord" should {
      "create callback record using gmpBulkSessionService if MongoDB cache is enabled" in {
        val service = createGmpSessionService
        when(mockAppConfig.isMongoDBCacheEnabled).thenReturn(true)
        when(mockGmpBulkSessionService.createCallbackRecord(hc)).thenReturn(Future.successful("callbackRecordCreated"))

        val result = Await.result(service.createCallbackRecord, 10.seconds)
        result mustBe "callbackRecordCreated"
      }

      "create callback record using sessionService if MongoDB cache is disabled" in {
        val service = createGmpSessionService
        when(mockAppConfig.isMongoDBCacheEnabled).thenReturn(false)
        when(mockSessionService.createCallbackRecord(hc)).thenReturn(Future.successful("callbackRecordCreated"))

        val result = Await.result(service.createCallbackRecord, 10.seconds)
        result mustBe "callbackRecordCreated"
      }
    }

    "updateCallbackRecord" should {
      "update callback record using gmpBulkSessionService if MongoDB cache is enabled" in {
        val service = createGmpSessionService
        when(mockAppConfig.isMongoDBCacheEnabled).thenReturn(true)
        when(mockGmpBulkSessionService.updateCallbackRecord(uploadStatus)(hc)).thenReturn(Future.successful("callbackRecordUpdated"))

        val result = Await.result(service.updateCallbackRecord("sessionId", uploadStatus), 10.seconds)
        result mustBe "callbackRecordUpdated"
      }

      "update callback record using sessionService if MongoDB cache is disabled" in {
        val service = createGmpSessionService
        when(mockAppConfig.isMongoDBCacheEnabled).thenReturn(false)
        when(mockSessionService.updateCallbackRecord("sessionId", uploadStatus)(hc)).thenReturn(Future.successful("callbackRecordUpdated"))

        val result = Await.result(service.updateCallbackRecord("sessionId", uploadStatus), 10.seconds)
        result mustBe "callbackRecordUpdated"
      }
    }

    "getCallbackRecord" should {
      "get callback record using gmpBulkSessionService if MongoDB cache is enabled" in {
        val service = createGmpSessionService
        when(mockAppConfig.isMongoDBCacheEnabled).thenReturn(true)
        when(mockGmpBulkSessionService.getCallbackRecord(hc)).thenReturn(Future.successful(Some(uploadStatus)))

        val result = Await.result(service.getCallbackRecord, 10.seconds)
        result mustBe Some(uploadStatus)
      }

      "get callback record using sessionService if MongoDB cache is disabled" in {
        val service = createGmpSessionService
        when(mockAppConfig.isMongoDBCacheEnabled).thenReturn(false)
        when(mockSessionService.getCallbackRecord(hc)).thenReturn(Future.successful(Some(uploadStatus)))

        val result = Await.result(service.getCallbackRecord, 10.seconds)
        result mustBe Some(uploadStatus)
      }
    }
  }
}
