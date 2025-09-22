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
import metrics.ApplicationMetrics
import models.{CalculationType, GmpDate, GmpSession, Leaving, MemberDetails, RevaluationRate, SingleCalculationSessionCache}
import repositories.SingleCalculationSessionRepository
import services.helper.Retryable
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SingleCalculationSessionService @Inject()(
                                                 metrics: ApplicationMetrics,
                                                 appConfig: ApplicationConfig,
                                                 singleCalculationSessionRepository: SingleCalculationSessionRepository
                                               )(implicit ec: ExecutionContext) extends Retryable {

  val cleanGmpSession: GmpSession = GmpSession(MemberDetails("", "", ""), "", "", None, None, Leaving(GmpDate(None, None, None), None), None)


  private def cleanGmpSingleCalculationSessionCache(
                                        id: String,
                                        gmpSession: GmpSession
                                      ): SingleCalculationSessionCache =
    SingleCalculationSessionCache(id, gmpSession)

  private def getSessionId(implicit hc: HeaderCarrier): String = {
    hc.sessionId match {
      case Some(id) => id.value
      case None => throw new RuntimeException("Unexpected error: No session ID found.")
    }
  }


  def fetchGmpSession()(implicit hc: HeaderCarrier): Future[Option[GmpSession]] = {
    val timer = metrics.keystoreStoreTimer.time()
    logger.debug("[SingleCalculationSessionService][fetchGmpSession]: Fetching Gmp Session data...")

    getSessionCacheDataWithRetry.map { result =>
      timer.stop()
      result.map(_.gmpSession)
    }.recover { case e: Throwable =>
      timer.stop()
      logger.warn("[SingleCalculationSessionService][fetchGmpSession] Error fetching Gmp Session data: " + e.getMessage)
      None
    }
  }


  def resetGmpSession()(implicit hc: HeaderCarrier): Future[Option[GmpSession]] = {
    val timer = metrics.keystoreStoreTimer.time()
    logger.debug("[SingleCalculationSessionService][resetGmpSession]: Resetting Gmp Session data...")

    val cacheData = cleanGmpSingleCalculationSessionCache(getSessionId, cleanGmpSession)
    setSessionCacheDataWithRetry(cacheData).map { _ =>
      timer.stop()
      Some(cleanGmpSession)
    }
  }


  def resetGmpSessionWithScon()(implicit hc: HeaderCarrier ): Future[Option[GmpSession]] = {
    val timer = metrics.keystoreStoreTimer.time()

    logger.debug(s"[SingleCalculationSessionService][fetchGmpSessionWithScon]: Resetting Gmp Session with scon data... ")

    fetchPensionDetails().flatMap { s =>
      val session = cleanGmpSession.copy(scon = s.getOrElse(""))

      val cacheData = cleanGmpSingleCalculationSessionCache(getSessionId, session)
      setSessionCacheDataWithRetry(cacheData).map { _ =>
        timer.stop()
        Some(session)
      }
    }
  }


  def cacheMemberDetails(memberDetails: MemberDetails)(implicit hc: HeaderCarrier): Future[Option[GmpSession]] = {
    val timer = metrics.keystoreStoreTimer.time()
    logger.debug(s"[SingleCalculationSessionService][cacheMemberDetails] : $memberDetails")

    val updatedCacheData: Future[SingleCalculationSessionCache] = singleCalculationSessionRepository.get(getSessionId).map {
      case Some(returnedSession) => returnedSession.copy(gmpSession = returnedSession.gmpSession.copy(memberDetails = memberDetails))
      case None => cleanGmpSingleCalculationSessionCache(getSessionId, cleanGmpSession.copy(memberDetails = memberDetails))
    }

    updatedCacheData.flatMap { sessionCache =>
      setSessionCacheDataWithRetry(sessionCache).map { _ =>
        timer.stop()
        Some(sessionCache.gmpSession)
      }.recover { case e: Throwable =>
        timer.stop()
        logger.warn("[SingleCalculationSessionService][cacheMemberDetails] Error caching member details: " + e.getMessage)
        None
      }

    }
  }

  def fetchMemberDetails()(implicit hc: HeaderCarrier): Future[Option[MemberDetails]] = {
    val timer = metrics.keystoreStoreTimer.time()
    logger.debug("[SingleCalculationSessionService][fetchMemberDetails]: Fetching Member Details Session data...")

    getSessionCacheDataWithRetry.map { currentSession =>
      timer.stop()
      currentSession.map(_.gmpSession.memberDetails)
    }.recover { case e: Throwable =>
      timer.stop()
      logger.warn("[SingleCalculationSessionService][fetchMemberDetails] Error fetching member details: " + e.getMessage)
      None
    }
  }

  def cachePensionDetails(scon: String)(implicit hc: HeaderCarrier): Future[Option[GmpSession]] = {
    val timer = metrics.keystoreStoreTimer.time()
    logger.debug(s"[SingleCalculationSessionService][cachePensionDetails] : $scon")

    val updatedCacheData = singleCalculationSessionRepository.get(getSessionId).map {
      case Some(returnedSession) =>  returnedSession.copy(gmpSession = returnedSession.gmpSession.copy(scon = scon))
      case None => cleanGmpSingleCalculationSessionCache(getSessionId, cleanGmpSession.copy(scon = scon))
    }

    updatedCacheData.flatMap { sessionCache =>
      setSessionCacheDataWithRetry(sessionCache).map { _ =>
        timer.stop()
        Some(sessionCache.gmpSession)
      }.recover { case e: Throwable =>
        timer.stop()
        logger.warn("[SingleCalculationSessionService][cachePensionDetails] Error caching pension details: " + e.getMessage)
        None
      }
    }
  }

  def fetchPensionDetails()(implicit hc: HeaderCarrier): Future[Option[String]] = {
    val timer = metrics.keystoreStoreTimer.time()
    logger.debug("[SingleCalculationSessionService][fetchPensionDetails]")

    getSessionCacheDataWithRetry.map { currentSession =>
      timer.stop()
      currentSession.map(_.gmpSession.scon)
    }.recover { case e: Throwable =>
      timer.stop()
      logger.warn("[SingleCalculationSessionService][fetchPensionDetails] Error fetching pension details: " + e.getMessage)
      None
    }
  }

  def cacheScenario(scenario: String)(implicit hc: HeaderCarrier): Future[Option[GmpSession]] = {
    val timer = metrics.keystoreStoreTimer.time()
    logger.debug(s"[SingleCalculationSessionService][cacheScenario] : $scenario")

    val updatedCacheData = singleCalculationSessionRepository.get(getSessionId).map {
      case Some(returnedSession) => returnedSession.copy(gmpSession = returnedSession.gmpSession.copy(scenario = scenario, rate = None, revaluationDate = None))
      case None => cleanGmpSingleCalculationSessionCache(getSessionId, cleanGmpSession.copy(scenario = scenario))
    }

    updatedCacheData.flatMap { sessionCache =>
      setSessionCacheDataWithRetry(sessionCache).map { _ =>
        timer.stop()
        Some(sessionCache.gmpSession)
      }.recover { case e: Throwable =>
        timer.stop()
        logger.warn("[SingleCalculationSessionService][cacheScenario] Error caching scenario: " + e.getMessage)
        None
      }
    }
  }

  def fetchScenario()(implicit hc: HeaderCarrier): Future[Option[String]] = {
    val timer = metrics.keystoreStoreTimer.time()
    logger.debug("[SingleCalculationSessionService][fetchScenario]")

    getSessionCacheDataWithRetry.map { currentSession =>
      timer.stop()
      currentSession.map(_.gmpSession.scenario)
    }.recover { case e: Throwable =>
      timer.stop()
      logger.warn("[SingleCalculationSessionService][fetchScenario] Error fetching scenario details: " + e.getMessage)
      None
    }
  }

  def cacheEqualise(equalise: Option[Int])(implicit hc: HeaderCarrier): Future[Option[GmpSession]] = {
    val timer = metrics.keystoreStoreTimer.time()
    logger.debug(s"[SingleCalculationSessionService][cacheEqualise] : $equalise")

    val updatedCacheData = singleCalculationSessionRepository.get(getSessionId).map {
      case Some(returnedSession) => returnedSession.copy(gmpSession = returnedSession.gmpSession.copy(equalise = equalise))
      case None => cleanGmpSingleCalculationSessionCache(getSessionId, cleanGmpSession.copy(equalise = equalise))
    }

    updatedCacheData.flatMap { sessionCache =>
      setSessionCacheDataWithRetry(sessionCache).map { _ =>
        timer.stop()
        Some(sessionCache.gmpSession)
      }.recover { case e: Throwable =>
        timer.stop()
        logger.warn("[SingleCalculationSessionService][cacheEqualise] Error caching equalise option: " + e.getMessage)
        None
      }
    }
  }

  def cacheRevaluationDate(date: Option[GmpDate])(implicit hc: HeaderCarrier): Future[Option[GmpSession]] = {
    val timer = metrics.keystoreStoreTimer.time()
    logger.debug(s"[SingleCalculationSessionService][cacheRevaluationDate] : $date")

    val updatedCacheData = singleCalculationSessionRepository.get(getSessionId).map {
      case Some(returnedSession) =>
        (returnedSession.gmpSession.scenario, returnedSession.gmpSession.leaving.leaving) match {
          case (CalculationType.REVALUATION, Some(Leaving.NO)) => returnedSession.copy(gmpSession = returnedSession.gmpSession.copy(
              revaluationDate = date,
              rate = Some(RevaluationRate.HMRC),
              leaving = Leaving(date.get, Some(Leaving.NO))
            ))
          case _ => returnedSession.copy(gmpSession = returnedSession.gmpSession.copy(revaluationDate = date))
        }
      case None => cleanGmpSingleCalculationSessionCache(getSessionId, cleanGmpSession.copy(revaluationDate = date))
    }

    updatedCacheData.flatMap { sessionCache =>
      setSessionCacheDataWithRetry(sessionCache).map { _ =>
        timer.stop()
        Some(sessionCache.gmpSession)
      }.recover { case e: Throwable =>
        timer.stop()
        logger.warn("[SingleCalculationSessionService][cacheRevaluationDate] Error caching revaluation date: " + e.getMessage)
        None
      }
    }
  }

  def cacheLeaving(leaving: Leaving)(implicit hc: HeaderCarrier): Future[Option[GmpSession]] = {
    val timer = metrics.keystoreStoreTimer.time()
    logger.debug(s"[SingleCalculationSessionService][cacheLeaving] : $leaving")

    val updatedCacheData = singleCalculationSessionRepository.get(getSessionId).map {
      case Some(returnedSession) => returnedSession.copy(gmpSession = returnedSession.gmpSession.copy(leaving = leaving))
      case None => cleanGmpSingleCalculationSessionCache(getSessionId, cleanGmpSession.copy(leaving = leaving))
    }

    updatedCacheData.flatMap { sessionCache =>
      setSessionCacheDataWithRetry(sessionCache).map { _ =>
        timer.stop()
        Some(sessionCache.gmpSession)
      }.recover { case e: Throwable =>
        timer.stop()
        logger.warn("[SingleCalculationSessionService][cacheLeaving] Error caching leaving details: " + e.getMessage)
        None
      }
    }
  }

  def fetchLeaving()(implicit hc: HeaderCarrier): Future[Option[Leaving]] = {
    val timer = metrics.keystoreStoreTimer.time()
    logger.debug("[SingleCalculationSessionService][fetchLeaving]")

    getSessionCacheDataWithRetry.map { currentSession =>
      timer.stop()
      currentSession.map(_.gmpSession.leaving)
    }.recover { case e: Throwable =>
      timer.stop()
      logger.warn("[SingleCalculationSessionService][fetchLeaving] Error fetching leaving details: " + e.getMessage)
      None
    }
  }

  def cacheRevaluationRate(rate: String)(implicit hc: HeaderCarrier): Future[Option[GmpSession]] = {
    val timer = metrics.keystoreStoreTimer.time()
    logger.debug(s"[SingleCalculationSessionService][cacheRevaluationRate] : $rate")

    val updatedCacheData = singleCalculationSessionRepository.get(getSessionId).map {
      case Some(returnedSession) => returnedSession.copy(gmpSession = returnedSession.gmpSession.copy(rate = Some(rate)))
      case None => cleanGmpSingleCalculationSessionCache(getSessionId, cleanGmpSession.copy(rate = Some(rate)))
    }

    updatedCacheData.flatMap { sessionCache =>
      setSessionCacheDataWithRetry(sessionCache).map { _ =>
        timer.stop()
        Some(sessionCache.gmpSession)
      }.recover { case e: Throwable =>
        timer.stop()
        logger.warn("[SingleCalculationSessionService][cacheRevaluationRate] Error caching revaluation rate: " + e.getMessage)
        None
      }
    }
  }


  private def getSessionCacheDataWithRetry(implicit hc: HeaderCarrier): Future[Option[SingleCalculationSessionCache]] = {
    retry(appConfig.serviceMaxNoOfAttempts, "Reading Single Calculation Session") {
      singleCalculationSessionRepository.get(getSessionId)
    }
  }

  private def setSessionCacheDataWithRetry(updatedSession: SingleCalculationSessionCache): Future[Boolean] = {
    retry(appConfig.serviceMaxNoOfAttempts, "Storing Single Calculation Session") {
      singleCalculationSessionRepository.set(updatedSession)
    }
  }
}
