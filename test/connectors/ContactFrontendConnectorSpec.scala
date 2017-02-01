/*
 * Copyright 2017 HM Revenue & Customs
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

import org.mockito.Matchers.{eq => meq, _}
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.{BadGatewayException, HeaderCarrier, HttpGet, HttpResponse}

import scala.concurrent.Future

class ContactFrontendConnectorSpec extends PlaySpec with OneAppPerSuite with MockitoSugar with BeforeAndAfterEach with ServicesConfig {
  implicit override lazy val app: Application = new GuiceApplicationBuilder()
  .configure(Map("Test.microservice.assets.url" -> "test-url", "Test.microservice.assets.version" -> "test-version"))
  .build

  implicit val headerCarrier = HeaderCarrier()

  object TestConnector extends ContactFrontendConnector {
    override val http = mock[HttpGet]
  }

  override def beforeEach() = {
    reset(TestConnector.http)
  }

  "ContactFrontendConnector" must {

    val dummyResponseHtml = "<div id=\"contact-partial\"></div>"
    lazy val serviceBase = s"${baseUrl("contact-frontend")}/contact"
    lazy val serviceUrl = s"$serviceBase/problem_reports"

    "contact the front end service to download the 'get help' partial" in {

      val response = HttpResponse(200, responseString = Some(dummyResponseHtml))

      when(TestConnector.http.GET[HttpResponse](meq(serviceUrl))(any(), any[HeaderCarrier])) thenReturn Future.successful(response)

      await(TestConnector.getHelpPartial)

      verify(TestConnector.http).GET(meq(serviceUrl))(any(), any[HeaderCarrier])
    }

    "return an empty string if a BadGatewayException is encountered" in {

      when(TestConnector.http.GET[HttpResponse](meq(serviceUrl))(any(), any[HeaderCarrier])) thenReturn Future.failed(new BadGatewayException("Phony exception"))

      val result = await(TestConnector.getHelpPartial)

      result mustBe ""
    }
  }
}
