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


trait AppConfig {
  val isMongoDBCacheEnabled: Boolean

  def serviceMaxNoOfAttempts: Int
}

@Singleton
class ApplicationConfig @Inject()(
  val runModeConfiguration: Configuration,
  val environment: Environment,
  servicesConfig: ServicesConfig) extends ServicesConfig(runModeConfiguration) with AppConfig {

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
  lazy val cacheTtl: Int = servicesConfig.getInt("mongodb.timeToLiveInSeconds")

  // Fetch boolean configuration with a default value
  override val isMongoDBCacheEnabled: Boolean = runModeConfiguration.getOptional[Boolean]("isMongoDBCacheEnabled").getOrElse(false)
  override def serviceMaxNoOfAttempts: Int = {
    runModeConfiguration.getOptional[String]("service.maxAttempts") match {
      case Some(value) if value.matches("^\\d+$") =>
        value.toInt
      case Some(invalidValue) =>
        throw new IllegalArgumentException(s"Invalid configuration for 'service.maxAttempts': $invalidValue. Must be an integer.")
      case None =>
        3
    }
  }
}




