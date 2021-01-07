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

import com.google.inject.name.Named
import com.google.inject.{Inject, Singleton}
import play.api.Mode
import play.api.{Configuration, Environment}
import uk.gov.hmrc.http.cache.client.SessionCache
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.http.HttpClient

@Singleton
class GmpSessionCache @Inject()(@Named("appName") appName: String,
                                environment: Environment,
                                configuration: Configuration,
                                val http: HttpClient,
                               val serviceConfig: ServicesConfig)  extends SessionCache{



   val defaultSource = appName
   val baseUri = serviceConfig.baseUrl("keystore")
   val domain = serviceConfig.getConfString("cachable.session-cache.domain", throw new Exception(s"Could not find config 'cachable.session-cache.domain'"))

   def appNameConfiguration: Configuration = configuration
   def mode: Mode = environment.mode
   def runModeConfiguration: Configuration = configuration
}
