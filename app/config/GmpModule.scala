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

import connectors.{GmpBulkConnector, GmpConnector}
import controllers.auth.UUIDGenerator
import metrics.Metrics
import play.api.inject.{Binding, Module}
import play.api.{Configuration, Environment}
import uk.gov.hmrc.http.{HttpGet, HttpPost, HttpPut}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

class GmpModule extends Module{
  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = Seq(
    bind[AuditConnector].to(GmpFrontendAuditConnector),
    bind[UUIDGenerator].to(UUIDGenerator),
    bind[Metrics].to(Metrics),
    bind[HttpGet].to(WSHttp),
    bind[HttpPost].to(WSHttp),
    bind[HttpPut].to(WSHttp)
  )
}
