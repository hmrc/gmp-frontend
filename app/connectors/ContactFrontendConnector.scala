/*
 * Copyright 2023 HM Revenue & Customs
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
import play.api.Mode
import play.api.{Configuration, Environment, Logging}
import uk.gov.hmrc.http.{BadGatewayException, HeaderCarrier}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import uk.gov.hmrc.http.HttpClient

@Singleton
class ContactFrontendConnector @Inject()(http: HttpClient,
                                         environment: Environment,
                                         val runModeConfiguration: Configuration,
                                         val servicesConfig: ServicesConfig) extends Logging {

  val mode: Mode = environment.mode

  lazy val serviceBase = s"${servicesConfig.baseUrl("contact-frontend")}/contact"

  def getHelpPartial(implicit hc: HeaderCarrier): Future[String] = {

    val url = s"$serviceBase/problem_reports"

    http.GET(url) map { r =>
      r.body
    } recover {
      case e: BadGatewayException =>
        logger.error(s"[ContactFrontendConnector] ${e.message}", e)
        ""
    }
  }
}
