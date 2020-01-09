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

import java.util.UUID

import org.mockito.Matchers
import org.mockito.Matchers.any
import org.mockito.Mockito.{when, _}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.http.HeaderNames
import play.api.i18n.{Messages, MessagesApi, MessagesImpl}
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import uk.gov.hmrc.crypto.{Crypted, Decrypter, Encrypter, PlainText}
import uk.gov.hmrc.http.logging.RequestId
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.filters.frontend.crypto.SessionCookieCrypto
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.play.partials.HeaderCarrierForPartials

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AttachmentsConnectorSpec extends PlaySpec with OneAppPerSuite with MockitoSugar with BeforeAndAfterEach {


  val mockHttp = mock[HttpClient]
  val uploadConfig = app.injector.instanceOf[UploadConfig]
  val encrypter = mock[Encrypter with Decrypter]

  implicit val messagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents].langs.availables.head
  implicit val messagesApi =  app.injector.instanceOf[MessagesApi]
  implicit lazy val messages: Messages = MessagesImpl(messagesControllerComponents, messagesApi)

  when(encrypter.encrypt(any[PlainText])).thenReturn(Crypted("foo"))
  val sessionCookieCrypto = mock[SessionCookieCrypto]
  when(sessionCookieCrypto.crypto).thenReturn(encrypter)

  class TestAttachmentsConnector extends AttachmentsConnector(uploadConfig,sessionCookieCrypto, mockHttp, app.configuration) {
    val sessionCookieCrypto: SessionCookieCrypto = app.injector.instanceOf[SessionCookieCrypto]
    override val crypto: (String) => String = cookie =>
      sessionCookieCrypto.crypto.encrypt(PlainText(cookie)).value
  }

  override def beforeEach = {
    reset(mockHttp)

  }

  "AttachmentsConnector" must {

    "getFileUploadPartial" when {

      "returns the partial from the attachemnst service" in {
        implicit val request = FakeRequest()
        implicit val hc = HeaderCarrier()
        implicit val hcwc = HeaderCarrierForPartials(hc,"")
        val html = "<h1>helloworld</h1>"
        when(mockHttp.GET[HttpResponse](Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(HttpResponse(200, responseString = Some
        (html))))
        new TestAttachmentsConnector().getFileUploadPartial().map {
          response => response.successfulContentOrEmpty must equal(html)
        }
      }

    }

  }

  "UploadConfig" must {
    implicit val hc = HeaderCarrier(requestId = Some(RequestId(UUID.randomUUID().toString)))
    //Play.start(new FakeApplication)
    val request = FakeRequest().withHeaders(HeaderNames.HOST -> "test.com")


    "have the collection" in {
      val config = uploadConfig(request,messages)
      config must include("collection=gmp")
    }

    "have a the attachments service url" in {
      val config = uploadConfig(request,messages)
      config must include("http://localhost:8895/attachments-internal/uploader")
    }

    "accept .csv" in {
      val config = uploadConfig(request,messages)
      config must include("accepts=.csv")
    }

  }

}
