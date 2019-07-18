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

import com.google.inject.{Inject, Singleton}
import connectors.GmpBulkConnector
import controllers.auth.GmpRegime
import play.api.Logger
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

@Singleton
class DashboardController @Inject()(override val authConnector: AuthConnector,
                                    gmpBulkConnector: GmpBulkConnector) extends GmpPageFlow(authConnector) {

  def get = AuthorisedFor(GmpRegime, pageVisibilityPredicate).async {
    implicit user =>
      implicit request => {

        sessionService.resetGmpSessionWithScon()

        gmpBulkConnector.getPreviousBulkRequests().map {
          bulkPreviousRequests => {
            Ok(views.html.dashboard(bulkPreviousRequests.sorted))
          }
        }.recover {
          case f => {
            Logger.error(s"[DashboardController][getPreviousBulkRequests returned {error : ${f.getMessage}")
            Ok(views.html.dashboard(Nil))
          }
        }
      }
  }

}
