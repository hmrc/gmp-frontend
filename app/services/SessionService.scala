/*
 * Copyright 2020 HM Revenue & Customs
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
import play.api.Logger
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Request
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SessionService @Inject()(metrics: ApplicationMetrics){

  val GMP_SESSION_KEY = "gmp_session"
  val cleanSession = GmpSession(MemberDetails("", "", ""), "", "", None, None, Leaving(GmpDate(None, None, None), None), None)

  val GMP_BULK_SESSION_KEY = "gmp_bulk_session"
  val cleanBulkSession = GmpBulkSession(None, None, None)

  def fetchGmpBulkSession()(implicit request: Request[_], hc: HeaderCarrier ,gmpsessionCache: GmpSessionCache): Future[Option[GmpBulkSession]] = {

    val timer = metrics.keystoreStoreTimer.time()

    Logger.debug(s"[SessionService][fetchGmpBulkSession]")

    gmpsessionCache.fetchAndGetEntry[GmpBulkSession](GMP_BULK_SESSION_KEY) map (gmpBulkSession => {
      timer.stop()
      gmpBulkSession
    })
  }

  def resetGmpBulkSession()(implicit request: Request[_], hc: HeaderCarrier ,gmpsessionCache: GmpSessionCache): Future[Option[GmpBulkSession]] = {
    val timer = metrics.keystoreStoreTimer.time()

    Logger.debug(s"[SessionService][fetchGmpBulkSession]")

    gmpsessionCache.cache[GmpBulkSession](GMP_BULK_SESSION_KEY, cleanBulkSession) map (cacheMap => {
      timer.stop()
      Some(cleanBulkSession)
    })
  }

  def cacheCallBackData(_callBackData: Option[CallBackData])(implicit request: Request[_], hc: HeaderCarrier ): Future[Option[GmpBulkSession]] = {
    val timer = metrics.keystoreStoreTimer.time()
implicit val gmpsessionCache: GmpSessionCache=GuiceApplicationBuilder().injector().instanceOf[GmpSessionCache]
    Logger.debug(s"[SessionService][cacheCallBackData] : ${_callBackData}")

    val result = gmpsessionCache.fetchAndGetEntry[GmpBulkSession](GMP_BULK_SESSION_KEY) flatMap { currentSession =>
      gmpsessionCache.cache[GmpBulkSession](GMP_BULK_SESSION_KEY,
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

  def cacheEmailAndReference(_email: Option[String], _reference: Option[String])(implicit request: Request[_], hc: HeaderCarrier ,gmpsessionCache: GmpSessionCache): Future[Option[GmpBulkSession]] = {
    val timer = metrics.keystoreStoreTimer.time()

    Logger.debug(s"[SessionService][cacheEmailAndReferencea] : email: ${_email}; reference: ${_reference}")

    val result = gmpsessionCache.fetchAndGetEntry[GmpBulkSession](GMP_BULK_SESSION_KEY) flatMap { currentSession =>
      gmpsessionCache.cache[GmpBulkSession](GMP_BULK_SESSION_KEY,
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


  def fetchGmpSession()(implicit request: Request[_], hc: HeaderCarrier ,gmpsessionCache: GmpSessionCache): Future[Option[GmpSession]] = {
    val timer = metrics.keystoreStoreTimer.time()

    Logger.debug(s"[SessionService][fetchGmpSession]")

    gmpsessionCache.fetchAndGetEntry[GmpSession](GMP_SESSION_KEY) map (gmpSession => {
      timer.stop()
      gmpSession
    })
  }

  def resetGmpSession()(implicit request: Request[_], hc: HeaderCarrier ,gmpsessionCache: GmpSessionCache): Future[Option[GmpSession]] = {
    val timer = metrics.keystoreStoreTimer.time()

    Logger.debug(s"[SessionService][fetchGmpSession]")

    gmpsessionCache.cache[GmpSession](GMP_SESSION_KEY, cleanSession) map (cacheMap => {
      timer.stop()
      Some(cleanSession)
    })
  }

  def resetGmpSessionWithScon()(implicit request: Request[_], hc: HeaderCarrier ,gmpsessionCache: GmpSessionCache): Future[Option[GmpSession]] = {
    val timer = metrics.keystoreStoreTimer.time()

    Logger.debug(s"[SessionService][fetchGmpSessionWithScon]")

    fetchPensionDetails.flatMap { s =>
      val session = cleanSession.copy(scon = s.getOrElse(""))
      gmpsessionCache.cache[GmpSession](GMP_SESSION_KEY, session) map (cacheMap => {
        timer.stop()
        Some(session)
      })
    }
  }

  def cacheMemberDetails(memberDetails: MemberDetails)(implicit request: Request[_], hc: HeaderCarrier ,gmpsessionCache: GmpSessionCache): Future[Option[GmpSession]] = {
    val timer = metrics.keystoreStoreTimer.time()

    Logger.debug(s"[SessionService][cacheMemberDetails] : $memberDetails")

    val result = gmpsessionCache.fetchAndGetEntry[GmpSession](GMP_SESSION_KEY) flatMap { currentSession =>
      gmpsessionCache.cache[GmpSession](GMP_SESSION_KEY,
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

  def fetchMemberDetails()(implicit request: Request[_], hc: HeaderCarrier ,gmpsessionCache: GmpSessionCache): Future[Option[MemberDetails]] = {
    val timer = metrics.keystoreStoreTimer.time()

    Logger.debug(s"[SessionService][fetchMemberDetails]")

    gmpsessionCache.fetchAndGetEntry[GmpSession](GMP_SESSION_KEY).map { currentSession =>
      currentSession.map {
        timer.stop()
        _.memberDetails
      }
    }
  }

  def cachePensionDetails(scon: String)(implicit request: Request[_], hc: HeaderCarrier ,gmpsessionCache: GmpSessionCache): Future[Option[GmpSession]] = {
    val timer = metrics.keystoreStoreTimer.time()

    Logger.debug(s"[SessionService][cachePensionDetails] : $scon")

    val result = gmpsessionCache.fetchAndGetEntry[GmpSession](GMP_SESSION_KEY) flatMap { currentSession =>
      gmpsessionCache.cache[GmpSession](GMP_SESSION_KEY,
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

  def fetchPensionDetails()(implicit request: Request[_], hc: HeaderCarrier ,gmpsessionCache: GmpSessionCache): Future[Option[String]] = {
    val timer = metrics.keystoreStoreTimer.time()

    Logger.debug(s"[SessionService][fetchPensionDetails]")

    gmpsessionCache.fetchAndGetEntry[GmpSession](GMP_SESSION_KEY).map { currentSession =>
      currentSession.map {
        timer.stop()
        _.scon
      }
    }
  }

  def cacheScenario(scenario: String)(implicit request: Request[_], hc: HeaderCarrier ,gmpsessionCache: GmpSessionCache): Future[Option[GmpSession]] = {
    val timer = metrics.keystoreStoreTimer.time()

    Logger.debug(s"[SessionService][cacheScenario] : $scenario")

    val result = gmpsessionCache.fetchAndGetEntry[GmpSession](GMP_SESSION_KEY) flatMap { currentSession =>
      gmpsessionCache.cache[GmpSession](GMP_SESSION_KEY,
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

  def fetchScenario()(implicit request: Request[_], hc: HeaderCarrier ,gmpsessionCache: GmpSessionCache): Future[Option[String]] = {
    val timer = metrics.keystoreStoreTimer.time()

    Logger.debug(s"[SessionService][fetchScenario]")

    gmpsessionCache.fetchAndGetEntry[GmpSession](GMP_SESSION_KEY).map { currentSession =>
      currentSession.map {
        timer.stop()
        _.scenario
      }
    }
  }

  def cacheEqualise(_equalise: Option[Int])(implicit request: Request[_], hc: HeaderCarrier ,gmpsessionCache: GmpSessionCache): Future[Option[GmpSession]] = {
    val timer = metrics.keystoreStoreTimer.time()

    Logger.debug(s"[SessionService][cacheEqualise] : ${_equalise}")

    val result = gmpsessionCache.fetchAndGetEntry[GmpSession](GMP_SESSION_KEY) flatMap { currentSession =>
      gmpsessionCache.cache[GmpSession](GMP_SESSION_KEY,
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

  def cacheRevaluationDate(date: Option[GmpDate])(implicit request: Request[_], hc: HeaderCarrier ,gmpsessionCache: GmpSessionCache): Future[Option[GmpSession]] = {
    val timer = metrics.keystoreStoreTimer.time()

    Logger.debug(s"[SessionService][cacheRevaluationDate] : $date")

    val result = gmpsessionCache.fetchAndGetEntry[GmpSession](GMP_SESSION_KEY) flatMap { currentSession =>
      gmpsessionCache.cache[GmpSession](GMP_SESSION_KEY,
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

  def cacheLeaving(leaving: Leaving)(implicit request: Request[_], hc: HeaderCarrier ,gmpsessionCache: GmpSessionCache): Future[Option[GmpSession]] = {
    val timer = metrics.keystoreStoreTimer.time()

    Logger.debug(s"[SessionService][cacheLeaving] : $leaving")

    val result = gmpsessionCache.fetchAndGetEntry[GmpSession](GMP_SESSION_KEY) flatMap { currentSession =>
      gmpsessionCache.cache[GmpSession](GMP_SESSION_KEY,
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

  def fetchLeaving()(implicit request: Request[_], hc: HeaderCarrier ,gmpsessionCache: GmpSessionCache): Future[Option[Leaving]] = {
    val timer = metrics.keystoreStoreTimer.time()

    Logger.debug(s"[SessionService][fetchLeaving]")

    gmpsessionCache.fetchAndGetEntry[GmpSession](GMP_SESSION_KEY).map { currentSession =>
      currentSession.map {
        timer.stop()
        _.leaving
      }
    }
  }

  def cacheRevaluationRate(rate: String)(implicit request: Request[_], hc: HeaderCarrier ,gmpsessionCache: GmpSessionCache): Future[Option[GmpSession]] = {
    val timer = metrics.keystoreStoreTimer.time()

    Logger.debug(s"[SessionService][cacheRevaluationRate] : $rate")

    val result = gmpsessionCache.fetchAndGetEntry[GmpSession](GMP_SESSION_KEY) flatMap { currentSession =>
      gmpsessionCache.cache[GmpSession](GMP_SESSION_KEY,
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
