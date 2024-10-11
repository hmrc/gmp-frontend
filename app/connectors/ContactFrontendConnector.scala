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

import com.google.inject.{Inject, Singleton}
import play.api.Mode
import play.api.{Configuration, Environment, Logging}
import uk.gov.hmrc.http.{BadGatewayException, HeaderCarrier, HttpResponse, StringContextOps}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw

import scala.concurrent.{ExecutionContext, Future}
import uk.gov.hmrc.http.client.HttpClientV2

@Singleton
class ContactFrontendConnector @Inject()(http: HttpClientV2,
                                         environment: Environment,
                                         val runModeConfiguration: Configuration,
                                         val servicesConfig: ServicesConfig)(implicit ec: ExecutionContext) extends Logging {

  val mode: Mode = environment.mode

  lazy val serviceBase = s"${servicesConfig.baseUrl("contact-frontend")}/contact"

  def getHelpPartial(implicit hc: HeaderCarrier): Future[String] = {

    val url = s"$serviceBase/problem_reports"

    http.get(url"$url")
      .execute[HttpResponse]
      .map(_.body)
      .recover {
        case e: BadGatewayException =>
          logger.error(s"[ContactFrontendConnector] ${e.message}", e)
          ""
      }
  }
}
