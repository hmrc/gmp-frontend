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

package connectors

import java.net.URLEncoder

import config.WSHttp
import controllers.routes
import play.api.Logger
import play.api.mvc.Request
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.frontend.filters.SessionCookieCryptoFilter
import uk.gov.hmrc.play.http._
import uk.gov.hmrc.play.partials.{HeaderCarrierForPartialsConverter, HtmlPartial}
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

trait UploadConfig extends ServicesConfig {

  def apply(implicit request: Request[_]): String = {
    lazy val url = s"${baseUrl("attachments")}/attachments-internal/uploader"
    val onSuccess = s"${baseUrl("gmp-frontend")}${routes.BulkReferenceController.get()}"
    val onFailure = s"${baseUrl("gmp-frontend")}${routes.FileUploadController.failure()}"
    val callback = s"${baseUrl("gmp-frontend")}${routes.FileUploadController.callback()}"

    Logger.debug(s"[UploadConfig][onSuccessUrl : $onSuccess]")
    s"$url?" +
      s"callbackUrl=${encode(callback)}" +
      s"&onSuccess=${encode(onSuccess)}" +
      s"&onFailure=${encode(onFailure)}" +
      s"&accepts=${encode(".csv")}" +
      s"&collection=${encode("gmp")}" + {
    }

  }

  private def encode(url: String) = URLEncoder.encode(url, "UTF-8")

}

object UploadConfig extends UploadConfig


trait AttachmentsConnector extends HeaderCarrierForPartialsConverter{

  val http: HttpGet with HttpPost = WSHttp

  def getFileUploadPartial()(implicit request: Request[_]): Future[HtmlPartial] = {

    val partial = http.GET[HtmlPartial](UploadConfig(request))

    partial onSuccess {
      case response => Logger.debug(s"[AttachmentsConnector[[getFileUploadPartial : $response]")
    }

    partial
  }

}

object AttachmentsConnector extends AttachmentsConnector{
  // $COVERAGE-OFF$Trivial and never going to be called by a test that uses it's own object implementation
  override val crypto = SessionCookieCryptoFilter.encrypt _
  // $COVERAGE-ON$
}
