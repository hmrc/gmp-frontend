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

package services

import com.google.inject.Inject
import config.ApplicationConfig
import connectors.UpscanConnector
import models.upscan.{UpscanInitiateRequest, UpscanInitiateResponse}
import play.api.mvc.Request
import uk.gov.hmrc.http.HeaderCarrier
import scala.concurrent.Future

class UpscanService @Inject()(
                               applicationConfig: ApplicationConfig,
                               upscanConnector: UpscanConnector
                             ) {

  lazy val redirectUrlBase: String = applicationConfig.upscanRedirectBase

  def getUpscanFormData()(implicit hc: HeaderCarrier, request: Request[_]): Future[UpscanInitiateResponse] = {
    val callback = controllers.routes.FileUploadController.callback(hc.sessionId.get.value)
      .absoluteURL(applicationConfig.upscanProtocol == "https")

    val success = s"$redirectUrlBase/guaranteed-minimum-pension/upload-csv/success"
    val failure = s"$redirectUrlBase/guaranteed-minimum-pension/upload-csv/failure"

    val upscanInitiateRequest = UpscanInitiateRequest(callback, success, failure)
    upscanConnector.getUpscanFormData(upscanInitiateRequest)
  }

}
