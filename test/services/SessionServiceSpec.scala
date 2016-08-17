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

package services

import helpers.RandomNino
import metrics.Metrics
import models._
import org.mockito.Matchers
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.{OneServerPerSuite, PlaySpec}
import play.api.libs.json._
import play.api.test.FakeRequest
import uk.gov.hmrc.emailaddress.EmailAddress
import uk.gov.hmrc.http.cache.client.{CacheMap, SessionCache}
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import SessionService._

class SessionServiceSpec extends PlaySpec with OneServerPerSuite with ScalaFutures with MockitoSugar {

  val memberDetails = MemberDetails(RandomNino.generate, "John", "Johnson")
  val scon = "S3123456A"
  val gmpSession = GmpSession(memberDetails, scon, CalculationType.DOL, None, None, Leaving(GmpDate(None,None,None),None), None)
  val json = Json.toJson[GmpSession](gmpSession)
  val mockSessionCache = mock[SessionCache]

  val callBackData = CallBackData("AAAAA", "11111", 1L, Some("Ted"), Some("application/json"), "YYYYYYY", None)
  val gmpBulkSession = GmpBulkSession(Some(callBackData), Some(EmailAddress("somebody@somewhere.com")), Some("reference"))
  val bulkJson = Json.toJson[GmpBulkSession](gmpBulkSession)

  object TestSessionService extends SessionService {
    override def sessionCache: SessionCache = mockSessionCache

    override def metrics = Metrics
  }

  implicit val request = FakeRequest()
  val hc = HeaderCarrier()

