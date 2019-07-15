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

package controllers.auth

import play.api.Mode.Mode
import play.api.Play.current
import play.api.{Configuration, Play}
import uk.gov.hmrc.play.config.RunMode

object ExternalUrls extends RunMode {
  val companyAuthHost = Play.configuration.getString("gg-urls.company-auth.host").getOrElse("")
  val loginCallback = Play.configuration.getString("gg-urls.login-callback.url").getOrElse("")
  val signOutCallback = Play.configuration.getString("gg-urls.signout-callback.url").getOrElse("")
  val loginPath = Play.configuration.getString("gg-urls.login_path").getOrElse("")
  val signOutPath = Play.configuration.getString("gg-urls.signout_path").getOrElse("")
  val signIn = s"$companyAuthHost/gg/$loginPath?continue=$loginCallback"
  val signOut = s"$companyAuthHost/gg/$signOutPath?continue=$signOutCallback"
  val continue = Play.configuration.getString("gg-urls.continue.url").getOrElse("")

  override protected def mode: Mode = Play.current.mode

  override protected def runModeConfiguration: Configuration = Play.current.configuration
}
