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

import com.google.inject.{Inject, Singleton}
import config.{ApplicationConfig, GmpContext, GmpSessionCache}
import connectors.GmpBulkConnector
import controllers.auth.AuthAction
import models.upscan.UploadedSuccessfully
import play.api.Logger
import play.api.i18n.Messages
import play.api.mvc.MessagesControllerComponents
import services.{BulkRequestCreationService, DataLimitExceededException, SessionService}
import uk.gov.hmrc.auth.core.AuthConnector
import views.Views

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BulkRequestReceivedController @Inject()(authAction: AuthAction,
                                              val authConnector: AuthConnector,
                                              sessionService: SessionService,
                                              bulkRequestCreationService: BulkRequestCreationService,
                                              gmpBulkConnector: GmpBulkConnector, ac:ApplicationConfig,
                                              implicit val config:GmpContext,
                                              messagesControllerComponents: MessagesControllerComponents,
                                              implicit val executionContext: ExecutionContext,
                                              implicit val sessionCache:GmpSessionCache,
                                              views: Views
                                             ) extends GmpController(messagesControllerComponents,ac,sessionService,config) {



  def get = authAction.async {
      implicit request => {
        val link = request.linkId
        Logger.debug(s"[BulkRequestReceivedController][get][GET] : ${request.body}")
        sessionService.fetchGmpBulkSession().flatMap {
          case Some(session) if session.callBackData.isDefined && session.callBackData.get.isInstanceOf[UploadedSuccessfully] =>
            val callbackData = session.callBackData.get.asInstanceOf[UploadedSuccessfully]
            val errorPageForToMuchData = Ok(views.failure(Messages("gmp.bulk.failure.too_large"), Messages("gmp.bulk.file_too_large.header"), Messages("gmp.bulk_failure_file_too_large.title")))
            bulkRequestCreationService.createBulkRequest(callbackData, session.emailAddress.getOrElse(""), session.reference.getOrElse("")) match {

              case Right(bulkRequest) => gmpBulkConnector.sendBulkRequest(bulkRequest, link).map {
                  case OK => Ok(views.bulkRequestReceived(bulkRequest.reference))
                  case CONFLICT => Ok(views.failure(Messages("gmp.bulk.failure.duplicate_upload"), Messages("gmp.bulk.problem.header"), Messages("gmp.bulk_failure_duplicate.title")))
                  case REQUEST_ENTITY_TOO_LARGE => errorPageForToMuchData
                  case _ => Ok(views.failure(Messages("gmp.bulk.failure.generic"), Messages("gmp.bulk.problem.header"), Messages("gmp.bulk_failure_generic.title")))
                }

              case Left(DataLimitExceededException) => Future.successful(errorPageForToMuchData)

              case Left(_) => Future.successful((Redirect(controllers.routes.IncorrectlyEncodedController.get())))
            }

          case _ => Future.successful(Ok(views.failure(Messages("gmp.error.session_parts_missing", "/guaranteed-minimum-pension/upload-csv"), Messages("gmp.cannot_calculate.gmp"), Messages("gmp.session_missing.title"))))
        }
      }
  }
}

