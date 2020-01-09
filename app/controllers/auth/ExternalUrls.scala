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

package controllers.auth

import com.google.inject.{Inject, Singleton}
import play.api.Mode
import play.api.{Configuration, Play}

// TODO tidy/remove this
@Singleton
class BaseExternalUrls @Inject()(val runModeConfiguration: Configuration) {

  val companyAuthHost = runModeConfiguration.getString("gg-urls.company-auth.host").getOrElse("")
  val loginCallback = runModeConfiguration.getString("gg-urls.login-callback.url").getOrElse("")
  val signOutCallback = runModeConfiguration.getString("gg-urls.signout-callback.url").getOrElse("")
  val loginPath = runModeConfiguration.getString("gg-urls.login_path").getOrElse("")
  val signOutPath = runModeConfiguration.getString("gg-urls.signout_path").getOrElse("")
  val signIn = s"$companyAuthHost/gg/$loginPath?continue=$loginCallback"
  val signOut = s"$companyAuthHost/gg/$signOutPath?continue=$signOutCallback"

   def mode: Mode = Play.current.mode

}

case object ExternalUrls extends BaseExternalUrls(Play.current.configuration)