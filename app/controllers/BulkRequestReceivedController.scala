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
import services.{BulkRequestCreationService, SessionService}

import scala.concurrent.Future

trait BulkRequestReceivedController extends GmpController {

  val sessionService: SessionService
  val bulkRequestCreationService: BulkRequestCreationService
  val gmpBulkConnector: GmpBulkConnector

  def get = AuthorisedFor(GmpRegime, pageVisibilityPredicate).async {
    implicit user =>
      implicit request => {
        sessionService.fetchGmpBulkSession().map{
          case Some(returnedSession) if(returnedSession.callBackData.isDefined) => {
            val callbackData = returnedSession.callBackData.get
            val bulkRequest = bulkRequestCreationService.createBulkRequest(callbackData.collection,callbackData.id,returnedSession.emailAddress.getOrElse(""),
              returnedSession.reference.getOrElse(""))
            gmpBulkConnector.sendBulkRequest(bulkRequest)
          }
          case None => throw new RuntimeException
        }

        Future.successful(Ok(views.html.bulk_request_received()))
      }
  }
}

object BulkRequestReceivedController extends BulkRequestReceivedController {
  val authConnector = GmpFrontendAuthConnector
  override val sessionService = SessionService
  override val bulkRequestCreationService = BulkRequestCreationService
  override val gmpBulkConnector = GmpBulkConnector
}