  "Session service" must {

    "gmpSession" must {

      "cache new member details" in {
        when(mockSessionCache.fetchAndGetEntry[GmpSession](any())(any(), any())).thenReturn(Future.successful(None))

        val newMemberDetails = MemberDetails(RandomNino.generate, "John", "Johnson")
        val json = Json.toJson[GmpSession](gmpSession.copy(newMemberDetails))

        when(mockSessionCache.cache[GmpSession](any(), any())(any(), any())).thenReturn(Future.successful(CacheMap("sessionValue", Map("gmp_session" -> json))))

        val result = Await.result(TestSessionService.cacheMemberDetails(newMemberDetails)(request, hc), 10 seconds)
        result must be(Some(gmpSession.copy(memberDetails = newMemberDetails)))
      }

      "update member details" in {
        when(mockSessionCache.fetchAndGetEntry[GmpSession](any())(any(), any())).thenReturn(Future.successful(Some(gmpSession)))

        val newMemberDetails = MemberDetails(RandomNino.generate, "John", "Johnson")
        val json = Json.toJson[GmpSession](gmpSession.copy(memberDetails = newMemberDetails))

        when(mockSessionCache.cache[GmpSession](any(), any())(any(), any())).thenReturn(Future.successful(CacheMap("sessionValue", Map("gmp_session" -> json))))

        val result = Await.result(TestSessionService.cacheMemberDetails(newMemberDetails)(request, hc), 10 seconds)
        result must be(Some(gmpSession.copy(memberDetails = newMemberDetails)))
      }

      "fetch member details" in {
        when(mockSessionCache.fetchAndGetEntry[GmpSession](any())(any(), any())).thenReturn(Future.successful(Some(gmpSession)))
        val result = Await.result(TestSessionService.fetchMemberDetails()(request, hc), 10 seconds)
        result must be(Some(memberDetails))
      }

      "cache new pension details" in {
        when(mockSessionCache.fetchAndGetEntry[GmpSession](any())(any(), any())).thenReturn(Future.successful(None))

        val newScon = "S3123226B"
        val json = Json.toJson[GmpSession](gmpSession.copy(scon = newScon))
        when(mockSessionCache.cache[GmpSession](any(), any())(any(), any())).thenReturn(Future.successful(CacheMap("sessionValue", Map("gmp_session" -> json))))

        val result = Await.result(TestSessionService.cachePensionDetails(newScon)(request, hc), 10 seconds)
        result must be(Some(gmpSession.copy(scon = newScon)))
      }

      "update pension details" in {
        when(mockSessionCache.fetchAndGetEntry[GmpSession](any())(any(), any())).thenReturn(Future.successful(Some(gmpSession)))

        val newScon = "S3123226B"
        val json = Json.toJson[GmpSession](gmpSession.copy(scon = newScon))

        when(mockSessionCache.cache[GmpSession](any(), any())(any(), any())).thenReturn(Future.successful(CacheMap("sessionValue", Map("gmp_session" -> json))))

        val result = Await.result(TestSessionService.cachePensionDetails(newScon)(request, hc), 10 seconds)
        result must be(Some(gmpSession.copy(scon = newScon)))
      }

      "fetch pension details" in {
        when(mockSessionCache.fetchAndGetEntry[GmpSession](any())(any(), any())).thenReturn(Future.successful(Some(gmpSession)))
        val result = Await.result(TestSessionService.fetchPensionDetails()(request, hc), 10 seconds)
        result must be(Some(scon))
      }

      "cache scenario" in {
        when(mockSessionCache.fetchAndGetEntry[GmpSession](any())(any(), any())).thenReturn(Future.successful(None))

        val json = Json.toJson[GmpSession](gmpSession)

        when(mockSessionCache.cache[GmpSession](any(), any())(any(), any())).thenReturn(Future.successful(CacheMap("sessionValue", Map("gmp_session" -> json))))
        val result = Await.result(TestSessionService.cacheScenario("2")(request, hc), 10 seconds)
        result must be(Some(gmpSession))
      }

      "update scenario" in {
        when(mockSessionCache.fetchAndGetEntry[GmpSession](any())(any(), any())).thenReturn(Future.successful(Some(gmpSession)))

        val newScenario = "2"
        val json = Json.toJson[GmpSession](gmpSession.copy(scenario = newScenario))

        when(mockSessionCache.cache[GmpSession](any(), any())(any(), any())).thenReturn(Future.successful(CacheMap("sessionValue", Map("gmp_session" -> json))))

        val result = Await.result(TestSessionService.cacheScenario(newScenario)(request, hc), 10 seconds)
        result must be(Some(gmpSession.copy(scenario = newScenario)))
      }

      "fetch the scenario" in {
        when(mockSessionCache.fetchAndGetEntry[GmpSession](any())(any(), any())).thenReturn(Future.successful(Some(gmpSession)))
        val result = Await.result(TestSessionService.fetchScenario()(request, hc), 10 seconds)
        result must be(Some(CalculationType.DOL))
      }

      "caching a revaluation date" must {

        "update the leaving date when the member has not left the scheme and revaluing" in {
          when(mockSessionCache.fetchAndGetEntry[GmpSession](any())(any(), any())).thenReturn(Future.successful(Some(gmpSession.copy(scenario = CalculationType.REVALUATION, leaving = Leaving(GmpDate(None, None, None), Some(Leaving.NO))))))

          val revalDate = GmpDate(Some("01"), Some("01"), Some("2010"))
          val json = Json.toJson[GmpSession](gmpSession.copy(scenario = CalculationType.REVALUATION, leaving = Leaving(revalDate, Some(Leaving.NO))))

          when(mockSessionCache.cache[GmpSession](any(), any())(any(), any())).thenReturn(Future.successful(CacheMap("sessionValue", Map("gmp_session" -> json))))

          val result = Await.result(TestSessionService.cacheRevaluationDate(Some(revalDate))(request, hc), 10 seconds)
          result must be(Some(gmpSession.copy(scenario = CalculationType.REVALUATION, leaving = Leaving(revalDate, Some(Leaving.NO)))))
        }

        "cache revaluation date when the member has left the scheme" in {
          when(mockSessionCache.fetchAndGetEntry[GmpSession](any())(any(), any())).thenReturn(Future.successful(Some(gmpSession.copy(leaving = Leaving(GmpDate(None, None, None), Some(Leaving.YES_AFTER))))))

          val revalDate = GmpDate(Some("01"), Some("01"), Some("2010"))
          val json = Json.toJson[GmpSession](gmpSession.copy(revaluationDate = Some(revalDate)))

          when(mockSessionCache.cache[GmpSession](any(), any())(any(), any())).thenReturn(Future.successful(CacheMap("sessionValue", Map("gmp_session" -> json))))

          val result = Await.result(TestSessionService.cacheRevaluationDate(Some(revalDate))(request, hc), 10 seconds)
          result must be(Some(gmpSession.copy(revaluationDate = Some(revalDate))))
        }

        "cahe reval date" in {
          when(mockSessionCache.fetchAndGetEntry[GmpSession](any())(any(), any())).thenReturn(Future.successful(None))
          val revalDate = GmpDate(Some("01"), Some("01"), Some("2010"))
          val json = Json.toJson[GmpSession](gmpSession.copy(revaluationDate = Some(revalDate)))

          when(mockSessionCache.cache[GmpSession](any(), any())(any(), any())).thenReturn(Future.successful(CacheMap("sessionValue", Map("gmp_session" -> json))))

          val result = Await.result(TestSessionService.cacheRevaluationDate(Some(revalDate))(request, hc), 10 seconds)
          result must be(Some(gmpSession.copy(revaluationDate = Some(revalDate))))
        }

        "set revaluation date to termination date and cache it when member has not left the scheme" in {

          val dol = GmpDate(Some("24"), Some("08"), Some("2016"))
          when(mockSessionCache.fetchAndGetEntry[GmpSession](any())(any(), any())).thenReturn(
            Future.successful(Some(gmpSession.copy(scenario = CalculationType.REVALUATION, leaving = Leaving(dol, Some(Leaving.NO))))))

          val json = Json.toJson[GmpSession](gmpSession.copy(scenario = CalculationType.REVALUATION,revaluationDate = Some(dol) ,leaving = Leaving(dol, Some(Leaving.NO))))
          when(mockSessionCache.cache[GmpSession](any(), any())(any(), any())).thenReturn(Future.successful(CacheMap("sessionValue", Map("gmp_session" -> json))))

          val result = Await.result(TestSessionService.cacheRevaluationDate(Some(dol))(request, hc), 10 seconds)
          result must be(Some(gmpSession.copy(scenario = CalculationType.REVALUATION , revaluationDate = Some(dol), leaving = Leaving(dol, Some(Leaving.NO)))))

        }
      }

      "cache leaving" in {
        when(mockSessionCache.fetchAndGetEntry[GmpSession](any())(any(), any())).thenReturn(Future.successful(None))

        val dol = GmpDate(Some("01"), Some("01"), Some("2010"))
        val json = Json.toJson[GmpSession](gmpSession.copy(leaving = Leaving(leavingDate = dol, leaving = Some("Yes"))))
        when(mockSessionCache.cache[GmpSession](any(), any())(any(), any())).thenReturn(Future.successful(CacheMap("sessionValue", Map("gmp_session" -> json))))
        val result = Await.result(TestSessionService.cacheLeaving(Leaving(leavingDate = dol, leaving = Some("Yes")))(request, hc), 10 seconds)
        result must be(Some(gmpSession.copy(leaving = Leaving(leavingDate = dol, leaving = Some("Yes")))))
      }

      "fetch leaving" in {
        when(mockSessionCache.fetchAndGetEntry[GmpSession](any())(any(), any())).thenReturn(Future.successful(Some(gmpSession)))
        val result = Await.result(TestSessionService.fetchLeaving()(request, hc), 10 seconds)
        result must be(Some(Leaving(GmpDate(None,None,None),None)))
      }

      "update leaving" in {
        when(mockSessionCache.fetchAndGetEntry[GmpSession](any())(any(), any())).thenReturn(Future.successful(Some(gmpSession)))

        val dol = GmpDate(Some("01"), Some("01"), Some("2010"))
        val json = Json.toJson[GmpSession](gmpSession.copy(leaving = Leaving(leavingDate = dol, leaving = Some("Yes"))))
        when(mockSessionCache.cache[GmpSession](any(), any())(any(), any())).thenReturn(Future.successful(CacheMap("sessionValue", Map("gmp_session" -> json))))
        val result = Await.result(TestSessionService.cacheLeaving(Leaving(leavingDate = dol, leaving = Some("Yes")))(request, hc), 10 seconds)
        result must be(Some(gmpSession.copy(leaving = Leaving(leavingDate = dol, leaving = Some("Yes")))))
      }

      "cache revaluation rate" in {
        when(mockSessionCache.fetchAndGetEntry[GmpSession](any())(any(), any())).thenReturn(Future.successful(None))

        val revalRate = RevaluationRate.FIXED
        val json = Json.toJson[GmpSession](gmpSession.copy(rate = Some(revalRate)))

        when(mockSessionCache.cache[GmpSession](any(), any())(any(), any())).thenReturn(Future.successful(CacheMap("sessionValue", Map("gmp_session" -> json))))

        val result = Await.result(TestSessionService.cacheRevaluationRate(revalRate)(request, hc), 10 seconds)
        result must be(Some(gmpSession.copy(rate = Some(revalRate))))
      }

      "update revaluation rate" in {
        when(mockSessionCache.fetchAndGetEntry[GmpSession](any())(any(), any())).thenReturn(Future.successful(Some(gmpSession)))

        val revalRate = RevaluationRate.FIXED
        val json = Json.toJson[GmpSession](gmpSession.copy(rate = Some(revalRate)))

        when(mockSessionCache.cache[GmpSession](any(), any())(any(), any())).thenReturn(Future.successful(CacheMap("sessionValue", Map("gmp_session" -> json))))

        val result = Await.result(TestSessionService.cacheRevaluationRate(revalRate)(request, hc), 10 seconds)
        result must be(Some(gmpSession.copy(rate = Some(revalRate))))
      }

      "keep revaluation rate when revaluation date is cached" in {

        val json = Json.toJson[GmpSession](gmpSession.copy(scenario = CalculationType.SURVIVOR, rate = Some(RevaluationRate.FIXED)))

        when(mockSessionCache.cache[GmpSession](any(), any())(any(), any())).thenReturn(Future.successful(CacheMap("sessionValue", Map("gmp_session" -> json))))

        val result = Await.result(TestSessionService.cacheRevaluationDate(None)(request, hc), 10 seconds)
        result must be(Some(gmpSession.copy(scenario = CalculationType.SURVIVOR, rate = Some(RevaluationRate.FIXED))))
      }

      "cache equalise" in {
        when(mockSessionCache.fetchAndGetEntry[GmpSession](any())(any(), any())).thenReturn(Future.successful(None))
        val json = Json.toJson[GmpSession](gmpSession.copy(equalise = Some(1)))
        when(mockSessionCache.cache[GmpSession](any(), any())(any(), any())).thenReturn(Future.successful(CacheMap("sessionValue", Map("gmp_session" -> json))))
        val result = Await.result(TestSessionService.cacheEqualise(Some(1))(request, hc), 10 seconds)
        result must be(Some(gmpSession.copy(equalise = Some(1))))
      }

      "update equalise" in {
        when(mockSessionCache.fetchAndGetEntry[GmpSession](any())(any(), any())).thenReturn(Future.successful(Some(gmpSession)))
        val json = Json.toJson[GmpSession](gmpSession.copy(equalise = Some(1)))
        when(mockSessionCache.cache[GmpSession](any(), any())(any(), any())).thenReturn(Future.successful(CacheMap("sessionValue", Map("gmp_session" -> json))))
        val result = Await.result(TestSessionService.cacheEqualise(Some(1))(request, hc), 10 seconds)
        result must be(Some(gmpSession.copy(equalise = Some(1))))
      }

      "reset the session" in {
        val result = Await.result(TestSessionService.resetGmpSession()(request, hc), 10 seconds)
        result must be(Some(SessionService.cleanSession))
      }

      "reset the session with scon" in {
        when(mockSessionCache.fetchAndGetEntry[GmpSession](any())(any(), any())).thenReturn(Future.successful(Some(gmpSession.copy(scon = scon))))
        val result = Await.result(TestSessionService.resetGmpSessionWithScon()(request, hc), 10 seconds)
        result must be(Some(SessionService.cleanSession.copy(scon = scon)))
      }

      "fetch the session" in {
        when(mockSessionCache.fetchAndGetEntry[GmpSession](any())(any(), any())).thenReturn(Future.successful(Some(gmpSession)))
        val result = Await.result(TestSessionService.fetchGmpSession()(request, hc), 10 seconds)
        result must be(Some(gmpSession))
      }
    }

    "gmpBulkSession" must {
      "fetch the session" in {
        when(mockSessionCache.fetchAndGetEntry[GmpBulkSession](any())(any(), any())).thenReturn(Future.successful(Some(gmpBulkSession)))
        val result = Await.result(TestSessionService.fetchGmpBulkSession()(request, hc), 10 seconds)
        result must be(Some(gmpBulkSession))
      }

      "reset the session" in {
        val result = Await.result(TestSessionService.resetGmpBulkSession()(request, hc), 10 seconds)
        result must be(Some(SessionService.cleanBulkSession))
      }

      "cache callbackdata" in {
        when(mockSessionCache.fetchAndGetEntry[GmpBulkSession](any())(any(), any())).thenReturn(Future.successful(None))

        val expectedResult = GmpBulkSession(Some(CallBackData("BBBBB", "222222", 2L, Some("Bill"), Some("application/json"), "XXXXXXX", None)), None, None)
        val json = Json.toJson[GmpBulkSession](expectedResult)

        when(mockSessionCache.cache[GmpBulkSession](any(), any())(any(), any())).thenReturn(Future.successful(CacheMap("sessionValue", Map("gmp_bulk_session" -> json))))
        val result = Await.result(TestSessionService.cacheCallBackData(Some(CallBackData("BBBBB", "222222", 2L, Some("Bill"), Some("application/json"), "XXXXXXX", None)))(request, hc), 10 seconds)
        result must be(Some(expectedResult))
      }

      "update callbackdata" in {
        when(mockSessionCache.fetchAndGetEntry[GmpBulkSession](any())(any(), any())).thenReturn(Future.successful(Some(gmpBulkSession)))

        val expectedResult = gmpBulkSession.copy(callBackData = Some(CallBackData("BBBBB", "222222", 2L, Some("Bill"), Some("application/json"), "XXXXXXX", None)))
        val json = Json.toJson[GmpBulkSession](expectedResult)

        when(mockSessionCache.cache[GmpBulkSession](any(), any())(any(), any())).thenReturn(Future.successful(CacheMap("sessionValue", Map("gmp_bulk_session" -> json))))
        val result = Await.result(TestSessionService.cacheCallBackData(Some(CallBackData("BBBBB", "222222", 2L, Some("Bill"), Some("application/json"), "XXXXXXX", None)))(request, hc), 10 seconds)
        result must be(Some(expectedResult))
      }

      "cache email and reference" in {
        when(mockSessionCache.fetchAndGetEntry[GmpBulkSession](any())(any(), any())).thenReturn(Future.successful(None))

        val expectedResult = GmpBulkSession(None,emailAddress = Some("nobody@nowhere.com"), reference = Some("a different reference"))
        val json = Json.toJson[GmpBulkSession](expectedResult)

        when(mockSessionCache.cache[GmpBulkSession](any(), any())(any(), any())).thenReturn(Future.successful(CacheMap("sessionValue", Map("gmp_bulk_session" -> json))))
        val result = Await.result(TestSessionService.cacheEmailAndReference(Some("nobody@nowhere.com"), Some("a different reference"))(request, hc), 10 seconds)
        result must be(Some(expectedResult))
      }

      "update email and reference" in {
        when(mockSessionCache.fetchAndGetEntry[GmpBulkSession](any())(any(), any())).thenReturn(Future.successful(Some(gmpBulkSession)))

        val expectedResult = gmpBulkSession.copy(emailAddress = Some("nobody@nowhere.com"), reference = Some("a different reference"))
        val json = Json.toJson[GmpBulkSession](expectedResult)

        when(mockSessionCache.cache[GmpBulkSession](any(), any())(any(), any())).thenReturn(Future.successful(CacheMap("sessionValue", Map("gmp_bulk_session" -> json))))
        val result = Await.result(TestSessionService.cacheEmailAndReference(Some("nobody@nowhere.com"), Some("a different reference"))(request, hc), 10 seconds)
        result must be(Some(expectedResult))
      }
    }
  }
}
