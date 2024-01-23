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
import play.api.test.FakeRequest
import uk.gov.hmrc.emailaddress.EmailAddress
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.concurrent.{Await, Future}

class SessionServiceSpec extends PlaySpec with GuiceOneServerPerSuite with ScalaFutures with MockitoSugar {

  val memberDetails = MemberDetails(RandomNino.generate, "John", "Johnson")
  val scon = "S3123456A"
  val gmpSession = GmpSession(memberDetails, scon, CalculationType.DOL, None, None, Leaving(GmpDate(None,None,None),None), None)
  val json = Json.toJson[GmpSession](gmpSession)
  val mockSessionCache = mock[GmpSessionCache]
  val metrics = app.injector.instanceOf[ApplicationMetrics]

  val callBackData = UploadedSuccessfully("ref1", "file1", "download1")
  val gmpBulkSession = GmpBulkSession(Some(callBackData), Some(EmailAddress("somebody@somewhere.com")), Some("reference"))
  val bulkJson = Json.toJson[GmpBulkSession](gmpBulkSession)

  object TestSessionService extends SessionService(metrics, mockSessionCache)

  implicit val request = FakeRequest()
  val hc = HeaderCarrier()

  "Session service" must {

    "gmpSession" must {

      "cache new member details" in {
        when(mockSessionCache.fetchAndGetEntry[GmpSession](any())(any(), any(), any())).thenReturn(Future.successful(None))

        val newMemberDetails = MemberDetails(RandomNino.generate, "John", "Johnson")
        val json = Json.toJson[GmpSession](gmpSession.copy(newMemberDetails))

        when(mockSessionCache.cache[GmpSession](any(), any())(any(), any(), any())).thenReturn(Future.successful(CacheMap("sessionValue", Map("gmp_session" -> json))))

        val result = Await.result(TestSessionService.cacheMemberDetails(newMemberDetails)(hc), 10 seconds)
        result must be(Some(gmpSession.copy(memberDetails = newMemberDetails)))
      }

      "update member details" in {
        when(mockSessionCache.fetchAndGetEntry[GmpSession](any())(any(), any(), any())).thenReturn(Future.successful(Some(gmpSession)))

        val newMemberDetails = MemberDetails(RandomNino.generate, "John", "Johnson")
        val json = Json.toJson[GmpSession](gmpSession.copy(memberDetails = newMemberDetails))

        when(mockSessionCache.cache[GmpSession](any(), any())(any(), any(), any())).thenReturn(Future.successful(CacheMap("sessionValue", Map("gmp_session" -> json))))

        val result = Await.result(TestSessionService.cacheMemberDetails(newMemberDetails)(hc), 10 seconds)
        result must be(Some(gmpSession.copy(memberDetails = newMemberDetails)))
      }

      "fetch member details" in {
        when(mockSessionCache.fetchAndGetEntry[GmpSession](any())(any(), any(), any())).thenReturn(Future.successful(Some(gmpSession)))
        val result = Await.result(TestSessionService.fetchMemberDetails()(hc), 10 seconds)
        result must be(Some(memberDetails))
      }

      "cache new pension details" in {
        when(mockSessionCache.fetchAndGetEntry[GmpSession](any())(any(), any(), any())).thenReturn(Future.successful(None))

        val newScon = "S3123226B"
        val json = Json.toJson[GmpSession](gmpSession.copy(scon = newScon))
        when(mockSessionCache.cache[GmpSession](any(), any())(any(), any(), any())).thenReturn(Future.successful(CacheMap("sessionValue", Map("gmp_session" -> json))))

        val result = Await.result(TestSessionService.cachePensionDetails(newScon)(hc), 10 seconds)
        result must be(Some(gmpSession.copy(scon = newScon)))
      }

      "update pension details" in {
        when(mockSessionCache.fetchAndGetEntry[GmpSession](any())(any(), any(), any())).thenReturn(Future.successful(Some(gmpSession)))

        val newScon = "S3123226B"
        val json = Json.toJson[GmpSession](gmpSession.copy(scon = newScon))

        when(mockSessionCache.cache[GmpSession](any(), any())(any(), any(), any())).thenReturn(Future.successful(CacheMap("sessionValue", Map("gmp_session" -> json))))

        val result = Await.result(TestSessionService.cachePensionDetails(newScon)(hc), 10 seconds)
        result must be(Some(gmpSession.copy(scon = newScon)))
      }

      "fetch pension details" in {
        when(mockSessionCache.fetchAndGetEntry[GmpSession](any())(any(), any(), any())).thenReturn(Future.successful(Some(gmpSession)))
        val result = Await.result(TestSessionService.fetchPensionDetails()(hc), 10 seconds)
        result must be(Some(scon))
      }

      "cache scenario" in {
        when(mockSessionCache.fetchAndGetEntry[GmpSession](any())(any(), any(), any())).thenReturn(Future.successful(None))

        val json = Json.toJson[GmpSession](gmpSession)

        when(mockSessionCache.cache[GmpSession](any(), any())(any(), any(), any())).thenReturn(Future.successful(CacheMap("sessionValue", Map("gmp_session" -> json))))
        val result = Await.result(TestSessionService.cacheScenario("2")(hc), 10 seconds)
        result must be(Some(gmpSession))
      }

      "update scenario" in {
        when(mockSessionCache.fetchAndGetEntry[GmpSession](any())(any(), any(), any())).thenReturn(Future.successful(Some(gmpSession)))

        val newScenario = "2"
        val json = Json.toJson[GmpSession](gmpSession.copy(scenario = newScenario))

        when(mockSessionCache.cache[GmpSession](any(), any())(any(), any(), any())).thenReturn(Future.successful(CacheMap("sessionValue", Map("gmp_session" -> json))))

        val result = Await.result(TestSessionService.cacheScenario(newScenario)(hc), 10 seconds)
        result must be(Some(gmpSession.copy(scenario = newScenario)))
      }

      "fetch the scenario" in {
        when(mockSessionCache.fetchAndGetEntry[GmpSession](any())(any(), any(), any())).thenReturn(Future.successful(Some(gmpSession)))
        val result = Await.result(TestSessionService.fetchScenario()(hc), 10 seconds)
        result must be(Some(CalculationType.DOL))
      }

      "caching a revaluation date" must {

        "update the leaving date when the member has not left the scheme and revaluing" in {
          when(mockSessionCache.fetchAndGetEntry[GmpSession](any())(any(), any(), any())).thenReturn(Future.successful(Some(gmpSession.copy(scenario = CalculationType.REVALUATION, leaving = Leaving(GmpDate(None, None, None), Some(Leaving.NO))))))

          val revalDate = GmpDate(Some("01"), Some("01"), Some("2010"))
          val json = Json.toJson[GmpSession](gmpSession.copy(scenario = CalculationType.REVALUATION, leaving = Leaving(revalDate, Some(Leaving.NO))))

          when(mockSessionCache.cache[GmpSession](any(), any())(any(), any(), any())).thenReturn(Future.successful(CacheMap("sessionValue", Map("gmp_session" -> json))))

          val result = Await.result(TestSessionService.cacheRevaluationDate(Some(revalDate))(hc), 10 seconds)
          result must be(Some(gmpSession.copy(scenario = CalculationType.REVALUATION, leaving = Leaving(revalDate, Some(Leaving.NO)))))
        }

        "cache revaluation date when the member has left the scheme" in {
          when(mockSessionCache.fetchAndGetEntry[GmpSession](any())(any(), any(), any())).thenReturn(Future.successful(Some(gmpSession.copy(leaving = Leaving(GmpDate(None, None, None), Some(Leaving.YES_AFTER))))))

          val revalDate = GmpDate(Some("01"), Some("01"), Some("2010"))
          val json = Json.toJson[GmpSession](gmpSession.copy(revaluationDate = Some(revalDate)))

          when(mockSessionCache.cache[GmpSession](any(), any())(any(), any(), any())).thenReturn(Future.successful(CacheMap("sessionValue", Map("gmp_session" -> json))))

          val result = Await.result(TestSessionService.cacheRevaluationDate(Some(revalDate))(hc), 10 seconds)
          result must be(Some(gmpSession.copy(revaluationDate = Some(revalDate))))
        }

        "cahe reval date" in {
          when(mockSessionCache.fetchAndGetEntry[GmpSession](any())(any(), any(), any())).thenReturn(Future.successful(None))
          val revalDate = GmpDate(Some("01"), Some("01"), Some("2010"))
          val json = Json.toJson[GmpSession](gmpSession.copy(revaluationDate = Some(revalDate)))

          when(mockSessionCache.cache[GmpSession](any(), any())(any(), any(), any())).thenReturn(Future.successful(CacheMap("sessionValue", Map("gmp_session" -> json))))

          val result = Await.result(TestSessionService.cacheRevaluationDate(Some(revalDate))(hc), 10 seconds)
          result must be(Some(gmpSession.copy(revaluationDate = Some(revalDate))))
        }

        "set revaluation date to termination date and cache it when member has not left the scheme" in {

          val dol = GmpDate(Some("24"), Some("08"), Some("2016"))
          when(mockSessionCache.fetchAndGetEntry[GmpSession](any())(any(), any(), any())).thenReturn(
            Future.successful(Some(gmpSession.copy(scenario = CalculationType.REVALUATION, leaving = Leaving(dol, Some(Leaving.NO))))))

          val json = Json.toJson[GmpSession](gmpSession.copy(scenario = CalculationType.REVALUATION,revaluationDate = Some(dol) ,leaving = Leaving(dol, Some(Leaving.NO))))
          when(mockSessionCache.cache[GmpSession](any(), any())(any(), any(), any())).thenReturn(Future.successful(CacheMap("sessionValue", Map("gmp_session" -> json))))

          val result = Await.result(TestSessionService.cacheRevaluationDate(Some(dol))(hc), 10 seconds)
          result must be(Some(gmpSession.copy(scenario = CalculationType.REVALUATION , revaluationDate = Some(dol), leaving = Leaving(dol, Some(Leaving.NO)))))

        }
      }

      "cache leaving" in {
        when(mockSessionCache.fetchAndGetEntry[GmpSession](any())(any(), any(), any())).thenReturn(Future.successful(None))

        val dol = GmpDate(Some("01"), Some("01"), Some("2010"))
        val json = Json.toJson[GmpSession](gmpSession.copy(leaving = Leaving(leavingDate = dol, leaving = Some("Yes"))))
        when(mockSessionCache.cache[GmpSession](any(), any())(any(), any(), any())).thenReturn(Future.successful(CacheMap("sessionValue", Map("gmp_session" -> json))))
        val result = Await.result(TestSessionService.cacheLeaving(Leaving(leavingDate = dol, leaving = Some("Yes")))(hc), 10 seconds)
        result must be(Some(gmpSession.copy(leaving = Leaving(leavingDate = dol, leaving = Some("Yes")))))
      }

      "fetch leaving" in {
        when(mockSessionCache.fetchAndGetEntry[GmpSession](any())(any(), any(), any())).thenReturn(Future.successful(Some(gmpSession)))
        val result = Await.result(TestSessionService.fetchLeaving()(hc), 10 seconds)
        result must be(Some(Leaving(GmpDate(None,None,None),None)))
      }

      "update leaving" in {
        when(mockSessionCache.fetchAndGetEntry[GmpSession](any())(any(), any(), any())).thenReturn(Future.successful(Some(gmpSession)))

        val dol = GmpDate(Some("01"), Some("01"), Some("2010"))
        val json = Json.toJson[GmpSession](gmpSession.copy(leaving = Leaving(leavingDate = dol, leaving = Some("Yes"))))
        when(mockSessionCache.cache[GmpSession](any(), any())(any(), any(), any())).thenReturn(Future.successful(CacheMap("sessionValue", Map("gmp_session" -> json))))
        val result = Await.result(TestSessionService.cacheLeaving(Leaving(leavingDate = dol, leaving = Some("Yes")))(hc), 10 seconds)
        result must be(Some(gmpSession.copy(leaving = Leaving(leavingDate = dol, leaving = Some("Yes")))))
      }

      "cache revaluation rate" in {
        when(mockSessionCache.fetchAndGetEntry[GmpSession](any())(any(), any(), any())).thenReturn(Future.successful(None))

        val revalRate = RevaluationRate.FIXED
        val json = Json.toJson[GmpSession](gmpSession.copy(rate = Some(revalRate)))

        when(mockSessionCache.cache[GmpSession](any(), any())(any(), any(), any())).thenReturn(Future.successful(CacheMap("sessionValue", Map("gmp_session" -> json))))

        val result = Await.result(TestSessionService.cacheRevaluationRate(revalRate)(hc), 10 seconds)
        result must be(Some(gmpSession.copy(rate = Some(revalRate))))
      }

      "update revaluation rate" in {
        when(mockSessionCache.fetchAndGetEntry[GmpSession](any())(any(), any(), any())).thenReturn(Future.successful(Some(gmpSession)))

        val revalRate = RevaluationRate.FIXED
        val json = Json.toJson[GmpSession](gmpSession.copy(rate = Some(revalRate)))

        when(mockSessionCache.cache[GmpSession](any(), any())(any(), any(), any())).thenReturn(Future.successful(CacheMap("sessionValue", Map("gmp_session" -> json))))

        val result = Await.result(TestSessionService.cacheRevaluationRate(revalRate)(hc), 10 seconds)
        result must be(Some(gmpSession.copy(rate = Some(revalRate))))
      }

      "keep revaluation rate when revaluation date is cached" in {

        val json = Json.toJson[GmpSession](gmpSession.copy(scenario = CalculationType.SURVIVOR, rate = Some(RevaluationRate.FIXED)))

        when(mockSessionCache.cache[GmpSession](any(), any())(any(), any(), any())).thenReturn(Future.successful(CacheMap("sessionValue", Map("gmp_session" -> json))))

        val result = Await.result(TestSessionService.cacheRevaluationDate(None)(hc), 10 seconds)
        result must be(Some(gmpSession.copy(scenario = CalculationType.SURVIVOR, rate = Some(RevaluationRate.FIXED))))
      }

      "cache equalise" in {
        when(mockSessionCache.fetchAndGetEntry[GmpSession](any())(any(), any(), any())).thenReturn(Future.successful(None))
        val json = Json.toJson[GmpSession](gmpSession.copy(equalise = Some(1)))
        when(mockSessionCache.cache[GmpSession](any(), any())(any(), any(), any())).thenReturn(Future.successful(CacheMap("sessionValue", Map("gmp_session" -> json))))
        val result = Await.result(TestSessionService.cacheEqualise(Some(1))(hc), 10 seconds)
        result must be(Some(gmpSession.copy(equalise = Some(1))))
      }

      "update equalise" in {
        when(mockSessionCache.fetchAndGetEntry[GmpSession](any())(any(), any(), any())).thenReturn(Future.successful(Some(gmpSession)))
        val json = Json.toJson[GmpSession](gmpSession.copy(equalise = Some(1)))
        when(mockSessionCache.cache[GmpSession](any(), any())(any(), any(), any())).thenReturn(Future.successful(CacheMap("sessionValue", Map("gmp_session" -> json))))
        val result = Await.result(TestSessionService.cacheEqualise(Some(1))(hc), 10 seconds)
        result must be(Some(gmpSession.copy(equalise = Some(1))))
      }

      "reset the session" in {
        val result = Await.result(TestSessionService.resetGmpSession()(hc), 10 seconds)
        result must be(Some(new SessionService(metrics, mockSessionCache).cleanSession))
      }

      "reset the session with scon" in {
        when(mockSessionCache.fetchAndGetEntry[GmpSession](any())(any(), any(), any())).thenReturn(Future.successful(Some(gmpSession.copy(scon = scon))))
        val result = Await.result(TestSessionService.resetGmpSessionWithScon()(hc), 10 seconds)
        result must be(Some(new SessionService(metrics, mockSessionCache).cleanSession.copy(scon = scon)))
      }

      "fetch the session" in {
        when(mockSessionCache.fetchAndGetEntry[GmpSession](any())(any(), any(), any())).thenReturn(Future.successful(Some(gmpSession)))
        val result = Await.result(TestSessionService.fetchGmpSession()(hc), 10 seconds)
        result must be(Some(gmpSession))
      }
    }

    "gmpBulkSession" must {
      "fetch the session" in {
        when(mockSessionCache.fetchAndGetEntry[GmpBulkSession](any())(any(), any(), any())).thenReturn(Future.successful(Some(gmpBulkSession)))
        val result = Await.result(TestSessionService.fetchGmpBulkSession()(hc), 10 seconds)
        result must be(Some(gmpBulkSession))
      }

      "reset the session" in {
        val result = Await.result(TestSessionService.resetGmpBulkSession()(hc), 10 seconds)
        result must be(Some(new SessionService(metrics, mockSessionCache).cleanBulkSession))
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
