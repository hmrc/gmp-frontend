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

package connectors

import com.google.inject.{Inject, Singleton}
import metrics.ApplicationMetrics
import models._
import play.api.Mode.Mode
import play.api.{Configuration, Environment, Logger}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.play.config.ServicesConfig

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class GmpConnector @Inject()(environment: Environment,
                             val runModeConfiguration: Configuration,
                             metrics: ApplicationMetrics,
                             httpPost: HttpClient,
                             httpGet: HttpClient,
                             httpPut: HttpClient) extends ServicesConfig {

  override protected def mode: Mode = environment.mode

  lazy val serviceURL = baseUrl("gmp")

  def calculateSingle(calculationRequest: CalculationRequest, link: String)(implicit headerCarrier: HeaderCarrier): Future[CalculationResponse] = {

    val gmpLink = "/gmp/" + link

    val baseURI = "gmp/calculate"
    val calculateUri = s"$serviceURL$gmpLink/$baseURI"

    val timer = metrics.gmpConnectorTimer.time()
    val result = httpPost.POST[CalculationRequest, CalculationResponse](calculateUri, calculationRequest)

    Logger.debug(s"[GmpConnector][calculateSingle][POST] : $calculationRequest")

    result onComplete {
      case _ => timer.stop()
    }

    // $COVERAGE-OFF$
    result onSuccess {
      case response => Logger.debug(s"[GmpConnector][calculateSingle][response] : $response")
    }

    result onFailure {
      case e: Exception => Logger.error(s"[GmpConnector][calculateSingle] ${e.getMessage}", e)
    }
    // $COVERAGE-ON$

    result
  }

  def validateScon(validateSconRequest: ValidateSconRequest, link: String)(implicit headerCarrier: HeaderCarrier): Future[ValidateSconResponse] = {

    val gmpLink = "/gmp/" + link

    val baseURI = "gmp/validateScon"
    val validateSconUri = s"$serviceURL$gmpLink/$baseURI"

    val timer = metrics.gmpConnectorTimer.time()
    val result = httpPost.POST[ValidateSconRequest, ValidateSconResponse](validateSconUri, validateSconRequest)

    Logger.debug(s"[GmpConnector][validateScon][POST] $validateSconRequest")

    result onComplete (_ => timer.stop())

    // $COVERAGE-OFF$
    result onSuccess {
      case response => Logger.debug(s"[GmpConnector][validateScon][response] : $response")
    }

    result onFailure {
      case e: Exception => Logger.error(s"[GmpConnector][validateScon] ${e.getMessage}", e)
    }
    // $COVERAGE-ON$

    result
  }
}
