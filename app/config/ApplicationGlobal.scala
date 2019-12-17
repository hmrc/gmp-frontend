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

import akka.stream.Materializer
import com.google.inject.Singleton
import com.typesafe.config.Config
import javax.inject.Inject
import net.ceedubs.ficus.Ficus._
import play.api.Mode.Mode
import play.api.i18n.MessagesApi
import play.api.mvc.Request
import play.api.{Configuration, Play}
import uk.gov.hmrc.play.bootstrap.config.ControllerConfigs
import uk.gov.hmrc.play.bootstrap.filters.DefaultLoggingFilter
import uk.gov.hmrc.play.bootstrap.filters.frontend.FrontendAuditFilter
import uk.gov.hmrc.play.bootstrap.http.FrontendErrorHandler
import uk.gov.hmrc.play.config.{AppName, ControllerConfig, RunMode}
@Singleton
class MyErrorHandler @Inject()(
                                val messagesApi: MessagesApi, val configuration: Configuration,
                                val loggingFilter:GmpFrontendLoggingFilter,
                                val frontendAuditFilter:GmpFrontendAuditFilter,
                                auditConnector:GmpFrontendAuditConnector,
                                val gmpContext:GmpContext
                              ) extends FrontendErrorHandler {


  override def standardErrorTemplate(pageTitle: String, heading: String, message: String)
                                    (implicit request: Request[_]) = ??? // put the code from your old global object here.

  /*override def notFoundTemplate(implicit request: Request[_]): Html = {
    views.html.global_page_not_found()
  }
  override def microserviceMetricsConfig(implicit app: Application): Option[Configuration] = app.configuration.getConfig("microservice.metrics")

  override protected def mode: Mode = Play.current.mode

  override protected def runModeConfiguration: Configuration = Play.current.configuration
}*/
}
object ControllerConfiguration extends ControllerConfig {
  lazy val controllerConfigs = Play.current.configuration.underlying.as[Config]("controllers")
}

class GmpFrontendLoggingFilter @Inject()(config: Configuration)(implicit override val mat: Materializer)
  extends DefaultLoggingFilter(ControllerConfigs.fromConfig(config)) {
  override def controllerNeedsLogging(controllerName: String): Boolean = ControllerConfiguration.paramsForController(controllerName).needsLogging
}

class GmpFrontendAuditFilter @Inject()(config: Configuration)(implicit override val mat: Materializer) extends FrontendAuditFilter with RunMode with AppName {

  override lazy val maskedFormFields = Seq.empty[String]

  override lazy val applicationPort = None

  override lazy val auditConnector = Play.current.injector.instanceOf[GmpFrontendAuditConnector]

  override def controllerNeedsAuditing(controllerName: String): Boolean = ControllerConfiguration.paramsForController(controllerName).needsAuditing

  override protected def mode: Mode = Play.current.mode

  override protected def runModeConfiguration: Configuration = Play.current.configuration

  override protected def appNameConfiguration: Configuration = Play.current.configuration
}
