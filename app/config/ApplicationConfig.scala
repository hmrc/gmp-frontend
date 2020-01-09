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

package config

import com.google.inject.{Inject, Singleton}
import com.typesafe.config.ConfigFactory
import play.api.Mode.Mode
import play.api.{Configuration, Environment, Play}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

@Singleton
class ApplicationConfig @Inject()(
  val runModeConfiguration: Configuration,
  val environment: Environment,
  servicesConfig: ServicesConfig) {


  private def loadConfig(key: String) = runModeConfiguration.getString(key).getOrElse(throw new Exception(s"Missing key: $key"))

   val assetsPrefix: String = loadConfig("assets.url") + loadConfig("assets.version")
   val analyticsToken: Option[String] = runModeConfiguration.getString("google-analytics.token")
   val analyticsHost: String = runModeConfiguration.getString("google-analytics.host").getOrElse("auto")
   val urBannerToggle:Boolean = loadConfig("urBanner.toggle").toBoolean
   val urBannerLink: String = loadConfig("urBanner.link")
   val optimizelyProjectId: String = loadConfig("optimizely.projectId")

  val globalErrors = ConfigFactory.load("global-errors.properties")
  val contactFormServiceIdentifier = "GMP"
  val frontendHost = loadConfig("platform.frontend.host")

   protected def mode: Mode = Play.current.mode

}



