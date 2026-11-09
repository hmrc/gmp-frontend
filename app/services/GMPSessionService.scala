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
import models.*
import models.upscan.*
import services.helper.Retryable
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class GMPSessionService @Inject() (
  gmpBulkSessionService:           GMPBulkSessionService,
  singleCalculationSessionService: SingleCalculationSessionService
) extends Retryable {

  val GMP_SESSION_KEY = "gmp_session"
  val cleanSession    = GmpSession(MemberDetails("", "", ""), "", "", None, None, Leaving(GmpDate(None, None, None), None), None)

  val GMP_BULK_SESSION_KEY = "gmp_bulk_session"
  val CALLBACK_SESSION_KEY = "gmp_callback_session"
  val cleanBulkSession     = GmpBulkSession(None, None, None)

  def fetchGmpBulkSession()(implicit hc: HeaderCarrier): Future[Option[GmpBulkSession]] =
    gmpBulkSessionService.fetchGmpBulkSession()

  def resetGmpBulkSession()(implicit hc: HeaderCarrier): Future[Option[GmpBulkSession]] =
    gmpBulkSessionService.resetGmpBulkSession()

  def cacheCallBackData(_callBackData: Option[UploadStatus])(implicit hc: HeaderCarrier): Future[Option[GmpBulkSession]] =
    gmpBulkSessionService.cacheCallBackData(_callBackData)

  def cacheEmailAndReference(_email: Option[String], _reference: Option[String])(implicit hc: HeaderCarrier): Future[Option[GmpBulkSession]] =
    gmpBulkSessionService.cacheEmailAndReference(_email, _reference)

  def fetchGmpSession()(implicit hc: HeaderCarrier): Future[Option[GmpSession]] =
    singleCalculationSessionService.fetchGmpSession()

  def resetGmpSession()(implicit hc: HeaderCarrier): Future[Option[GmpSession]] =
    singleCalculationSessionService.resetGmpSession()

  def resetGmpSessionWithScon()(implicit hc: HeaderCarrier): Future[Option[GmpSession]] =
    singleCalculationSessionService.resetGmpSessionWithScon()

  def cacheMemberDetails(memberDetails: MemberDetails)(implicit hc: HeaderCarrier): Future[Option[GmpSession]] =
    singleCalculationSessionService.cacheMemberDetails(memberDetails)

  def fetchMemberDetails()(implicit hc: HeaderCarrier): Future[Option[MemberDetails]] =
    singleCalculationSessionService.fetchMemberDetails()

  def cachePensionDetails(scon: String)(implicit hc: HeaderCarrier): Future[Option[GmpSession]] =
    singleCalculationSessionService.cachePensionDetails(scon)

  def fetchPensionDetails()(implicit hc: HeaderCarrier): Future[Option[String]] =
    singleCalculationSessionService.fetchPensionDetails()

  def cacheScenario(scenario: String)(implicit hc: HeaderCarrier): Future[Option[GmpSession]] =
    singleCalculationSessionService.cacheScenario(scenario)

  def fetchScenario()(implicit hc: HeaderCarrier): Future[Option[String]] =
    singleCalculationSessionService.fetchScenario()

  def cacheEqualise(_equalise: Option[Int])(implicit hc: HeaderCarrier): Future[Option[GmpSession]] =
    singleCalculationSessionService.cacheEqualise(_equalise)

  def cacheRevaluationDate(date: Option[GmpDate])(implicit hc: HeaderCarrier): Future[Option[GmpSession]] =
    singleCalculationSessionService.cacheRevaluationDate(date)

  def cacheLeaving(leaving: Leaving)(implicit hc: HeaderCarrier): Future[Option[GmpSession]] =
    singleCalculationSessionService.cacheLeaving(leaving)

  def fetchLeaving()(implicit hc: HeaderCarrier): Future[Option[Leaving]] =
    singleCalculationSessionService.fetchLeaving()

  def cacheRevaluationRate(rate: String)(implicit hc: HeaderCarrier): Future[Option[GmpSession]] =
    singleCalculationSessionService.cacheRevaluationRate(rate)

  def createCallbackRecord(implicit hc: HeaderCarrier): Future[Any] =
    gmpBulkSessionService.createCallbackRecord

  def updateCallbackRecord(sessionId: String, uploadStatus: UploadStatus)(implicit hc: HeaderCarrier): Future[Any] =
    gmpBulkSessionService.updateCallbackRecord(uploadStatus)

  def getCallbackRecord(implicit hc: HeaderCarrier): Future[Option[UploadStatus]] =
    gmpBulkSessionService.getCallbackRecord

}
