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

package config

import com.google.inject.{Inject, Singleton}
import com.typesafe.config.ConfigFactory
import play.api.{Configuration, Environment}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

@Singleton
class ApplicationConfig @Inject()(
  val runModeConfiguration: Configuration,
  val environment: Environment,
  servicesConfig: ServicesConfig) {

  private def loadConfig(key: String) = runModeConfiguration.getOptional[String](key).getOrElse(throw new Exception(s"Missing key: $key"))

  val optimizelyProjectId: String = loadConfig("optimizely.projectId")

  val contactHost = runModeConfiguration.getOptional[String](s"contact-frontend.host").getOrElse("")

  val reportAProblemPartialUrl: String =
    s"$contactHost/contact/problem_reports_ajax?service=$contactFormServiceIdentifier"
  val reportAProblemNonJSUrl: String =
    s"$contactHost/contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"

  val globalErrors = ConfigFactory.load("global-errors.properties")
  lazy val contactFormServiceIdentifier = "GMP"

  val upscanInitiateHost: String = servicesConfig.baseUrl("upscan")
  val upscanProtocol: String = servicesConfig.getConfString("upscan.protocol", "https")
  val upscanRedirectBase: String = runModeConfiguration.get[String]("microservice.services.upscan.redirect-base")

  lazy val timeout = servicesConfig.getInt("timeout.seconds")
  lazy val timeoutCountdown = servicesConfig.getInt("timeout.countdown")
}




