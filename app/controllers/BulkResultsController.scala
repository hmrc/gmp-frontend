/*
 * Copyright 2021 HM Revenue & Customs
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

import com.google.inject.Inject
import config.{ApplicationConfig, GmpContext}
import connectors.GmpBulkConnector
import controllers.auth.AuthAction
import play.api.Logger
import play.api.mvc.MessagesControllerComponents
import services.SessionService
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.UpstreamErrorResponse
import views.Views

import scala.concurrent.ExecutionContext

class BulkResultsController @Inject()(authAction: AuthAction,
                                      val authConnector: AuthConnector,
                                      gmpBulkConnector: GmpBulkConnector,
                                      messagesControllerComponents: MessagesControllerComponents,
                                      ac:ApplicationConfig,sessionService: SessionService,implicit val config:GmpContext,
                                      implicit val executionContext: ExecutionContext,
                                      views: Views
                                     ) extends GmpController(messagesControllerComponents,ac,sessionService,config) {
   def get(uploadReference: String, comingFromPage: Int) = authAction.async {
    implicit request => {

      val log = (e: Throwable) => Logger.error(s"[BulkResultsController][GET] ${e.getMessage}", e)

      val link = request.linkId

        gmpBulkConnector.getBulkResultsSummary(uploadReference, link).map {
          bulkResultsSummary => {
            Ok(views.bulkResults(bulkResultsSummary, uploadReference, comingFromPage))
          }
        }.recover {
          case e: UpstreamErrorResponse if e.statusCode == FORBIDDEN => {
            log(e)
            Ok(views.bulkWrongUser())
          }
          case e: UpstreamErrorResponse if e.statusCode == NOT_FOUND => {
            log(e)
            Ok(views.bulkResultsNotFound())
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

  def getResultsAsCsv(uploadReference: String, filter: String) = authAction.async {
      implicit request => {
        val link = request.linkId
        gmpBulkConnector.getResultsAsCsv(uploadReference, filter, link).map {
          csvResponse => Ok(csvResponse.body).as("text/csv").withHeaders(("Content-Disposition", csvResponse.header("Content-Disposition").getOrElse("")))
        }
      }
  }

  def getContributionsAndEarningsAsCsv(uploadReference: String) = authAction.async {
      implicit request => {
        val link = request.linkId
        gmpBulkConnector.getContributionsAndEarningsAsCsv(uploadReference, link).map {
          csvResponse => Ok(csvResponse.body).as("text/csv").withHeaders(("Content-Disposition", csvResponse.header("Content-Disposition").getOrElse("")))
        }
      }
  }
}
