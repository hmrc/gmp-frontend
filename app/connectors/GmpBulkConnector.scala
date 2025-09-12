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

package connectors

import com.google.inject.Inject
import models.{BulkCalculationRequest, BulkPreviousRequest, BulkResultsSummary}

import java.time.LocalDateTime
import play.api.Mode
import play.api.libs.json.Json
import play.api.{Configuration, Environment, Logging}
import uk.gov.hmrc.http._
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.HttpReads.Implicits._
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue

import scala.concurrent.{ExecutionContext, Future}

class GmpBulkConnector @Inject()(environment: Environment,
                                 val runModeConfiguration: Configuration,
                                 http: HttpClientV2,
                                 servicesConfig: ServicesConfig)(implicit ec: ExecutionContext) extends Logging {

  def mode: Mode = environment.mode

  lazy val serviceURL = servicesConfig.baseUrl("gmp-bulk")

  def sendBulkRequest(bcr: BulkCalculationRequest, link: String)(implicit headerCarrier: HeaderCarrier): Future[Int] = {

    val baseURI = s"gmp/${link}/gmp/bulk-data"
    val bulkUri = s"$serviceURL/$baseURI/"
    val result = http.post(url"$bulkUri")
      .withBody(Json.toJson(bcr.copy(timestamp = LocalDateTime.now(), userId = link)))
      .execute[HttpResponse]

    logger.debug(s"[GmpBulkConnector][sendBulkRequest][POST] size : ${bcr.calculationRequests.size}")

    result.map { x =>
      logger.debug(s"[GmpBulkConnector][sendBulkRequest][success] : $x")
        x.status
    } recover {
      case conflict: UpstreamErrorResponse if conflict.statusCode == play.api.http.Status.CONFLICT =>
        logger.warn(s"[GmpBulkConnector][sendBulkRequest] Conflict")
        conflict.statusCode

      case large_file: UpstreamErrorResponse if large_file.statusCode == play.api.http.Status.REQUEST_ENTITY_TOO_LARGE =>
        logger.warn(s"[GmpBulkConnector][sendBulkRequest] File too large")
        large_file.statusCode

      case e: Throwable => logger.error(s"[GmpBulkConnector][sendBulkRequest] ${e.getMessage}", e)
        play.api.http.Status.INTERNAL_SERVER_ERROR
    }
  }

  def getPreviousBulkRequests(link: String)(implicit headerCarrier: HeaderCarrier): Future[List[BulkPreviousRequest]] = {

    val baseURI = s"gmp/${link}/gmp/retrieve-previous-requests"
    val bulkUri = s"$serviceURL/$baseURI"
    val result = http.get(url"$bulkUri")
      .execute[List[BulkPreviousRequest]]

    logger.debug(s"[GmpBulkConnector][getPreviousBulkRequests][GET]")

    // $COVERAGE-OFF$
    result onComplete {
      case response => logger.debug(s"[GmpBulkConnector][getPreviousBulkRequests][response] : $response")
    }

    result.failed.foreach {
      case e: Exception => logger.error(s"[GmpBulkConnector][getPreviousBulkRequests] ${e.getMessage}", e)
      case t: Throwable => logger.error(s"[GmpBulkConnector][getPreviousBulkRequests] ${t.getMessage}", t)
    }
    // $COVERAGE-ON$

    result

  }

  def getBulkResultsSummary(uploadReference: String, link: String)(implicit headerCarrier: HeaderCarrier): Future[BulkResultsSummary] = {

    val baseURI = s"gmp/${link}/gmp/get-results-summary"
    val bulkUri = s"$serviceURL/$baseURI/$uploadReference"

    val result = http.get(url"$bulkUri")
      .execute[BulkResultsSummary]

    logger.debug(s"[GmpBulkConnector][getBulkResultsSummary][GET] reference : $uploadReference")

    // $COVERAGE-OFF$
    result onComplete {
      case response => logger.debug(s"[GmpBulkConnector][getBulkResultsSummary][response] : $response")
    }

    result.failed.foreach {
      case e: Exception => logger.error(s"[GmpBulkConnector][getBulkResultsSummary] ${e.getMessage}", e)
      case t: Throwable => logger.error(s"[GmpBulkConnector][getBulkResultsSummary] ${t.getMessage}", t)
    }
    // $COVERAGE-ON$

    result
  }

  def getResultsAsCsv(uploadReference: String, filter: String, link: String)(implicit headerCarrier: HeaderCarrier): Future[HttpResponse] = {

    val baseURI = s"gmp/${link}/gmp/get-results-as-csv"
    val bulkUri = s"$serviceURL/$baseURI/$uploadReference/$filter"
    val result = http.get(url"$bulkUri")
      .execute[HttpResponse]

    logger.debug(s"[GmpBulkConnector][getResultsAsCsv][GET] reference : $uploadReference")

    // $COVERAGE-OFF$
    result onComplete {
      case response => logger.debug(s"[GmpBulkConnector][getResultsAsCsv][response] : $response")
    }

    result.failed.foreach {
      case e: Exception => logger.error(s"[GmpBulkConnector][getResultsAsCsv] ${e.getMessage}", e)
      case t: Throwable => logger.error(s"[GmpBulkConnector][getResultsAsCsv] ${t.getMessage}", t)
    }
    // $COVERAGE-ON$

    result.map {
      response => response
    }
  }

  def getContributionsAndEarningsAsCsv(uploadReference: String, link: String)(implicit headerCarrier: HeaderCarrier): Future[HttpResponse] = {

    val baseURI = s"gmp/${link}/gmp/get-contributions-as-csv"
    val bulkUri = s"$serviceURL/$baseURI/$uploadReference"
    val result = http.get(url"$bulkUri")
      .execute[HttpResponse]

    logger.debug(s"[GmpBulkConnector][getContributionsAndEarningsAsCsv][GET] reference : $uploadReference")

    // $COVERAGE-OFF$
    result onComplete {
      case response => logger.debug(s"[GmpBulkConnector][getContributionsAndEarningsAsCsv][response] : $response")
    }

    result.failed.foreach {
      case e: Exception => logger.error(s"[GmpBulkConnector][getContributionsAndEarningsAsCsv] ${e.getMessage}", e)
      case t: Throwable => logger.error(s"[GmpBulkConnector][getContributionsAndEarningsAsCsv] ${t.getMessage}", t)
    }
    // $COVERAGE-ON$

    result.map {
      response => response
    }
  }

}
