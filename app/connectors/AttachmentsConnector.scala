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

package connectors

import java.net.URLEncoder

import com.google.inject.Inject
import controllers.routes
import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.{Configuration, Environment, Logger, Mode}
import uk.gov.hmrc.crypto.PlainText
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.play.bootstrap.filters.frontend.crypto.SessionCookieCrypto
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._
import uk.gov.hmrc.play.partials.{HeaderCarrierForPartialsConverter, HtmlPartial}

import scala.concurrent.Future

class UploadConfig @Inject()( environment: Environment,
                              val runModeConfiguration: Configuration,
                              servicesConfig: ServicesConfig,
                              applicationConfig: config.ApplicationConfig)  {

   def mode: Mode = environment.mode

  def apply(implicit request: Request[_], messages: Messages): String = {
    lazy val url = s"${servicesConfig.baseUrl("attachments")}/attachments-internal/uploader"
    val onSuccess = applicationConfig.frontendHost+routes.BulkReferenceController.get()
    val onFailure = applicationConfig.frontendHost+routes.FileUploadController.failure()
    //remove this
    val callback = s"${servicesConfig.baseUrl("gmp-frontend")}${routes.FileUploadController.callback("fewfewfew")}"
    val pageHeadingGA = messages("gmp.fileupload.header")
    Logger.debug(s"[UploadConfig][onSuccessUrl : $onSuccess]")
    s"$url?" +
      s"callbackUrl=${encode(callback)}" +
      s"&onSuccess=${encode(onSuccess)}" +
      s"&onFailure=${encode(onFailure)}" +
      s"&accepts=${encode(".csv")}" +
      s"&collection=${encode("gmp")}" +
      s"&pageHeadingGA=${encode(pageHeadingGA)}"

  }

  private def encode(url: String) = URLEncoder.encode(url, "UTF-8")

}

class AttachmentsConnector @Inject()(
                                      uploadConfig: UploadConfig, sessionCookieCrypto: SessionCookieCrypto,
                                      http: HttpClient,
                                      configuration: Configuration
                                    ) extends HeaderCarrierForPartialsConverter {

  override val crypto: (String) => String = cookie =>
    sessionCookieCrypto.crypto.encrypt(PlainText(cookie)).value

  def getFileUploadPartial()(implicit request: Request[_],messages: Messages): Future[HtmlPartial] = {

    val partial = http.GET[HtmlPartial](uploadConfig(request,messages))

    // $COVERAGE-OFF$
    partial onSuccess {
      case response => Logger.debug(s"[AttachmentsConnector][getFileUploadPartial] $response")
    }

    partial.onFailure {
      case e: Exception => Logger.error(s"[AttachmentsConnector][getFileUploadPartial] Failed to get upload partial", e)
    }
    // $COVERAGE-ON$

    partial
  }
}
