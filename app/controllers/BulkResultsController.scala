/*
 * Copyright 2018 HM Revenue & Customs
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

package controllers

import config.GmpFrontendAuthConnector
import connectors.GmpBulkConnector
import controllers.auth.GmpRegime
import play.api.Logger
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import uk.gov.hmrc.http.{ NotFoundException, Upstream4xxResponse }

trait BulkResultsController extends GmpController {

  val gmpBulkConnector: GmpBulkConnector

  def get(uploadReference: String, comingFromPage: Int) = AuthorisedFor(GmpRegime, pageVisibilityPredicate).async {

    val log = (e: Throwable) => Logger.error(s"[BulkResultsController][GET] ${e.getMessage}", e)

    implicit user =>
      implicit request => {
        gmpBulkConnector.getBulkResultsSummary(uploadReference).map {
          bulkResultsSummary => {
            Ok(views.html.bulk_results(bulkResultsSummary, uploadReference, comingFromPage))
          }
        }.recover {
          case e: Upstream4xxResponse if e.upstreamResponseCode == FORBIDDEN => {
            log(e)
            Ok(views.html.bulk_wrong_user())
          }
          case e: NotFoundException => {
            log(e)
            Ok(views.html.bulk_results_not_found())
          }
          // $COVERAGE-OFF$
          case e: Exception => {
            log(e)
            throw e
          }
          // $COVERAGE-ON$
        }
      }
  }

  def getResultsAsCsv(uploadReference: String, filter: String) = AuthorisedFor(GmpRegime, pageVisibilityPredicate).async {
    implicit user =>
      implicit request => {
        gmpBulkConnector.getResultsAsCsv(uploadReference, filter).map {
          csvResponse => Ok(csvResponse.body).as("text/csv").withHeaders(("Content-Disposition", csvResponse.header("Content-Disposition").getOrElse("")))
        }
      }
  }


  def getContributionsAndEarningsAsCsv(uploadReference: String) = AuthorisedFor(GmpRegime, pageVisibilityPredicate).async {
    implicit user =>
      implicit request => {
        gmpBulkConnector.getContributionsAndEarningsAsCsv(uploadReference).map {
          csvResponse => Ok(csvResponse.body).as("text/csv").withHeaders(("Content-Disposition", csvResponse.header("Content-Disposition").getOrElse("")))
        }
      }
  }
}


object BulkResultsController extends BulkResultsController {
  val authConnector = GmpFrontendAuthConnector
  val gmpBulkConnector = GmpBulkConnector
}
