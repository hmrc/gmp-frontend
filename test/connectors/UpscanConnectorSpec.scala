/*
 * Copyright 2021 HM Revenue & Customs
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

import models.upscan.{PreparedUpload, Reference, UploadForm, UpscanInitiateRequest}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import utils.WireMockHelper
import com.github.tomakehurst.wiremock.client.WireMock._
import scala.concurrent.duration._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.http.Status._
import scala.concurrent.Await
import play.api.libs.json.Json


class UpscanConnectorSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite with MockitoSugar with WireMockHelper {

  "getUpscanFormData" should {
    val timeout = 10000 millis

    "return a UpscanInitiateResponse" when {
      "upscan returns valid successful response" in {
        val body = PreparedUpload(Reference("Reference"), UploadForm("downloadUrl", Map("formKey" -> "formValue")))
        server.stubFor(
          post(urlEqualTo(connector.upscanInitiatePath))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withBody(Json.toJson(body).toString())
            )
        )

        val result = Await.result(connector.getUpscanFormData(request), timeout)
        result shouldBe body.toUpscanInitiateResponse
      }
    }

    "throw an exception" when {
      "upscan returns a 4xx response" in {
        server.stubFor(
          post(urlEqualTo(connector.upscanInitiatePath))
            .willReturn(
              aResponse()
                .withStatus(BAD_REQUEST)
            )
        )
        a [UpstreamErrorResponse] should be thrownBy Await.result(connector.getUpscanFormData(request), timeout)
      }

      "upscan returns 5xx response" in {
        server.stubFor(
          post(urlEqualTo(connector.upscanInitiatePath))
            .willReturn(
              aResponse()
                .withStatus(SERVICE_UNAVAILABLE)
            )
        )
        an [UpstreamErrorResponse] should be thrownBy Await.result(connector.getUpscanFormData(request), timeout)
      }
    }
  }

  lazy val connector: UpscanConnector = app.injector.instanceOf[UpscanConnector]
  implicit val hc: HeaderCarrier = HeaderCarrier()
  val request = UpscanInitiateRequest("callbackUrl", "successRedirectUrl", "errorRedirectUrl")
  override def fakeApplication(): Application = new GuiceApplicationBuilder()
    .configure(
      "microservice.services.upscan.port" -> server.port()
    ).build()
}
