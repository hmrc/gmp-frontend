/*
 * Copyright 2024 HM Revenue & Customs
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

import com.google.inject.Inject
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Mode
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import play.api.{Application, Configuration, Environment}
import uk.gov.hmrc.http.{BadGatewayException, HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.net.URL
import scala.concurrent.Future

class ContactFrontendConnectorSpec @Inject()(servicesConfig: ServicesConfig) extends HttpClientV2Helper
  with GuiceOneAppPerSuite with BeforeAndAfterEach {

  implicit override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(Map("Test.microservice.assets.url" -> "test-url", "Test.microservice.assets.version" -> "test-version"))
    .build()

  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  protected def mode: Mode = app.injector.instanceOf[Mode]

  protected def runModeConfiguration: Configuration = app.injector.instanceOf[Configuration]

  implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

  object TestConnector extends ContactFrontendConnector(mockHttp, app.injector.instanceOf[Environment],
    runModeConfiguration, app.injector.instanceOf[ServicesConfig])

  override def beforeEach() = {
    reset(mockHttp, requestBuilder)
    when(mockHttp.get(any[URL])(any[HeaderCarrier])).thenReturn(requestBuilder)
    when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)
  }

  "ContactFrontendConnector" must {

    val dummyResponseHtml = "<div id=\"contact-partial\"></div>"
    lazy val serviceBase = s"${servicesConfig.baseUrl("contact-frontend")}/contact"
    lazy val serviceUrl = s"$serviceBase/problem_reports"

    "contact the front end service to download the 'get help' partial" in {

      val response = HttpResponse(200, dummyResponseHtml)

      requestBuilderExecute[HttpResponse](Future.successful(response))

      await(TestConnector.getHelpPartial)

      verify(mockHttp).get(new URL(serviceUrl))(any[HeaderCarrier])
    }

    "return an empty string if a BadGatewayException is encountered" in {

      requestBuilderExecute[HttpResponse](Future.failed(new BadGatewayException("Phony exception")))

      val result = await(TestConnector.getHelpPartial)

      result mustBe ""
    }
  }
}
