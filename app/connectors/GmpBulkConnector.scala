/*
 * Copyright 2019 HM Revenue & Customs
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

package connectors

import com.google.inject.Inject
import models.{BulkCalculationRequest, BulkPreviousRequest, BulkResultsSummary}
import org.joda.time.LocalDateTime
import play.api.Mode.Mode
import play.api.{Configuration, Environment, Logger}
import uk.gov.hmrc.http._
import uk.gov.hmrc.play.config.ServicesConfig

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class GmpBulkConnector @Inject()(environment: Environment,
                                 val runModeConfiguration: Configuration,
                                 httpGet: HttpGet,
                                 httpPost: HttpPost) extends ServicesConfig {

  override protected def mode: Mode = environment.mode

  lazy val serviceURL = baseUrl("gmp-bulk")

//  def getUser(user: AuthContext): String = {
//    user.principal.accounts.psa.map(_.link).getOrElse(
//      user.principal.accounts.psp.map(_.link).getOrElse(
//        throw new RuntimeException("User Authorisation failed"))).substring(5)
//  }

  def sendBulkRequest(bcr: BulkCalculationRequest, link: String)(implicit headerCarrier: HeaderCarrier): Future[Int] = {

    val baseURI = s"gmp/${link}/gmp/bulk-data"
    val bulkUri = s"$serviceURL/$baseURI/"
    val result = httpPost.POST[BulkCalculationRequest, HttpResponse](bulkUri,bcr.copy(timestamp = LocalDateTime.now(),userId = link))

    Logger.debug(s"[GmpBulkConnector][sendBulkRequest][POST] size : ${bcr.calculationRequests.size}")

    result.map { x =>
      Logger.debug(s"[GmpBulkConnector][sendBulkRequest][success] : $x")
        x.status
    } recover {
      case conflict: Upstream4xxResponse if conflict.upstreamResponseCode == play.api.http.Status.CONFLICT =>
        Logger.warn(s"[GmpBulkConnector][sendBulkRequest] Conflict")
        conflict.upstreamResponseCode

      case large_file: Upstream4xxResponse if large_file.upstreamResponseCode == play.api.http.Status.REQUEST_ENTITY_TOO_LARGE =>
        Logger.warn(s"[GmpBulkConnector][sendBulkRequest] File too large")
        large_file.upstreamResponseCode

      case e: Throwable => Logger.error(s"[GmpBulkConnector][sendBulkRequest] ${e.getMessage}", e)
        500
    }
  }

  def getPreviousBulkRequests(link: String)(implicit headerCarrier: HeaderCarrier): Future[List[BulkPreviousRequest]] = {

    val baseURI = s"gmp/${link}/gmp/retrieve-previous-requests"
    val bulkUri = s"$serviceURL/$baseURI"
    val result = httpGet.GET[List[BulkPreviousRequest]](bulkUri)

    Logger.debug(s"[GmpBulkConnector][getPreviousBulkRequests][GET]")

    // $COVERAGE-OFF$
    result onSuccess {
      case response => Logger.debug(s"[GmpBulkConnector][getPreviousBulkRequests][response] : $response")
    }

    result onFailure {
      case e: Exception => Logger.error(s"[GmpBulkConnector][getPreviousBulkRequests] ${e.getMessage}", e)
    }
    // $COVERAGE-ON$

    result

  }

  def getBulkResultsSummary(uploadReference: String, link: String)(implicit headerCarrier: HeaderCarrier): Future[BulkResultsSummary] = {

    val baseURI = s"gmp/${link}/gmp/get-results-summary"
    val bulkUri = s"$serviceURL/$baseURI/$uploadReference"

    val result = httpGet.GET[BulkResultsSummary](bulkUri)

    Logger.debug(s"[GmpBulkConnector][getBulkResultsSummary][GET] reference : $uploadReference")

    // $COVERAGE-OFF$
    result onSuccess {
      case response => Logger.debug(s"[GmpBulkConnector][getBulkResultsSummary][response] : $response")
    }

    result onFailure {
      case e: Exception => Logger.error(s"[GmpBulkConnector][getBulkResultsSummary] ${e.getMessage}", e)
    }
    // $COVERAGE-ON$

    result
  }

  def getResultsAsCsv(uploadReference: String, filter: String, link: String)(implicit headerCarrier: HeaderCarrier): Future[HttpResponse] = {

    val baseURI = s"gmp/${link}/gmp/get-results-as-csv"
    val bulkUri = s"$serviceURL/$baseURI/$uploadReference/$filter"
    val result = httpGet.GET(bulkUri)

    Logger.debug(s"[GmpBulkConnector][getResultsAsCsv][GET] reference : $uploadReference")

    // $COVERAGE-OFF$
    result onSuccess {
      case response => Logger.debug(s"[GmpBulkConnector][getResultsAsCsv][response] : $response")
    }

    result onFailure {
      case e: Exception => Logger.error(s"[GmpBulkConnector][getResultsAsCsv] ${e.getMessage}", e)
    }
    // $COVERAGE-ON$

    result.map {
      response => response
    }
  }

  def getContributionsAndEarningsAsCsv(uploadReference: String, link: String)(implicit headerCarrier: HeaderCarrier): Future[HttpResponse] = {

    val baseURI = s"gmp/${link}/gmp/get-contributions-as-csv"
    val bulkUri = s"$serviceURL/$baseURI/$uploadReference"
    val result = httpGet.GET(bulkUri)

    Logger.debug(s"[GmpBulkConnector][getContributionsAndEarningsAsCsv][GET] reference : $uploadReference")

    // $COVERAGE-OFF$
    result onSuccess {
      case response => Logger.debug(s"[GmpBulkConnector][getContributionsAndEarningsAsCsv][response] : $response")
    }

    result onFailure {
      case e: Exception => Logger.error(s"[GmpBulkConnector][getContributionsAndEarningsAsCsv] ${e.getMessage}", e)
    }
    // $COVERAGE-ON$

    result.map {
      response => response
    }
  }

}
