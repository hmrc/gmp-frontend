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
import metrics.ApplicationMetrics
import models.{GmpDate, GmpSession, Leaving, MemberDetails}
import play.api.Logging
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SingleCalculationSessionService @Inject()(
                                                 metrics: ApplicationMetrics,
                                                 gmpSessionCache: GmpSessionCache)(implicit ec: ExecutionContext) extends Logging{

  val GMP_SESSION_KEY = "gmp_session"
  val cleanSession = GmpSession(MemberDetails("", "", ""), "", "", None, None, Leaving(GmpDate(None, None, None), None), None)


  def cacheMemberDetails(memberDetails: MemberDetails)(implicit hc: HeaderCarrier): Future[Option[GmpSession]] = {
    val timer = metrics.keystoreStoreTimer.time()
    logger.debug(s"[SingleCalculationSessionService][cacheMemberDetails] : $memberDetails")
    val result = gmpSessionCache.fetchAndGetEntry[GmpSession](GMP_SESSION_KEY) flatMap { currentSession =>
      gmpSessionCache.cache[GmpSession](GMP_SESSION_KEY,
        currentSession match {
          case Some(returnedSession) => returnedSession.copy(memberDetails = memberDetails)
          case None => cleanSession.copy(memberDetails = memberDetails)
        }
      )
    }
    result.map(cacheMap => {
      timer.stop()
      cacheMap.getEntry[GmpSession](GMP_SESSION_KEY)
    })
  }

  def fetchMemberDetails()(implicit hc: HeaderCarrier): Future[Option[MemberDetails]] = {
    val timer = metrics.keystoreStoreTimer.time()
    logger.debug("[SingleCalculationSessionService][fetchMemberDetails]")
    gmpSessionCache.fetchAndGetEntry[GmpSession](GMP_SESSION_KEY).map { currentSession =>
      timer.stop()
      currentSession.map(_.memberDetails)
    }
  }

  def cachePensionDetails(scon: String)(implicit hc: HeaderCarrier): Future[Option[GmpSession]] = {
    val timer = metrics.keystoreStoreTimer.time()
    logger.debug(s"[SingleCalculationSessionService][cachePensionDetails] : $scon")
    val result = gmpSessionCache.fetchAndGetEntry[GmpSession](GMP_SESSION_KEY) flatMap { currentSession =>
      gmpSessionCache.cache[GmpSession](GMP_SESSION_KEY,
        currentSession match {
          case Some(returnedSession) => returnedSession.copy(scon = scon)
          case None => cleanSession.copy(scon = scon)
        }
      )
    }
    result.map(cacheMap => {
      timer.stop()
      cacheMap.getEntry[GmpSession](GMP_SESSION_KEY)
    })
  }

  def fetchPensionDetails()(implicit hc: HeaderCarrier): Future[Option[String]] = {
    val timer = metrics.keystoreStoreTimer.time()
    logger.debug("[SingleCalculationSessionService][fetchPensionDetails]")
    gmpSessionCache.fetchAndGetEntry[GmpSession](GMP_SESSION_KEY).map { currentSession =>
      timer.stop()
      currentSession.map(_.scon)
    }
  }

  def cacheScenario(scenario: String)(implicit hc: HeaderCarrier): Future[Option[GmpSession]] = {
    val timer = metrics.keystoreStoreTimer.time()
    logger.debug(s"[SingleCalculationSessionService][cacheScenario] : $scenario")
    val result = gmpSessionCache.fetchAndGetEntry[GmpSession](GMP_SESSION_KEY) flatMap { currentSession =>
      gmpSessionCache.cache[GmpSession](GMP_SESSION_KEY,
        currentSession match {
          case Some(returnedSession) => returnedSession.copy(scenario = scenario)
          case None => cleanSession.copy(scenario = scenario)
        }
      )
    }
    result.map(cacheMap => {
      timer.stop()
      cacheMap.getEntry[GmpSession](GMP_SESSION_KEY)
    })
  }

  def fetchScenario()(implicit hc: HeaderCarrier): Future[Option[String]] = {
    val timer = metrics.keystoreStoreTimer.time()
    logger.debug("[SingleCalculationSessionService][fetchScenario]")
    gmpSessionCache.fetchAndGetEntry[GmpSession](GMP_SESSION_KEY).map { currentSession =>
      timer.stop()
      currentSession.map(_.scenario)
    }
  }

  def cacheEqualise(equalise: Option[Int])(implicit hc: HeaderCarrier): Future[Option[GmpSession]] = {
    val timer = metrics.keystoreStoreTimer.time()
    logger.debug(s"[SingleCalculationSessionService][cacheEqualise] : $equalise")
    val result = gmpSessionCache.fetchAndGetEntry[GmpSession](GMP_SESSION_KEY) flatMap { currentSession =>
      gmpSessionCache.cache[GmpSession](GMP_SESSION_KEY,
        currentSession match {
          case Some(returnedSession) => returnedSession.copy(equalise = equalise)
          case None => cleanSession.copy(equalise = equalise)
        }
      )
    }
    result.map(cacheMap => {
      timer.stop()
      cacheMap.getEntry[GmpSession](GMP_SESSION_KEY)
    })
  }

  def cacheRevaluationDate(date: Option[GmpDate])(implicit hc: HeaderCarrier): Future[Option[GmpSession]] = {
    val timer = metrics.keystoreStoreTimer.time()
    logger.debug(s"[SingleCalculationSessionService][cacheRevaluationDate] : $date")
    val result = gmpSessionCache.fetchAndGetEntry[GmpSession](GMP_SESSION_KEY) flatMap { currentSession =>
      gmpSessionCache.cache[GmpSession](GMP_SESSION_KEY,
        currentSession match {
          case Some(returnedSession) => returnedSession.copy(revaluationDate = date)
          case None => cleanSession.copy(revaluationDate = date)
        }
      )
    }
    result.map(cacheMap => {
      timer.stop()
      cacheMap.getEntry[GmpSession](GMP_SESSION_KEY)
    })
  }

  def cacheLeaving(leaving: Leaving)(implicit hc: HeaderCarrier): Future[Option[GmpSession]] = {
    val timer = metrics.keystoreStoreTimer.time()
    logger.debug(s"[SingleCalculationSessionService][cacheLeaving] : $leaving")
    val result = gmpSessionCache.fetchAndGetEntry[GmpSession](GMP_SESSION_KEY) flatMap { currentSession =>
      gmpSessionCache.cache[GmpSession](GMP_SESSION_KEY,
        currentSession match {
          case Some(returnedSession) => returnedSession.copy(leaving = leaving)
          case None => cleanSession.copy(leaving = leaving)
        }
      )
    }
    result.map(cacheMap => {
      timer.stop()
      cacheMap.getEntry[GmpSession](GMP_SESSION_KEY)
    })
  }

  def fetchLeaving()(implicit hc: HeaderCarrier): Future[Option[Leaving]] = {
    val timer = metrics.keystoreStoreTimer.time()
    logger.debug("[SingleCalculationSessionService][fetchLeaving]")
    gmpSessionCache.fetchAndGetEntry[GmpSession](GMP_SESSION_KEY).map { currentSession =>
      timer.stop()
      currentSession.map(_.leaving)
    }
  }

  def cacheRevaluationRate(rate: String)(implicit hc: HeaderCarrier): Future[Option[GmpSession]] = {
    val timer = metrics.keystoreStoreTimer.time()
    logger.debug(s"[SingleCalculationSessionService][cacheRevaluationRate] : $rate")
    val result = gmpSessionCache.fetchAndGetEntry[GmpSession](GMP_SESSION_KEY) flatMap { currentSession =>
      gmpSessionCache.cache[GmpSession](GMP_SESSION_KEY,
        currentSession match {
          case Some(returnedSession) => returnedSession.copy(rate = Some(rate))
          case None => cleanSession.copy(rate = Some(rate))
        }
      )
    }
    result.map(cacheMap => {
      timer.stop()
      cacheMap.getEntry[GmpSession](GMP_SESSION_KEY)
    })
  }

}
