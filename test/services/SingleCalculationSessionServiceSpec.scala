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
import models.{CalculationType, GmpDate, GmpSession, Leaving, MemberDetails, RevaluationRate, SingleCalculationSessionCache}
import helpers.RandomNino
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import repositories.SingleCalculationSessionRepository
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.language.postfixOps

class SingleCalculationSessionServiceSpec extends PlaySpec with GuiceOneServerPerSuite with ScalaFutures with MockitoSugar {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  val memberDetails = MemberDetails(RandomNino.generate, "John", "Johnson")
  val scon = "S3123456A"
  val gmpSession = GmpSession(memberDetails, scon, CalculationType.DOL, None, None, Leaving(GmpDate(None, None, None), None), None)
  val json = Json.toJson[GmpSession](gmpSession)
  val mockSessionCache = mock[GmpSessionCache]

  val mockSessionRepository = mock[SingleCalculationSessionRepository]
  val metrics = app.injector.instanceOf[ApplicationMetrics]
  val mockAppConfig = mock[ApplicationConfig]
  when(mockAppConfig.serviceMaxNoOfAttempts).thenReturn(3)

  object TestSessionService extends SingleCalculationSessionService(metrics, mockAppConfig, mockSessionRepository)

  implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
  implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("testSessionId")))

  "SingleCalculationSessionService" must {

    "cache new member details" in {
      when(mockSessionRepository.get(any())).thenReturn(Future.successful(None))
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val newMemberDetails = MemberDetails(RandomNino.generate, "John", "Johnson")
      val json = Json.toJson[GmpSession](gmpSession.copy(newMemberDetails))

      when(mockSessionCache.cache[GmpSession](any(), any())(any(), any(), any())).thenReturn(Future.successful(CacheMap("sessionValue", Map("gmp_session" -> json))))

      val result = Await.result(TestSessionService.cacheMemberDetails(newMemberDetails)(hc), 10.seconds)
      result must be(Some(TestSessionService.cleanGmpSession.copy(memberDetails = newMemberDetails)))
    }

    "update member details" in {
      when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(SingleCalculationSessionCache("sessionId", gmpSession))))
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val newMemberDetails = MemberDetails(RandomNino.generate, "John", "Johnson")
      val json = Json.toJson[GmpSession](gmpSession.copy(memberDetails = newMemberDetails))

      when(mockSessionCache.cache[GmpSession](any(), any())(any(), any(), any())).thenReturn(Future.successful(CacheMap("sessionValue", Map("gmp_session" -> json))))

      val result = Await.result(TestSessionService.cacheMemberDetails(newMemberDetails)(hc), 10.seconds)
      result must be(Some(gmpSession.copy(memberDetails = newMemberDetails)))
    }


    "fetch member details" in {
      when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(SingleCalculationSessionCache("sessionId", gmpSession))))

      val result = Await.result(TestSessionService.fetchMemberDetails()(hc), 10.seconds)
      result must be(Some(memberDetails))
    }

    "cache new pension details" in {
      when(mockSessionRepository.get(any())).thenReturn(Future.successful(None))
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val newScon = "S3123226B"
      val json = Json.toJson[GmpSession](gmpSession.copy(scon = newScon))
      when(mockSessionCache.cache[GmpSession](any(), any())(any(), any(), any())).thenReturn(Future.successful(CacheMap("sessionValue", Map("gmp_session" -> json))))

      val result = Await.result(TestSessionService.cachePensionDetails(newScon)(hc), 10.seconds)
      result must be(Some(TestSessionService.cleanGmpSession.copy(scon = newScon)))
    }

    "update pension details" in {
      when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(SingleCalculationSessionCache("sessionId", gmpSession))))
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val newScon = "S3123226B"
      val json = Json.toJson[GmpSession](gmpSession.copy(scon = newScon))
      when(mockSessionCache.cache[GmpSession](any(), any())(any(), any(), any())).thenReturn(Future.successful(CacheMap("sessionValue", Map("gmp_session" -> json))))


      val result = Await.result(TestSessionService.cachePensionDetails(newScon)(hc), 10.seconds)
      result must be(Some(gmpSession.copy(scon = newScon)))
    }

    "fetch pension details" in {
      when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(SingleCalculationSessionCache("sessionId", gmpSession))))

      val result = Await.result(TestSessionService.fetchPensionDetails()(hc), 10.seconds)
      result must be(Some(scon))
    }

    "cache scenario" in {
      when(mockSessionRepository.get(any())).thenReturn(Future.successful(None))
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val json = Json.toJson[GmpSession](gmpSession)
      when(mockSessionCache.cache[GmpSession](any(), any())(any(), any(), any())).thenReturn(Future.successful(CacheMap("sessionValue", Map("gmp_session" -> json))))

      val result = Await.result(TestSessionService.cacheScenario("2")(hc), 10.seconds)
      result must be(Some(TestSessionService.cleanGmpSession.copy(scenario = "2")))
    }

    "update scenario" in {
      when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(SingleCalculationSessionCache("sessionId", gmpSession))))
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val newScenario = "2"
      val json = Json.toJson[GmpSession](gmpSession.copy(scenario = newScenario))

      when(mockSessionCache.cache[GmpSession](any(), any())(any(), any(), any())).thenReturn(Future.successful(CacheMap("sessionValue", Map("gmp_session" -> json))))

      val result = Await.result(TestSessionService.cacheScenario(newScenario)(hc), 10.seconds)
      result must be(Some(gmpSession.copy(scenario = newScenario)))
    }

    "fetch the scenario" in {
      when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(SingleCalculationSessionCache("sessionId", gmpSession))))

      val result = Await.result(TestSessionService.fetchScenario()(hc), 10.seconds)
      result must be(Some(CalculationType.DOL))
    }

    "caching a revaluation date" must {

      "update the leaving date when the member has not left the scheme and revaluing" in {
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(SingleCalculationSessionCache(
          "sessionId",
          gmpSession.copy(scenario = CalculationType.REVALUATION, leaving = Leaving(GmpDate(None, None, None), Some(Leaving.NO)))
        ))))

        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        val revalDate = GmpDate(Some("01"), Some("01"), Some("2010"))
        val expectedSession = gmpSession.copy(
          scenario = CalculationType.REVALUATION,
          revaluationDate = Some(revalDate),
          leaving = Leaving(revalDate, Some(Leaving.NO)),
          rate = Some("hmrc")
        )

        val json = Json.toJson(expectedSession)
        when(mockSessionCache.cache[GmpSession](any(), any())(any(), any(), any())).thenReturn(Future.successful(CacheMap("sessionValue", Map("gmp_session" -> json))))

        val result = Await.result(TestSessionService.cacheRevaluationDate(Some(revalDate))(hc), 10.seconds)
        result must be(Some(expectedSession))
      }

      "cache revaluation date when the member has left the scheme" in {
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(SingleCalculationSessionCache(
          "sessionId",
          gmpSession.copy(leaving = Leaving(GmpDate(None, None, None), Some(Leaving.YES_AFTER)))
        ))))

        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        val revalDate = GmpDate(Some("01"), Some("01"), Some("2010"))
        val expectedSession = gmpSession.copy(
          revaluationDate = Some(revalDate),
          leaving = Leaving(GmpDate(None, None, None), Some(Leaving.YES_AFTER))
        )
        val json = Json.toJson(expectedSession)
        when(mockSessionCache.cache[GmpSession](any(), any())(any(), any(), any())).thenReturn(Future.successful(CacheMap("sessionValue", Map("gmp_session" -> json))))

        val result = Await.result(TestSessionService.cacheRevaluationDate(Some(revalDate))(hc), 10.seconds)
        result must be(Some(expectedSession))
      }


      "cache revaluation date" in {
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(None))
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        val revalDate = GmpDate(Some("01"), Some("01"), Some("2010"))
        val json = Json.toJson[GmpSession](gmpSession.copy(revaluationDate = Some(revalDate)))

        when(mockSessionCache.cache[GmpSession](any(), any())(any(), any(), any())).thenReturn(Future.successful(CacheMap("sessionValue", Map("gmp_session" -> json))))

        val result = Await.result(TestSessionService.cacheRevaluationDate(Some(revalDate))(hc), 10.seconds)
        result must be(Some(TestSessionService.cleanGmpSession.copy(revaluationDate = Some(revalDate))))
      }

      "set revaluation date to termination date and cache it when member has not left the scheme" in {
        val dol = GmpDate(Some("24"), Some("08"), Some("2016"))

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(SingleCalculationSessionCache(
          "sessionId",
          gmpSession.copy(scenario = CalculationType.REVALUATION, leaving = Leaving(dol, Some(Leaving.NO)))
        ))))
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        val expectedSession = gmpSession.copy(
          scenario = CalculationType.REVALUATION,
          revaluationDate = Some(dol),
          leaving = Leaving(dol, Some(Leaving.NO)),
          rate = Some("hmrc")
        )

        val json = Json.toJson(expectedSession)
        when(mockSessionCache.cache[GmpSession](any(), any())(any(), any(), any())).thenReturn(Future.successful(CacheMap("sessionValue", Map("gmp_session" -> json))))

        val result = Await.result(TestSessionService.cacheRevaluationDate(Some(dol))(hc), 10.seconds)
        result must be(Some(expectedSession))
      }
    }

    "cache leaving" in {
      when(mockSessionRepository.get(any())).thenReturn(Future.successful(None))
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val dol = GmpDate(Some("01"), Some("01"), Some("2010"))
      val json = Json.toJson[GmpSession](gmpSession.copy(leaving = Leaving(leavingDate = dol, leaving = Some("Yes"))))
      when(mockSessionCache.cache[GmpSession](any(), any())(any(), any(), any())).thenReturn(Future.successful(CacheMap("sessionValue", Map("gmp_session" -> json))))

      val result = Await.result(TestSessionService.cacheLeaving(Leaving(leavingDate = dol, leaving = Some("Yes")))(hc), 10 seconds)
      result must be(Some(TestSessionService.cleanGmpSession.copy(leaving = Leaving(leavingDate = dol, leaving = Some("Yes")))))
    }

    "fetch leaving" in {
      when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(SingleCalculationSessionCache("sessionId", gmpSession))))

      val result = Await.result(TestSessionService.fetchLeaving()(hc), 10.seconds)
      result must be(Some(Leaving(GmpDate(None, None, None), None)))
    }


    "update leaving" in {
      when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(SingleCalculationSessionCache("sessionId", gmpSession))))
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val dol = GmpDate(Some("01"), Some("01"), Some("2010"))
      val json = Json.toJson[GmpSession](gmpSession.copy(leaving = Leaving(leavingDate = dol, leaving = Some("Yes"))))
      when(mockSessionCache.cache[GmpSession](any(), any())(any(), any(), any())).thenReturn(Future.successful(CacheMap("sessionValue", Map("gmp_session" -> json))))

      val result = Await.result(TestSessionService.cacheLeaving(Leaving(leavingDate = dol, leaving = Some("Yes")))(hc), 10 seconds)
      result must be(Some(gmpSession.copy(leaving = Leaving(leavingDate = dol, leaving = Some("Yes")))))
    }

    "cache revaluation rate" in {
      when(mockSessionRepository.get(any())).thenReturn(Future.successful(None))
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val revalRate = RevaluationRate.FIXED
      val json = Json.toJson[GmpSession](gmpSession.copy(rate = Some(revalRate)))
      when(mockSessionCache.cache[GmpSession](any(), any())(any(), any(), any())).thenReturn(Future.successful(CacheMap("sessionValue", Map("gmp_session" -> json))))

      val result = Await.result(TestSessionService.cacheRevaluationRate(revalRate)(hc), 10.seconds)
      result must be(Some(TestSessionService.cleanGmpSession.copy(rate = Some(revalRate))))
    }


    "update revaluation rate" in {
      when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(SingleCalculationSessionCache("sessionId", gmpSession))))
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val revalRate = RevaluationRate.FIXED
      val json = Json.toJson[GmpSession](gmpSession.copy(rate = Some(revalRate)))
      when(mockSessionCache.cache[GmpSession](any(), any())(any(), any(), any())).thenReturn(Future.successful(CacheMap("sessionValue", Map("gmp_session" -> json))))

      val result = Await.result(TestSessionService.cacheRevaluationRate(revalRate)(hc), 10.seconds)
      result must be(Some(gmpSession.copy(rate = Some(revalRate))))
    }


    "keep revaluation rate when revaluation date is cached" in {
      when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(SingleCalculationSessionCache(
        "sessionId",
        gmpSession.copy(scenario = CalculationType.SURVIVOR, rate = Some(RevaluationRate.FIXED))
      ))))
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val expectedSession = gmpSession.copy(
        scenario = CalculationType.SURVIVOR,
        rate = Some(RevaluationRate.FIXED)
      )
      val json = Json.toJson(expectedSession)
      when(mockSessionCache.cache[GmpSession](any(), any())(any(), any(), any())).thenReturn(Future.successful(CacheMap("sessionValue", Map("gmp_session" -> json))))

      val result = Await.result(TestSessionService.cacheRevaluationDate(None)(hc), 10.seconds)
      result must be(Some(expectedSession))
    }


    "cache equalise" in {
      when(mockSessionRepository.get(any())).thenReturn(Future.successful(None))
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val json = Json.toJson[GmpSession](gmpSession.copy(equalise = Some(1)))
      when(mockSessionCache.cache[GmpSession](any(), any())(any(), any(), any())).thenReturn(Future.successful(CacheMap("sessionValue", Map("gmp_session" -> json))))

      val result = Await.result(TestSessionService.cacheEqualise(Some(1))(hc), 10.seconds)
      result must be(Some(TestSessionService.cleanGmpSession.copy(equalise = Some(1))))
    }

    "update equalise" in {
      when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(SingleCalculationSessionCache("sessionId", gmpSession))))
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val json = Json.toJson[GmpSession](gmpSession.copy(equalise = Some(1)))
      when(mockSessionCache.cache[GmpSession](any(), any())(any(), any(), any())).thenReturn(Future.successful(CacheMap("sessionValue", Map("gmp_session" -> json))))

      val result = Await.result(TestSessionService.cacheEqualise(Some(1))(hc), 10.seconds)
      result must be(Some(gmpSession.copy(equalise = Some(1))))
    }

    "reset the session" in {
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
      val result = Await.result(TestSessionService.resetGmpSession()(hc), 10 seconds)
      result must be(Some(TestSessionService.cleanGmpSession))
    }

    "reset the session with scon" in {
      when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(SingleCalculationSessionCache(
        "sessionId",
        gmpSession.copy(scon = scon))
      )))

      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
      val result = Await.result(TestSessionService.resetGmpSessionWithScon()(hc), 10 seconds)
      result must be(Some(TestSessionService.cleanGmpSession.copy(scon = scon)))
    }


    "fetch the session" in {
      when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(SingleCalculationSessionCache("sessionId", gmpSession))))
      val result = Await.result(TestSessionService.fetchGmpSession()(hc), 10 seconds)
      result must be(Some(gmpSession))
    }

  }
}
