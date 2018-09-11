/*
 * Copyright 2018 HM Revenue & Customs
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

import javax.inject.Inject
import com.typesafe.config.Config
import config.{ApplicationConfig, WSHttp}
import controllers.routes
import play.api.Logger
import play.api.i18n.Messages
import play.api.mvc.Request
import uk.gov.hmrc.http.hooks.HttpHook
import uk.gov.hmrc.http.{HeaderCarrier, HttpGet, HttpResponse}
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.frontend.filters.SessionCookieCryptoFilter
import uk.gov.hmrc.play.http.ws.WSGet
import uk.gov.hmrc.play.partials.{HeaderCarrierForPartialsConverter, HtmlPartial}
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._
import play.api.i18n.{ I18nSupport, Messages, MessagesApi }
import play.api.i18n.Messages.Implicits._
import scala.concurrent.Future
import play.api.Play.current

trait UploadConfig  extends ServicesConfig {

  def apply(implicit request: Request[_]): String = {
    lazy val url = s"${baseUrl("attachments")}/attachments-internal/uploader"
    val onSuccess = ApplicationConfig.frontendHost+routes.BulkReferenceController.get()
    val onFailure = ApplicationConfig.frontendHost+routes.FileUploadController.failure()
    val callback = s"${baseUrl("gmp-frontend")}${routes.FileUploadController.callback()}"
    val pageHeadingGA = Messages("gmp.fileupload.header")

    Logger.debug(s"[UploadConfig][onSuccessUrl : $onSuccess]")
    s"$url?" +
      s"callbackUrl=${encode(callback)}" +
      s"&onSuccess=${encode(onSuccess)}" +
      s"&onFailure=${encode(onFailure)}" +
      s"&accepts=${encode(".csv")}" +
      s"&collection=${encode("gmp")}" +
      s"&pageHeadingGA=${encode(pageHeadingGA)}" + {
    }

  }

  private def encode(url: String) = URLEncoder.encode(url, "UTF-8")

}

object UploadConfig extends UploadConfig


trait AttachmentsConnector extends HeaderCarrierForPartialsConverter {

  val http: HttpGet = WSHttp

  def getFileUploadPartial()(implicit request: Request[_]): Future[HtmlPartial] = {
Logger.debug("test config...!!!!!!!!!"+UploadConfig(request).toString)
    println("test config...!!!!!!!!!"+UploadConfig(request).toString)
    val partial = http.GET[HtmlPartial](UploadConfig(request))

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

object AttachmentsConnector extends AttachmentsConnector{
  // $COVERAGE-OFF$Trivial and never going to be called by a test that uses it's own object implementation
  override val crypto = SessionCookieCryptoFilter.encrypt _
  // $COVERAGE-ON$
}
