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

import config.{ApplicationConfig, GmpSessionCache}
import metrics.ApplicationMetrics
import models._
import models.upscan.UploadedSuccessfully
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import repositories.GMPBulkSessionRepository
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

class GMPBulkSessionServiceSpec extends PlaySpec with GuiceOneServerPerSuite with ScalaFutures with MockitoSugar {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  val mockSessionRepository = mock[GMPBulkSessionRepository]
  val mockSessionCache = mock[GmpSessionCache]
  val mockMetrics = app.injector.instanceOf[ApplicationMetrics]
  val mockAppConfig = mock[ApplicationConfig]
  when(mockAppConfig.serviceMaxNoOfAttempts).thenReturn(3)

  val callBackData = UploadedSuccessfully("ref1", "file1", "download1")
  val emailRegex = "^([a-zA-Z0-9.!#$%&’'*+/=?^_{|}~-]+)@([a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*)$".r
  val email = "somebody@somewhere.com"
  require(email.matches(emailRegex.regex), "Invalid email format")

  val gmpBulkSession = GmpBulkSession(Some(callBackData), Some(email), Some("reference"))
  val bulkJson = Json.toJson[GmpBulkSession](gmpBulkSession)

  object TestSessionService extends GMPBulkSessionService(mockMetrics, mockAppConfig, mockSessionRepository)

  implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
  implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("testSessionId")))

  "GMPBulkSessionService" must {

    "fetch the session" in {
      when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(GMPBulkSessionCache("sessionId", gmpBulkSession))))
      val result = Await.result(TestSessionService.fetchGmpBulkSession()(hc), 10.seconds)
      result must be(Some(gmpBulkSession))
    }

    "reset the session" in {
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
      val result = Await.result(TestSessionService.resetGmpBulkSession()(hc), 10.seconds)
      result must be(Some(TestSessionService.cleanBulkSession))
    }

    "cache callback data" in {
      when(mockSessionRepository.get(any())).thenReturn(Future.successful(None))

      val callbackData = GmpBulkSession(Some(UploadedSuccessfully("reference", "fileName", "download")), None, None)
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
      val json = Json.toJson[GmpBulkSession](callbackData)

      when(mockSessionCache.cache[GmpBulkSession](any(), any())(any(), any(), any())).thenReturn(Future.successful(CacheMap("sessionValue", Map("gmp_bulk_session" -> json))))

      val result = Await.result(TestSessionService.cacheCallBackData(Some(UploadedSuccessfully("reference", "fileName", "download")))(hc), 10.seconds)
      result must be(Some(callbackData))
    }

    "update callback data" in {
      when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(GMPBulkSessionCache("sessionId", gmpBulkSession))))

      val expectedResult = gmpBulkSession.copy(callBackData = Some(UploadedSuccessfully("reference", "fileName", "download")))
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
      val json = Json.toJson[GmpBulkSession](expectedResult)

      when(mockSessionCache.cache[GmpBulkSession](any(), any())(any(), any(), any())).thenReturn(Future.successful(CacheMap("sessionValue", Map("gmp_bulk_session" -> json))))
      val result = Await.result(TestSessionService.cacheCallBackData(Some(UploadedSuccessfully("reference", "fileName", "download")))(hc), 10.seconds)
      result must be(Some(expectedResult))

    }

    "cache email and reference" in {
      when(mockSessionRepository.get(any())).thenReturn(Future.successful(None))

      val expectedResult = GmpBulkSession(None,emailAddress = Some("nobody@nowhere.com"), reference = Some("a different reference"))
      val json = Json.toJson[GmpBulkSession](expectedResult)

      when(mockSessionCache.cache[GmpBulkSession](any(), any())(any(), any(), any())).thenReturn(Future.successful(CacheMap("sessionValue", Map("gmp_bulk_session" -> json))))
      val result = Await.result(TestSessionService.cacheEmailAndReference(Some("nobody@nowhere.com"), Some("a different reference"))(hc), 10.seconds)
      result must be(Some(expectedResult))
    }

    "update email and reference" in {
      when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(GMPBulkSessionCache("sessionId", gmpBulkSession))))

      val expectedResult = gmpBulkSession.copy(emailAddress = Some("nobody@nowhere.com"), reference = Some("a different reference"))
      val json = Json.toJson[GmpBulkSession](expectedResult)

      when(mockSessionCache.cache[GmpBulkSession](any(), any())(any(), any(), any())).thenReturn(Future.successful(CacheMap("sessionValue", Map("gmp_bulk_session" -> json))))
      val result = Await.result(TestSessionService.cacheEmailAndReference(Some("nobody@nowhere.com"), Some("a different reference"))(hc), 10.seconds)
      result must be(Some(expectedResult))

    }
  }
  }
