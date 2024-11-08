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

import com.google.inject.Inject
import config.ApplicationConfig
import metrics.ApplicationMetrics
import models._
import models.upscan.{NotStarted, UploadStatus}
import repositories.GMPBulkSessionRepository
import services.helper.Retryable
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class GMPBulkSessionService @Inject()(
                                       metrics: ApplicationMetrics,
                                       appConfig: ApplicationConfig,
                                       gmpBulkSessionRepository: GMPBulkSessionRepository
                                     )(implicit ec: ExecutionContext) extends Retryable {

  val cleanBulkSession = GmpBulkSession(None, None, None)

  private def cleanGmpBulkSessionCache(
                                     id: String,
                                     gmpBulkSession: GmpBulkSession
                                   ): GMPBulkSessionCache =
    GMPBulkSessionCache(id, gmpBulkSession)

  private def getSessionId(implicit hc: HeaderCarrier): String = {
    hc.sessionId match {
      case Some(id) => id.value
      case None => throw new RuntimeException("Unexpected error, No session id found")
    }
  }

  def fetchGmpBulkSession()(implicit hc: HeaderCarrier): Future[Option[GmpBulkSession]] = {
    val timer = metrics.keystoreStoreTimer.time()
    logger.info("[GMPBulkSessionService][fetchGmpBulkSession]: Fetching Bulk Session data...")

    getSessionCacheDataWithRetry.map { result =>
      timer.stop()
      result.map(_.gmpBulkSession)
    }.recover { case e: Throwable =>
      timer.stop()
      logger.warn("[GMPBulkSessionService][fetchGmpBulkSession] Error fetching Bulk Session data: " + e.getMessage)
      None
    }
  }

  def resetGmpBulkSession()(implicit hc: HeaderCarrier): Future[Option[GmpBulkSession]] = {
    val timer = metrics.keystoreStoreTimer.time()
    logger.info("[GMPBulkSessionService][resetGmpBulkSession]: Resetting Bulk Session data...")

    val cacheData = cleanGmpBulkSessionCache(getSessionId, cleanBulkSession)
    setSessionCacheDataWithRetry(cacheData).map { _ =>
      timer.stop()
      Some(cleanBulkSession)
    }
  }

  def cacheCallBackData(_callBackData: Option[UploadStatus])(implicit hc: HeaderCarrier): Future[Option[GmpBulkSession]] = {
    val timer = metrics.keystoreStoreTimer.time()
    logger.debug(s"[GMPBulkSessionService][cacheCallBackData] : ${_callBackData}")

    val updatedCacheData: Future[Option[GMPBulkSessionCache]] = gmpBulkSessionRepository.get(getSessionId).map {
      case Some(returnedSession) =>
        Some(returnedSession.copy(gmpBulkSession = returnedSession.gmpBulkSession.copy(callBackData = _callBackData)))
      case None =>
        Some(cleanGmpBulkSessionCache(getSessionId, cleanBulkSession.copy(callBackData = _callBackData)))
    }

    updatedCacheData.flatMap {
      case Some(bulkSessionCache) =>
        gmpBulkSessionRepository.set(bulkSessionCache).map { _ =>
          timer.stop()
          Some(bulkSessionCache.gmpBulkSession)
        }
      case None =>
        timer.stop()
        Future.successful(None)
    }

  }

  def cacheEmailAndReference(_email: Option[String], _reference: Option[String])(implicit hc: HeaderCarrier): Future[Option[GmpBulkSession]] = {
    val timer = metrics.keystoreStoreTimer.time()

    logger.debug(s"[GMPBulkSessionService][cacheEmailAndReferencea] : email: ${_email}; reference: ${_reference}")

    val updatedCache = gmpBulkSessionRepository.get(getSessionId).map {
      case Some(bulkSessionCache) =>
        bulkSessionCache.copy(gmpBulkSession = bulkSessionCache.gmpBulkSession.copy(emailAddress = _email, reference = _reference))
      case None =>
        cleanGmpBulkSessionCache(getSessionId, cleanBulkSession.copy(emailAddress = _email, reference = _reference))
    }

    updatedCache.flatMap { sessionCache =>
      setSessionCacheDataWithRetry(sessionCache).map { _ =>
        timer.stop()
        Some(sessionCache.gmpBulkSession)
      }.recover { case e: Throwable =>
        timer.stop()
        logger.warn("[GMPBulkSessionService][cacheEmailAndReference] Error storing Email and Reference: " + e.getMessage)
        None
      }
    }
  }


  def createCallbackRecord(implicit hc: HeaderCarrier): Future[Any] = {
    logger.info("[GMPBulkSessionService][createCallbackRecord]: Creating Callback Record...")

    getSessionCacheDataWithRetry.map {_.map { gmpBulkSessionCache =>
        val updatedSession = gmpBulkSessionCache.copy(
          gmpBulkSession = gmpBulkSessionCache.gmpBulkSession.copy(callBackData = Some(NotStarted))
        )

        setSessionCacheDataWithRetry(updatedSession)
      }
    }
  }

  def updateCallbackRecord(uploadStatus: UploadStatus)(implicit hc: HeaderCarrier): Future[Any] = {
    logger.info("[GMPBulkSessionService][updateCallbackRecord]: Updating Callback Record...")

    getSessionCacheDataWithRetry.map {
      _.map { gmpBulkSessionCache =>
        val updatedSession = gmpBulkSessionCache.copy(
          gmpBulkSession = gmpBulkSessionCache.gmpBulkSession.copy(callBackData = Some(uploadStatus))
        )

        setSessionCacheDataWithRetry(updatedSession)
      }
    }
  }


  def getCallbackRecord(implicit hc: HeaderCarrier): Future[Option[UploadStatus]] = {
    val timer = metrics.keystoreStoreTimer.time()
    logger.info("[GMPBulkSessionService][getCallbackRecord]: Reading Callback Record...")

    gmpBulkSessionRepository.get(getSessionId).map { result =>
      timer.stop()
      result.flatMap(_.gmpBulkSession.callBackData)
    }
  }

  private def getSessionCacheDataWithRetry(implicit hc: HeaderCarrier): Future[Option[GMPBulkSessionCache]] = {
    retry(appConfig.serviceMaxNoOfAttempts, "Reading GMP Bulk Session") {
      gmpBulkSessionRepository.get(getSessionId)
    }
  }

  private def setSessionCacheDataWithRetry(updatedSession: GMPBulkSessionCache): Future[Boolean] = {
    retry(appConfig.serviceMaxNoOfAttempts, "Storing GMP Bulk Session") {
      gmpBulkSessionRepository.set(updatedSession)
    }
  }
}
