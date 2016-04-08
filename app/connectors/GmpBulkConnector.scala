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

package connectors

import config.WSHttp
import models.{ValidateSconResponse, ValidateSconRequest, CalculationResponse, BulkCalculationRequest}
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.frontend.auth.AuthContext
import uk.gov.hmrc.play.http.{HttpPost, HttpResponse, HeaderCarrier}

import scala.concurrent.Future

trait GmpBulkConnector extends ServicesConfig{

  val httpPost: HttpPost = WSHttp
  lazy val serviceURL = baseUrl("gmp-bulk")


  def sendBulkRequest(bcr: BulkCalculationRequest)(implicit headerCarrier: HeaderCarrier): Future[HttpResponse] = {

    val baseURI = "gmp/bulk-data"
    val bulkUri = s"$serviceURL/$baseURI"
    val result = httpPost.POST[BulkCalculationRequest, HttpResponse](bulkUri,bcr)
    result
  }

}

object GmpBulkConnector extends GmpBulkConnector
