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

package controllers

import config.GmpFrontendAuthConnector
import connectors.GmpBulkConnector
import controllers.auth.GmpRegime
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

object MoreBulkResultsController extends MoreBulkResultsController{
  val authConnector = GmpFrontendAuthConnector
  val gmpBulkConnector = GmpBulkConnector
}

trait MoreBulkResultsController extends GmpPageFlow {

  val gmpBulkConnector: GmpBulkConnector

  def retrieveMoreBulkResults = AuthorisedFor(GmpRegime, pageVisibilityPredicate).async {
    implicit user =>
      implicit request => {
        gmpBulkConnector.getPreviousBulkRequests().map {
          bulkPreviousRequests => {
            Ok(views.html.more_bulk_results(bulkPreviousRequests.sorted))
          }
        }
      }
  }

}
