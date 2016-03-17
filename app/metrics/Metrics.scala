/*
 * Copyright 2016 HM Revenue & Customs
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

import com.codahale.metrics.{Counter, Timer}
import com.kenshoo.play.metrics.MetricsRegistry

trait Metrics {

  val keystoreStoreTimer: Timer
  val keystoreRetrieveTimer: Timer
  val gmpConnectorTimer: Timer
  def countNpsError(code: String) : Unit
  def countNpsSconInvalid() : Unit

}

object Metrics extends Metrics {

  override val keystoreStoreTimer = MetricsRegistry.defaultRegistry.timer("gmp-keystore-storage-timer")
  override val keystoreRetrieveTimer = MetricsRegistry.defaultRegistry.timer("gmp-keystore-retrieve-timer")
  override val gmpConnectorTimer = MetricsRegistry.defaultRegistry.timer("gmp-connector-timer")
  override def countNpsError(code: String) = MetricsRegistry.defaultRegistry.counter(s"gmp-npserror-$code").inc()
  override def countNpsSconInvalid() = MetricsRegistry.defaultRegistry.counter(s"gmp-npssconinvalid").inc()
}
