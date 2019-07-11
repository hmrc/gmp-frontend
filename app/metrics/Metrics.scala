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

package metrics

import com.codahale.metrics.{MetricRegistry, Timer}
import uk.gov.hmrc.play.graphite.MicroserviceMetrics
trait Metrics {

  val keystoreStoreTimer: Timer
  val keystoreRetrieveTimer: Timer
  val gmpConnectorTimer: Timer
  def countNpsError(code: String) : Unit
  def countNpsSconInvalid() : Unit

}

object Metrics extends Metrics with MicroserviceMetrics{
  lazy val registry: MetricRegistry = metrics.defaultRegistry

  override val keystoreStoreTimer = registry.timer("gmp-keystore-storage-timer")
  override val keystoreRetrieveTimer = registry.timer("gmp-keystore-retrieve-timer")
  override val gmpConnectorTimer = registry.timer("gmp-connector-timer")
  override def countNpsError(code: String) = registry.counter(s"gmp-npserror-$code").inc()
  override def countNpsSconInvalid() = registry.counter(s"gmp-npssconinvalid").inc()
}
