/*
 * Copyright 2016 HM Revenue & Customs
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
import models.{BulkResultsSummary}
import uk.gov.hmrc.play.http.Upstream4xxResponse


import scala.concurrent.Future

trait BulkResultsController extends GmpController {

  val gmpBulkConnector: GmpBulkConnector

  def get(uploadReference: String) = AuthorisedFor(GmpRegime, pageVisibilityPredicate).async {

    implicit user =>
      implicit request => {
        gmpBulkConnector.getBulkResultsSummary(uploadReference).map{
          bulkResultsSummary => Ok(views.html.bulk_results(bulkResultsSummary, uploadReference))
        }.recover {
          case e: Upstream4xxResponse if e.upstreamResponseCode == FORBIDDEN => {
            Ok(views.html.bulk_wrong_user(request))
          }
        }
      }
  }

  def getResultsAsCsv(uploadReference: String, filter: String) = AuthorisedFor(GmpRegime, pageVisibilityPredicate).async {
    implicit user =>
      implicit request => {
        gmpBulkConnector.getResultsAsCsv(uploadReference, filter).map {
          csv => Ok(csv).as("text/csv")
        }
      }
  }

}


object BulkResultsController extends BulkResultsController {
  val authConnector = GmpFrontendAuthConnector
  val gmpBulkConnector = GmpBulkConnector
}
