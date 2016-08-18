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
import play.api.Logger
import play.api.i18n.Messages
import services.{BulkRequestCreationService, DataLimitExceededException, SessionService}

import scala.concurrent.Future


trait BulkRequestReceivedController extends GmpController {

  val sessionService: SessionService
  val bulkRequestCreationService: BulkRequestCreationService
  val gmpBulkConnector: GmpBulkConnector

  def get = AuthorisedFor(GmpRegime, pageVisibilityPredicate).async {
    implicit user =>
      implicit request => {
        Logger.debug(s"[BulkRequestReceivedController][get][GET] : ${request.body}")
        sessionService.fetchGmpBulkSession().flatMap {
          case Some(session) if (session.callBackData.isDefined) => {
            val callbackData = session.callBackData.get

              bulkRequestCreationService.createBulkRequest(callbackData.collection, callbackData.id, session.emailAddress.getOrElse(""),
                session.reference.getOrElse("")) match {
                case Left(bulkRequest) =>
                  gmpBulkConnector.sendBulkRequest(bulkRequest).map {
                    case OK => Ok(views.html.bulk_request_received(bulkRequest.reference))
                    case CONFLICT => Ok(views.html.failure(Messages("gmp.bulk.failure.duplicate_upload"), Messages("gmp.bulk.problem.header"), Messages("gmp.bulk_failure_duplicate.title")))
                    case REQUEST_ENTITY_TOO_LARGE => Ok(views.html.failure(Messages("gmp.bulk.failure.too_large"), Messages("gmp.bulk.file_too_large.header"), Messages("gmp.bulk_failure_file_too_large.title")))
                    case _ => Ok(views.html.failure(Messages("gmp.bulk.failure.generic"), Messages("gmp.bulk.problem.header"), Messages("gmp.bulk_failure_generic.title")))
                  }
                case Right(e: DataLimitExceededException) => Future.successful(Ok(views.html.failure(Messages("gmp.bulk.failure.too_large"), Messages("gmp.bulk.file_too_large.header"), Messages("gmp.bulk_failure_file_too_large.title"))))
              }

          }
          case _ => Future.successful(Ok(views.html.failure(Messages("gmp.error.session_parts_missing", "/guaranteed-minimum-pension/upload-csv"), Messages("gmp.cannot_calculate.gmp"), Messages("gmp.session_missing.title"))))
        }
      }
  }
}

object BulkRequestReceivedController extends BulkRequestReceivedController {
  val authConnector = GmpFrontendAuthConnector
  override val sessionService = SessionService
  override val bulkRequestCreationService = BulkRequestCreationService
  override val gmpBulkConnector = GmpBulkConnector
}
