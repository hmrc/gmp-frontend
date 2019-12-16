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

import akka.actor.ActorSystem
import com.google.inject.Inject
import com.typesafe.config.Config
import play.api.Mode.Mode
import play.api.{Configuration, Environment, Play}
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.cache.client.SessionCache
import uk.gov.hmrc.http.hooks.HttpHook
import uk.gov.hmrc.play.audit.http.HttpAuditing
import uk.gov.hmrc.play.audit.http.config.AuditingConfig
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.config.{AppName, RunMode, ServicesConfig}
import uk.gov.hmrc.play.bootstrap.config.LoadAuditingConfig
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.play.http.ws._

class GmpFrontendAuditConnector @Inject()(environment: Environment, configuration: Configuration) extends AuditConnector with AppName with RunMode {
  lazy val auditingConfig: AuditingConfig = LoadAuditingConfig(appNameConfiguration, mode,s"auditing")

  override protected def appNameConfiguration: Configuration = configuration
  override protected def mode: Mode = environment.mode
  override protected def runModeConfiguration: Configuration = configuration
}

trait Hooks extends uk.gov.hmrc.http.hooks.HttpHooks with HttpAuditing {
  override val hooks: Seq[HttpHook] = Seq.empty[HttpHook]
  override lazy val auditConnector: AuditConnector = Play.current.injector.instanceOf[GmpFrontendAuditConnector]
}

trait WSHttp extends HttpGet with WSGet with HttpPut with WSPut with HttpPost with WSPost with HttpDelete with WSDelete with Hooks with AppName {
  override protected def appNameConfiguration: Configuration = Play.current.configuration
  override protected def actorSystem: ActorSystem = akka.actor.ActorSystem()
  override protected def configuration: Option[Config] = Some(Play.current.configuration.underlying)
}

object WSHttp extends WSHttp with Hooks

class GmpSessionCache @Inject()(environment: Environment,
                                configuration: Configuration,
                                val http: HttpClient) extends SessionCache with AppName with ServicesConfig {

  override lazy val defaultSource = appName
  override lazy val baseUri = baseUrl("keystore")
  override lazy val domain = getConfString("cachable.session-cache.domain", throw new Exception(s"Could not find config 'cachable.session-cache.domain'"))

  override protected def appNameConfiguration: Configuration = configuration
  override protected def mode: Mode = environment.mode
  override protected def runModeConfiguration: Configuration = configuration
}
