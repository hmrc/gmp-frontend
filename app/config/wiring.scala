/*
 * Copyright 2017 HM Revenue & Customs
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

import uk.gov.hmrc.http.cache.client.SessionCache
import uk.gov.hmrc.play.audit.http.HttpAuditing
import uk.gov.hmrc.play.audit.http.config.LoadAuditingConfig
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.config.{AppName, RunMode, ServicesConfig}
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.http.ws.{WSDelete, WSGet, WSPost, WSPut}

object GmpFrontendAuditConnector extends AuditConnector with AppName with RunMode {
  override lazy val auditingConfig = LoadAuditingConfig(s"auditing")
}

object WSHttp extends WSGet with WSPut with WSPost with WSDelete with AppName with RunMode with HttpAuditing {
  //No implicit auditing
  //override val hooks = NoneRequired

  //implicit auditing
  override val hooks = Seq(AuditingHook)
  override val auditConnector = GmpFrontendAuditConnector
}

object GmpFrontendAuthConnector extends AuthConnector with ServicesConfig {
  val serviceUrl = baseUrl("auth")
  lazy val http = WSHttp
}

object GmpSessionCache extends SessionCache with AppName with ServicesConfig {
  override lazy val http = WSHttp
  override lazy val defaultSource = appName
  override lazy val baseUri = baseUrl("keystore")
  override lazy val domain = getConfString("cachable.session-cache.domain", throw new Exception(s"Could not find config 'cachable.session-cache.domain'"))
}
