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

import com.typesafe.config.Config
import net.ceedubs.ficus.Ficus._
import play.api.Mode.Mode
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.mvc.Request
import play.api.{Application, Configuration, Play}
import play.twirl.api.Html
import uk.gov.hmrc.crypto.ApplicationCrypto
import uk.gov.hmrc.play.config.{AppName, ControllerConfig, RunMode}
import uk.gov.hmrc.play.frontend.bootstrap.DefaultFrontendGlobal
import uk.gov.hmrc.play.frontend.filters.{FrontendAuditFilter, FrontendLoggingFilter, MicroserviceFilterSupport}

object ApplicationGlobal extends DefaultFrontendGlobal with RunMode {

  override lazy val auditConnector = Play.current.injector.instanceOf[GmpFrontendAuditConnector]
  implicit lazy val gmpContext = Play.current.injector.instanceOf[GmpContext]
  override val loggingFilter = GmpFrontendLoggingFilter
  override val frontendAuditFilter = GmpFrontendAuditFilter

  override def onStart(app: Application) {
    super.onStart(app)
    new ApplicationCrypto(Play.current.configuration.underlying).verifyConfiguration()
  }

  override def standardErrorTemplate(pageTitle: String, heading: String, message: String)(implicit request: Request[_]): Html =
    views.html.global_error(pageTitle, heading, message)

  override def notFoundTemplate(implicit request: Request[_]): Html = {
    views.html.global_page_not_found(
      "gmp.page_not_found.tabtitle",
      "gmp.page_not_found.heading",
      "gmp.page.not.found.error.check.web.address.correct",
      "gmp.page.not.found.error.check.web.address.full",
      "gmp.page.not.found.error.contact",
      "gmp.page.not.found.error.pension.scheme.url",
      "gmp.page.not.found.error.contact.end",
      "gmp.page.not.found.error.service"
    )
  }
  override def microserviceMetricsConfig(implicit app: Application): Option[Configuration] = app.configuration.getConfig("microservice.metrics")

  override protected def mode: Mode = Play.current.mode

  override protected def runModeConfiguration: Configuration = Play.current.configuration
}

object ControllerConfiguration extends ControllerConfig {
  lazy val controllerConfigs = Play.current.configuration.underlying.as[Config]("controllers")
}

object GmpFrontendLoggingFilter extends FrontendLoggingFilter with MicroserviceFilterSupport {
  override def controllerNeedsLogging(controllerName: String): Boolean = ControllerConfiguration.paramsForController(controllerName).needsLogging
}

object GmpFrontendAuditFilter extends FrontendAuditFilter with RunMode with AppName with MicroserviceFilterSupport {

  override lazy val maskedFormFields = Seq.empty[String]

  override lazy val applicationPort = None

  override lazy val auditConnector = Play.current.injector.instanceOf[GmpFrontendAuditConnector]

  override def controllerNeedsAuditing(controllerName: String): Boolean = ControllerConfiguration.paramsForController(controllerName).needsAuditing

  override protected def mode: Mode = Play.current.mode

  override protected def runModeConfiguration: Configuration = Play.current.configuration

  override protected def appNameConfiguration: Configuration = Play.current.configuration
}
