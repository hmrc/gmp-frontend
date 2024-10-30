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

import config.GmpSessionCache
import helpers.RandomNino
import metrics.ApplicationMetrics
import models._
import models.upscan.UploadedSuccessfully
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import org.scalatestplus.play.PlaySpec
import play.api.libs.json._
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.concurrent.{Await, ExecutionContext, Future}

class GMPBulkSessionServiceSpec extends PlaySpec with GuiceOneServerPerSuite with ScalaFutures with MockitoSugar {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  val memberDetails = MemberDetails(RandomNino.generate, "John", "Johnson")
  val scon = "S3123456A"
  val gmpSession = GmpSession(memberDetails, scon, CalculationType.DOL, None, None, Leaving(GmpDate(None,None,None),None), None)
  val json = Json.toJson[GmpSession](gmpSession)
  val mockSessionCache = mock[GmpSessionCache]
  val metrics = app.injector.instanceOf[ApplicationMetrics]

  val callBackData = UploadedSuccessfully("ref1", "file1", "download1")
  val emailRegex = "^([a-zA-Z0-9.!#$%&’'*+/=?^_{|}~-]+)@([a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*)$".r
  val email = "somebody@somewhere.com"
  require(email.matches(emailRegex.regex), "Invalid email format")

  val gmpBulkSession = GmpBulkSession(Some(callBackData), Some(email), Some("reference"))
  val bulkJson = Json.toJson[GmpBulkSession](gmpBulkSession)

  object TestSessionService extends GMPBulkSessionService(metrics, mockSessionCache)

  implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
  val hc = HeaderCarrier()

  "GMPBulkSessionServiceSpec service" must {

    "gmpBulkSession" must {
      "fetch the session" in {
        when(mockSessionCache.fetchAndGetEntry[GmpBulkSession](any())(any(), any(), any())).thenReturn(Future.successful(Some(gmpBulkSession)))
        val result = Await.result(TestSessionService.fetchGmpBulkSession()(hc), 10 seconds)
        result must be(Some(gmpBulkSession))
      }

      "reset the session" in {
        when(mockSessionCache.cache[GmpBulkSession](any(), any())(any(), any(), any()))
          .thenReturn(Future.successful(CacheMap("sessionValue", Map("gmp_bulk_session" -> bulkJson))))

        val result = Await.result(TestSessionService.resetGmpBulkSession()(hc), 10 seconds)
        result must be(Some(TestSessionService.cleanBulkSession))
      }


      "cache callbackdata" in {
        when(mockSessionCache.fetchAndGetEntry[GmpBulkSession](any())(any(), any(), any())).thenReturn(Future.successful(None))

        val expectedResult = GmpBulkSession(Some(UploadedSuccessfully("reference", "fileName", "download")), None, None)
        val json = Json.toJson[GmpBulkSession](expectedResult)

        when(mockSessionCache.cache[GmpBulkSession](any(), any())(any(), any(), any())).thenReturn(Future.successful(CacheMap("sessionValue", Map("gmp_bulk_session" -> json))))
        val result = Await.result(TestSessionService.cacheCallBackData(Some(UploadedSuccessfully("reference", "fileName", "download")))(hc), 10 seconds)
        result must be(Some(expectedResult))
      }

      "update callbackdata" in {
        when(mockSessionCache.fetchAndGetEntry[GmpBulkSession](any())(any(), any(), any())).thenReturn(Future.successful(Some(gmpBulkSession)))

        val expectedResult = gmpBulkSession.copy(callBackData = Some(UploadedSuccessfully("reference", "fileName", "download")))
        val json = Json.toJson[GmpBulkSession](expectedResult)

        when(mockSessionCache.cache[GmpBulkSession](any(), any())(any(), any(), any())).thenReturn(Future.successful(CacheMap("sessionValue", Map("gmp_bulk_session" -> json))))
        val result = Await.result(TestSessionService.cacheCallBackData(Some(UploadedSuccessfully("reference", "fileName", "download")))(hc), 10 seconds)
        result must be(Some(expectedResult))
      }

      "cache email and reference" in {
        when(mockSessionCache.fetchAndGetEntry[GmpBulkSession](any())(any(), any(), any())).thenReturn(Future.successful(None))

        val expectedResult = GmpBulkSession(None,emailAddress = Some("nobody@nowhere.com"), reference = Some("a different reference"))
        val json = Json.toJson[GmpBulkSession](expectedResult)

        when(mockSessionCache.cache[GmpBulkSession](any(), any())(any(), any(), any())).thenReturn(Future.successful(CacheMap("sessionValue", Map("gmp_bulk_session" -> json))))
        val result = Await.result(TestSessionService.cacheEmailAndReference(Some("nobody@nowhere.com"), Some("a different reference"))(hc), 10 seconds)
        result must be(Some(expectedResult))
      }

      "update email and reference" in {
        when(mockSessionCache.fetchAndGetEntry[GmpBulkSession](any())(any(), any(), any())).thenReturn(Future.successful(Some(gmpBulkSession)))

        val expectedResult = gmpBulkSession.copy(emailAddress = Some("nobody@nowhere.com"), reference = Some("a different reference"))
        val json = Json.toJson[GmpBulkSession](expectedResult)

        when(mockSessionCache.cache[GmpBulkSession](any(), any())(any(), any(), any())).thenReturn(Future.successful(CacheMap("sessionValue", Map("gmp_bulk_session" -> json))))
        val result = Await.result(TestSessionService.cacheEmailAndReference(Some("nobody@nowhere.com"), Some("a different reference"))(hc), 10 seconds)
        result must be(Some(expectedResult))
      }
    }
  }
}
