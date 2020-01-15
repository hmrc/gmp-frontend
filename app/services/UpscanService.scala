/*
 * Copyright 2020 HM Revenue & Customs
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

package services

import com.google.inject.Inject
import config.ApplicationConfig
import connectors.UpscanConnector
import models.upscan.{UpscanInitiateRequest, UpscanInitiateResponse}
import play.api.{Environment, Mode}
import play.api.mvc.{Call, Request}
import uk.gov.hmrc.http.HeaderCarrier
import scala.concurrent.Future

class UpscanService @Inject()(
                               applicationConfig: ApplicationConfig,
                               upscanConnector: UpscanConnector,
                               environment: Environment
                             ) {

  lazy val redirectUrlBase: String = applicationConfig.upscanRedirectBase
  private implicit def urlToString(c: Call): String = redirectUrlBase + c.url

  def getUpscanFormData()(implicit hc: HeaderCarrier, request: Request[_]): Future[UpscanInitiateResponse] = {
    val callback = controllers.routes.FileUploadController.callback(hc.sessionId.get.value)
      .absoluteURL(environment.mode == Mode.Prod)

    val success = controllers.routes.FileUploadController.showResult()
    val failure = redirectUrlBase + "/guaranteed-minimum-pension/upload-csv/failure"
    val upscanInitiateRequest = UpscanInitiateRequest(callback, success, failure)
    upscanConnector.getUpscanFormData(upscanInitiateRequest)
  }

}
