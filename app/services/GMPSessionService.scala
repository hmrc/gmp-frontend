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
import models._
import models.upscan._
import services.helper.Retryable
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class GMPSessionService @Inject()(gmpBulkSessionService: GMPBulkSessionService,
                                  singleCalculationSessionService: SingleCalculationSessionService,
                                  sessionService: SessionService,
                                  appConfig: ApplicationConfig) extends Retryable{

  val GMP_SESSION_KEY = "gmp_session"
  val cleanSession = GmpSession(MemberDetails("", "", ""), "", "", None, None, Leaving(GmpDate(None, None, None), None), None)

  val GMP_BULK_SESSION_KEY = "gmp_bulk_session"
  val CALLBACK_SESSION_KEY = "gmp_callback_session"
  val cleanBulkSession = GmpBulkSession(None, None, None)


  private def featureSwitchCheck[A](expr1: => Future[A], expr2: => Future[A]): Future[A] = {
    if (appConfig.isMongoDBCacheEnabled) expr1 else expr2
  }

  def fetchGmpBulkSession()(implicit hc: HeaderCarrier): Future[Option[GmpBulkSession]] = {
    featureSwitchCheck(
      gmpBulkSessionService.fetchGmpBulkSession(),
      sessionService.fetchGmpBulkSession()
    )
  }

    def resetGmpBulkSession()(implicit hc: HeaderCarrier ): Future[Option[GmpBulkSession]] = {
      featureSwitchCheck(
        gmpBulkSessionService.resetGmpBulkSession(),
        sessionService.resetGmpBulkSession()
      )
  }

  def cacheCallBackData(_callBackData: Option[UploadStatus])(implicit  hc: HeaderCarrier): Future[Option[GmpBulkSession]] = {
    featureSwitchCheck(
      gmpBulkSessionService.cacheCallBackData(_callBackData),
      sessionService.cacheCallBackData(_callBackData)
    )
  }

  def cacheEmailAndReference(_email: Option[String], _reference: Option[String])
                            (implicit  hc: HeaderCarrier ): Future[Option[GmpBulkSession]] = {
    featureSwitchCheck(
      gmpBulkSessionService.cacheEmailAndReference(_email, _reference),
      sessionService.cacheEmailAndReference(_email, _reference)
    )
  }

  def fetchGmpSession()(implicit  hc: HeaderCarrier ): Future[Option[GmpSession]] = {
    featureSwitchCheck(
      singleCalculationSessionService.fetchGmpSession(),
      sessionService.fetchGmpSession()
    )
  }

  def resetGmpSession()(implicit  hc: HeaderCarrier ): Future[Option[GmpSession]] = {
    featureSwitchCheck(
      singleCalculationSessionService.resetGmpSession(),
      sessionService.resetGmpSession()
    )
  }

  def resetGmpSessionWithScon()(implicit hc: HeaderCarrier ): Future[Option[GmpSession]] = {
    featureSwitchCheck(
      singleCalculationSessionService.resetGmpSessionWithScon(),
      sessionService.resetGmpSessionWithScon()
    )
  }

  def cacheMemberDetails(memberDetails: MemberDetails)(implicit  hc: HeaderCarrier ): Future[Option[GmpSession]] = {
    featureSwitchCheck(
      singleCalculationSessionService.cacheMemberDetails(memberDetails),
      sessionService.cacheMemberDetails(memberDetails)
    )
  }

  def fetchMemberDetails()(implicit hc: HeaderCarrier ): Future[Option[MemberDetails]] = {
    featureSwitchCheck(
      singleCalculationSessionService.fetchMemberDetails(),
      sessionService.fetchMemberDetails()
    )
  }

  def cachePensionDetails(scon: String)(implicit  hc: HeaderCarrier ): Future[Option[GmpSession]] = {
    featureSwitchCheck(
      singleCalculationSessionService.cachePensionDetails(scon),
      sessionService.cachePensionDetails(scon)
    )
  }

  def fetchPensionDetails()(implicit  hc: HeaderCarrier ): Future[Option[String]] = {
    featureSwitchCheck(
      singleCalculationSessionService.fetchPensionDetails(),
      sessionService.fetchPensionDetails()
    )
  }

  def cacheScenario(scenario: String)(implicit  hc: HeaderCarrier ): Future[Option[GmpSession]] = {
    featureSwitchCheck(
      singleCalculationSessionService.cacheScenario(scenario),
      sessionService.cacheScenario(scenario)
    )
  }

  def fetchScenario()(implicit hc: HeaderCarrier ): Future[Option[String]] = {
    featureSwitchCheck(
      singleCalculationSessionService.fetchScenario(),
      sessionService.fetchScenario()
    )
  }

  def cacheEqualise(_equalise: Option[Int])(implicit  hc: HeaderCarrier ): Future[Option[GmpSession]] = {
    featureSwitchCheck(
      singleCalculationSessionService.cacheEqualise(_equalise),
      sessionService.cacheEqualise(_equalise)
    )
  }

  def cacheRevaluationDate(date: Option[GmpDate])(implicit  hc: HeaderCarrier): Future[Option[GmpSession]] = {
    featureSwitchCheck(
      singleCalculationSessionService.cacheRevaluationDate(date),
      sessionService.cacheRevaluationDate(date)
    )
  }

  def cacheLeaving(leaving: Leaving)(implicit  hc: HeaderCarrier ): Future[Option[GmpSession]] = {
    featureSwitchCheck(
      singleCalculationSessionService.cacheLeaving(leaving),
      sessionService.cacheLeaving(leaving)
    )
  }

  def fetchLeaving()(implicit  hc: HeaderCarrier ): Future[Option[Leaving]] = {
    featureSwitchCheck(
      singleCalculationSessionService.fetchLeaving(),
      sessionService.fetchLeaving()
    )
  }

  def cacheRevaluationRate(rate: String)(implicit  hc: HeaderCarrier ): Future[Option[GmpSession]] = {
    featureSwitchCheck(
      singleCalculationSessionService.cacheRevaluationRate(rate),
      sessionService.cacheRevaluationRate(rate)
    )
  }

  def createCallbackRecord(implicit  hc: HeaderCarrier): Future[Any] = {
    featureSwitchCheck(
      gmpBulkSessionService.createCallbackRecord,
      sessionService.createCallbackRecord
    )
  }

  def updateCallbackRecord(sessionId: String, uploadStatus: UploadStatus)(implicit  hc: HeaderCarrier): Future[Any] = {
    featureSwitchCheck(
      gmpBulkSessionService.updateCallbackRecord(uploadStatus),
      sessionService.updateCallbackRecord(sessionId, uploadStatus)
    )
  }

  def getCallbackRecord(implicit  hc: HeaderCarrier): Future[Option[UploadStatus]] = {
    featureSwitchCheck(
      gmpBulkSessionService.getCallbackRecord,
      sessionService.getCallbackRecord
    )
  }

}
