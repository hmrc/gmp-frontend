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

import com.google.inject.Inject
import org.mockito.Matchers.{eq => meq, _}
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Mode
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import play.api.{Application, Configuration, Environment, Play}
import uk.gov.hmrc.http.{BadGatewayException, HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}

class ContactFrontendConnectorSpec @Inject()(servicesConfig: ServicesConfig) extends PlaySpec with GuiceOneAppPerSuite with MockitoSugar with BeforeAndAfterEach{
  implicit override lazy val app: Application = new GuiceApplicationBuilder()
  .configure(Map("Test.microservice.assets.url" -> "test-url", "Test.microservice.assets.version" -> "test-version"))
  .build

  protected def mode: Mode = Play.current.mode
  protected def runModeConfiguration: Configuration = Play.current.configuration


  implicit val headerCarrier = HeaderCarrier()
  val mockHttp = mock[HttpClient]

  object TestConnector extends ContactFrontendConnector(mockHttp, app.injector.instanceOf[Environment],
                                                          runModeConfiguration,app.injector.instanceOf[ServicesConfig])

  override def beforeEach() = {
    reset(mockHttp)
  }

  "ContactFrontendConnector" must {

    val dummyResponseHtml = "<div id=\"contact-partial\"></div>"
    lazy val serviceBase = s"${servicesConfig.baseUrl("contact-frontend")}/contact"
    lazy val serviceUrl = s"$serviceBase/problem_reports"

    "contact the front end service to download the 'get help' partial" in {

      val response = HttpResponse(200, responseString = Some(dummyResponseHtml))

      when(mockHttp.GET[HttpResponse](meq(serviceUrl))(any(), any[HeaderCarrier], any[ExecutionContext])) thenReturn Future.successful(response)

      await(TestConnector.getHelpPartial)

      verify(mockHttp).GET(meq(serviceUrl))(any(), any[HeaderCarrier], any[ExecutionContext])
    }

    "return an empty string if a BadGatewayException is encountered" in {

      when(mockHttp.GET[HttpResponse](meq(serviceUrl))(any(), any[HeaderCarrier], any[ExecutionContext])) thenReturn Future.failed(new BadGatewayException("Phony exception"))

      val result = await(TestConnector.getHelpPartial)

      result mustBe ""
    }
  }
}
