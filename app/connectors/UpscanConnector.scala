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

package connectors

import config.ApplicationConfig

import javax.inject.{Inject, Named}
import models.upscan.UpscanInitiateRequest
import models.upscan.{PreparedUpload, UpscanInitiateResponse}
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2

import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue

class UpscanConnector @Inject()(
                                 configuration: ApplicationConfig,
                                 httpClient: HttpClientV2,
                                 @Named("appName") val appName: String
                               )(implicit ec: ExecutionContext) {

  private val upscanInitiateHost: String = configuration.upscanInitiateHost
  private[connectors] val upscanInitiatePath: String = "/upscan/v2/initiate"
  private val upscanInitiateUrl: String = upscanInitiateHost + upscanInitiatePath

  def getUpscanFormData(body: UpscanInitiateRequest)(implicit hc: HeaderCarrier): Future[UpscanInitiateResponse] = {
    httpClient.post(url"$upscanInitiateUrl")
      .withBody(Json.toJson(body))
      .execute[PreparedUpload]
      .map(_.toUpscanInitiateResponse)
  }
}