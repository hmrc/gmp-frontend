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
import config.GmpSessionCache
import metrics.ApplicationMetrics
import models._
import models.upscan._
import play.api.Logging
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class GMPBulkSessionService @Inject()(metrics: ApplicationMetrics,
                               gmpSessionCache: GmpSessionCache)(implicit ec: ExecutionContext) extends Logging{

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
}
