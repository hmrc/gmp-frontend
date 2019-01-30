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

package config

import com.typesafe.config.ConfigFactory
import play.api.{Configuration, Play}
import play.api.Mode.Mode
import play.api.Play._
import uk.gov.hmrc.play.config.ServicesConfig

trait ApplicationConfig {

  val assetsPrefix: String
  val analyticsToken: Option[String]
  val analyticsHost: String
  val frontendHost: String
  val urBannerToggle: Boolean
  val urBannerLink: String

}

object ApplicationConfig extends ApplicationConfig with ServicesConfig {

  override lazy val frontendHost = loadConfig("platform.frontend.host")

  private def loadConfig(key: String) = configuration.getString(key).getOrElse(throw new Exception(s"Missing key: $key"))

  override lazy val assetsPrefix: String = loadConfig("assets.url") + loadConfig("assets.version")
  override lazy val analyticsToken: Option[String] = configuration.getString("google-analytics.token")
  override lazy val analyticsHost: String = configuration.getString("google-analytics.host").getOrElse("auto")
  override lazy val urBannerToggle:Boolean = loadConfig("urBanner.toggle").toBoolean
  override lazy val urBannerLink: String = loadConfig("urBanner.link")

  val globalErrors = ConfigFactory.load("global-errors.properties")
  val contactFormServiceIdentifier = "GMP"

  override protected def mode: Mode = Play.current.mode
  override protected def runModeConfiguration: Configuration = Play.current.configuration
}
