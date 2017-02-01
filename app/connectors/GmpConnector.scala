/*
 * Copyright 2017 HM Revenue & Customs
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

import config.{ApplicationConfig, WSHttp}
import metrics.Metrics
import models._
import play.api.Logger
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.frontend.auth.AuthContext
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpGet, HttpPost, HttpPut}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait GmpConnector extends ServicesConfig {
  val httpPost: HttpPost = WSHttp
  val httpGet: HttpGet = WSHttp
  val httpPut: HttpPut = WSHttp
  val applicationConfig: ApplicationConfig

  def metrics: Metrics

  lazy val serviceURL = baseUrl("gmp")

  def getUser(user: AuthContext): String = {
    user.principal.accounts.psa.map(_.link).getOrElse(
      user.principal.accounts.psp.map(_.link).getOrElse(
        throw new RuntimeException("User Authorisation failed"))).substring(5)
  }

  def calculateSingle(calculationRequest: CalculationRequest)(implicit user: AuthContext, headerCarrier: HeaderCarrier): Future[CalculationResponse] = {

    val gmpLink = "/gmp/" + getUser(user)

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

  def validateScon(validateSconRequest: ValidateSconRequest)(implicit user: AuthContext, headerCarrier: HeaderCarrier): Future[ValidateSconResponse] = {

    val gmpLink = "/gmp/" + getUser(user)

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

object GmpConnector extends GmpConnector {
  // $COVERAGE-OFF$ Trivial and never going to be called by a test that uses it's own object implementation
  override def metrics = Metrics

  override val applicationConfig: ApplicationConfig = ApplicationConfig
  // $COVERAGE-ON$

}
