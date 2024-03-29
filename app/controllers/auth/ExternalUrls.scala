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

package controllers.auth

import com.google.inject.{Inject, Singleton}
import play.api.Configuration

@Singleton
class ExternalUrls @Inject()(val runModeConfiguration: Configuration) {

  val basGatewayHost = runModeConfiguration.getOptional[String]("gg-urls.bas-gateway.host").getOrElse("")
  val loginCallback = runModeConfiguration.getOptional[String]("gg-urls.login-callback.url").getOrElse("")
  val signOutCallback = runModeConfiguration.getOptional[String]("gg-urls.signout-callback.url").getOrElse("")
  val loginPath = runModeConfiguration.getOptional[String]("gg-urls.login_path").getOrElse("")
  val signOutPath = runModeConfiguration.getOptional[String]("gg-urls.signout_path").getOrElse("")
  val signIn = s"$basGatewayHost/bas-gateway/$loginPath?continue_url=$loginCallback"
  val signOut = s"$basGatewayHost/bas-gateway/$signOutPath?continue=$signOutCallback"

}
