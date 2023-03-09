/*
 * Copyright 2023 HM Revenue & Customs
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

import com.google.inject.Inject
import config.GmpSessionCache
import metrics.ApplicationMetrics
import models._
import models.upscan._
import play.api.Logging
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class SessionService @Inject()(metrics: ApplicationMetrics,gmpSessionCache: GmpSessionCache, implicit val executionContext: ExecutionContext) extends Logging{

  val GMP_SESSION_KEY = "gmp_session"
  val cleanSession = GmpSession(MemberDetails("", "", ""), "", "", None, None, Leaving(GmpDate(None, None, None), None), None)

  val GMP_BULK_SESSION_KEY = "gmp_bulk_session"
  val CALLBACK_SESSION_KEY = "gmp_callback_session"
  val cleanBulkSession = GmpBulkSession(None, None, None)

  def fetchGmpBulkSession()(implicit hc: HeaderCarrier): Future[Option[GmpBulkSession]] = {

    val timer = metrics.keystoreStoreTimer.time()

    logger.debug(s"[SessionService][fetchGmpBulkSession]")

    gmpSessionCache.fetchAndGetEntry[GmpBulkSession](GMP_BULK_SESSION_KEY) map (gmpBulkSession => {
      timer.stop()
      gmpBulkSession
    })
  }

  def resetGmpBulkSession()(implicit hc: HeaderCarrier ): Future[Option[GmpBulkSession]] = {
    val timer = metrics.keystoreStoreTimer.time()

    logger.debug(s"[SessionService][fetchGmpBulkSession]")

    gmpSessionCache.cache[GmpBulkSession](GMP_BULK_SESSION_KEY, cleanBulkSession) map (cacheMap => {
      timer.stop()
      Some(cleanBulkSession)
    })
  }

  def cacheCallBackData(_callBackData: Option[UploadStatus])(implicit  hc: HeaderCarrier): Future[Option[GmpBulkSession]] = {
    val timer = metrics.keystoreStoreTimer.time()
      logger.debug(s"[SessionService][cacheCallBackData] : ${_callBackData}")

    val result = gmpSessionCache.fetchAndGetEntry[GmpBulkSession](GMP_BULK_SESSION_KEY) flatMap { currentSession =>
      gmpSessionCache.cache[GmpBulkSession](GMP_BULK_SESSION_KEY,
        currentSession match {
          case Some(returnedSession) => returnedSession.copy(callBackData = _callBackData)
          case None => cleanBulkSession.copy(callBackData = _callBackData)
        }
      )
    }

    result.map(cacheMap => {
      timer.stop()
      cacheMap.getEntry[GmpBulkSession](GMP_BULK_SESSION_KEY)
    })
  }

  def cacheEmailAndReference(_email: Option[String], _reference: Option[String])
                            (implicit  hc: HeaderCarrier ): Future[Option[GmpBulkSession]] = {
    val timer = metrics.keystoreStoreTimer.time()

    logger.debug(s"[SessionService][cacheEmailAndReferencea] : email: ${_email}; reference: ${_reference}")

    val result = gmpSessionCache.fetchAndGetEntry[GmpBulkSession](GMP_BULK_SESSION_KEY) flatMap { currentSession =>
      gmpSessionCache.cache[GmpBulkSession](GMP_BULK_SESSION_KEY,
        currentSession match {
          case Some(returnedSession) => returnedSession.copy(emailAddress = _email, reference = _reference)
          case None => cleanBulkSession.copy(emailAddress = _email, reference = _reference)
        }
      )
    }

    result.map(cacheMap => {
      timer.stop()
      cacheMap.getEntry[GmpBulkSession](GMP_BULK_SESSION_KEY)
    })
  }


  def fetchGmpSession()(implicit  hc: HeaderCarrier ): Future[Option[GmpSession]] = {
    val timer = metrics.keystoreStoreTimer.time()

    logger.debug(s"[SessionService][fetchGmpSession]")

    gmpSessionCache.fetchAndGetEntry[GmpSession](GMP_SESSION_KEY) map (gmpSession => {
      timer.stop()
      gmpSession
    })
  }

  def resetGmpSession()(implicit  hc: HeaderCarrier ): Future[Option[GmpSession]] = {
    val timer = metrics.keystoreStoreTimer.time()

    logger.debug(s"[SessionService][fetchGmpSession]")

    gmpSessionCache.cache[GmpSession](GMP_SESSION_KEY, cleanSession) map (cacheMap => {
      timer.stop()
      Some(cleanSession)
    })
  }

  def resetGmpSessionWithScon()(implicit hc: HeaderCarrier ): Future[Option[GmpSession]] = {
    val timer = metrics.keystoreStoreTimer.time()

    logger.debug(s"[SessionService][fetchGmpSessionWithScon]")

    fetchPensionDetails.flatMap { s =>
      val session = cleanSession.copy(scon = s.getOrElse(""))
      gmpSessionCache.cache[GmpSession](GMP_SESSION_KEY, session) map (cacheMap => {
        timer.stop()
        Some(session)
      })
    }
  }

  def cacheMemberDetails(memberDetails: MemberDetails)(implicit  hc: HeaderCarrier ): Future[Option[GmpSession]] = {
    val timer = metrics.keystoreStoreTimer.time()

    logger.debug(s"[SessionService][cacheMemberDetails] : $memberDetails")

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

  def fetchMemberDetails()(implicit hc: HeaderCarrier ): Future[Option[MemberDetails]] = {
    val timer = metrics.keystoreStoreTimer.time()

    logger.debug(s"[SessionService][fetchMemberDetails]")

    gmpSessionCache.fetchAndGetEntry[GmpSession](GMP_SESSION_KEY).map { currentSession =>
      currentSession.map {
        timer.stop()
        _.memberDetails
      }
    }
  }

  def cachePensionDetails(scon: String)(implicit  hc: HeaderCarrier ): Future[Option[GmpSession]] = {
    val timer = metrics.keystoreStoreTimer.time()

    logger.debug(s"[SessionService][cachePensionDetails] : $scon")

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

  def fetchPensionDetails()(implicit  hc: HeaderCarrier ): Future[Option[String]] = {
    val timer = metrics.keystoreStoreTimer.time()

    logger.debug(s"[SessionService][fetchPensionDetails]")

    gmpSessionCache.fetchAndGetEntry[GmpSession](GMP_SESSION_KEY).map { currentSession =>
      currentSession.map {
        timer.stop()
        _.scon
      }
    }
  }

  def cacheScenario(scenario: String)(implicit  hc: HeaderCarrier ): Future[Option[GmpSession]] = {
    val timer = metrics.keystoreStoreTimer.time()

    logger.debug(s"[SessionService][cacheScenario] : $scenario")

    val result = gmpSessionCache.fetchAndGetEntry[GmpSession](GMP_SESSION_KEY) flatMap { currentSession =>
      gmpSessionCache.cache[GmpSession](GMP_SESSION_KEY,
        currentSession match {
          case Some(returnedSession) => returnedSession.copy(scenario = scenario, rate = None, revaluationDate = None)
          case None => cleanSession.copy(scenario = scenario)
        }
      )
    }

    result.map(cacheMap => {
      timer.stop()
      cacheMap.getEntry[GmpSession](GMP_SESSION_KEY)
    })
  }

  def fetchScenario()(implicit hc: HeaderCarrier ): Future[Option[String]] = {
    val timer = metrics.keystoreStoreTimer.time()

    logger.debug(s"[SessionService][fetchScenario]")

    gmpSessionCache.fetchAndGetEntry[GmpSession](GMP_SESSION_KEY).map { currentSession =>
      currentSession.map {
        timer.stop()
        _.scenario
      }
    }
  }

  def cacheEqualise(_equalise: Option[Int])(implicit  hc: HeaderCarrier ): Future[Option[GmpSession]] = {
    val timer = metrics.keystoreStoreTimer.time()

    logger.debug(s"[SessionService][cacheEqualise] : ${_equalise}")

    val result = gmpSessionCache.fetchAndGetEntry[GmpSession](GMP_SESSION_KEY) flatMap { currentSession =>
      gmpSessionCache.cache[GmpSession](GMP_SESSION_KEY,
        currentSession match {
          case Some(returnedSession) => returnedSession.copy(equalise = _equalise)
          case None => cleanSession.copy(equalise = _equalise)
        }
      )
    }

    result.map(cacheMap => {
      timer.stop()
      cacheMap.getEntry[GmpSession](GMP_SESSION_KEY)
    })
  }

  def cacheRevaluationDate(date: Option[GmpDate])(implicit  hc: HeaderCarrier): Future[Option[GmpSession]] = {
    val timer = metrics.keystoreStoreTimer.time()

    logger.debug(s"[SessionService][cacheRevaluationDate] : $date")

    val result = gmpSessionCache.fetchAndGetEntry[GmpSession](GMP_SESSION_KEY) flatMap { currentSession =>
      gmpSessionCache.cache[GmpSession](GMP_SESSION_KEY,
        currentSession match {
          case Some(returnedSession) => {
            (returnedSession.scenario, returnedSession.leaving.leaving) match {
              case (CalculationType.REVALUATION, Some(Leaving.NO)) =>
                returnedSession.copy(revaluationDate = date, rate = Some(RevaluationRate.HMRC), leaving = Leaving(date.get, Some(Leaving.NO)))
              case _ => returnedSession.copy(revaluationDate = date)
            }
          }
          case None => cleanSession.copy(revaluationDate = date)
        }
      )
    }

    result.map(cacheMap => {
      timer.stop()
      cacheMap.getEntry[GmpSession](GMP_SESSION_KEY)
    })
  }

  def cacheLeaving(leaving: Leaving)(implicit  hc: HeaderCarrier ): Future[Option[GmpSession]] = {
    val timer = metrics.keystoreStoreTimer.time()

    logger.debug(s"[SessionService][cacheLeaving] : $leaving")

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

  def fetchLeaving()(implicit  hc: HeaderCarrier ): Future[Option[Leaving]] = {
    val timer = metrics.keystoreStoreTimer.time()

    logger.debug(s"[SessionService][fetchLeaving]")

    gmpSessionCache.fetchAndGetEntry[GmpSession](GMP_SESSION_KEY).map { currentSession =>
      currentSession.map {
        timer.stop()
        _.leaving
      }
    }
  }

  def cacheRevaluationRate(rate: String)(implicit  hc: HeaderCarrier ): Future[Option[GmpSession]] = {
    val timer = metrics.keystoreStoreTimer.time()

    logger.debug(s"[SessionService][cacheRevaluationRate] : $rate")

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

  def createCallbackRecord(implicit  hc: HeaderCarrier): Future[Any] = {
    gmpSessionCache.cache[UploadStatus](CALLBACK_SESSION_KEY, NotStarted)
  }

  def updateCallbackRecord(sessionId: String, uploadStatus: UploadStatus)(implicit  hc: HeaderCarrier): Future[Any] = {
    gmpSessionCache.cache(gmpSessionCache.defaultSource, sessionId, CALLBACK_SESSION_KEY, uploadStatus)
  }

  def getCallbackRecord(implicit  hc: HeaderCarrier): Future[Option[UploadStatus]] = {
    gmpSessionCache.fetchAndGetEntry[UploadStatus](CALLBACK_SESSION_KEY)
  }

}
