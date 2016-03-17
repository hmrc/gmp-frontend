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

import java.util.UUID

import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.{OneServerPerSuite, PlaySpec}
import play.api.Play
import play.api.http.HeaderNames
import play.api.test.{FakeApplication, FakeRequest}
import uk.gov.hmrc.play.frontend.filters.SessionCookieCryptoFilter
import uk.gov.hmrc.play.http.hooks.HttpHook
import uk.gov.hmrc.play.http.logging.RequestId
import uk.gov.hmrc.play.http.ws.{WSGet, WSPost}
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.partials.HeaderCarrierForPartials

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AttachmentsConnectorSpec extends PlaySpec with OneServerPerSuite with MockitoSugar with BeforeAndAfterEach {

  class MockHttp extends WSGet with WSPost {

    override val hooks: Seq[HttpHook] = Nil
  }

  val mockHttp = mock[MockHttp]

  object TestAttachmentsConnector extends AttachmentsConnector {
    val crypto = SessionCookieCryptoFilter.encrypt _
    override val http = mockHttp
  }

  override def beforeEach = {
    reset(mockHttp)

  }


  "AttachmentsConnector" must {

    "getFileUploadPartial" must {

      "returns the partial from the attachemnst service" in {
        implicit val request = FakeRequest()
        implicit val hc = HeaderCarrier()
        implicit val hcwc = HeaderCarrierForPartials(hc,"")
        val html = "<h1>helloworld</h1>"
        when(mockHttp.GET[HttpResponse](Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(HttpResponse(200, responseString = Some
        (html))))
        TestAttachmentsConnector.getFileUploadPartial().map {
          response => response.successfulContentOrEmpty must equal(html)
        }
      }

    }

  }


  "UploadConfig" must {
    implicit val hc = HeaderCarrier(requestId = Some(RequestId(UUID.randomUUID().toString)))
    Play.start(new FakeApplication)
    val request = FakeRequest().withHeaders(HeaderNames.HOST -> "test.com")
    val config = UploadConfig(request)

    "have the collection" in {
      config must include("collection=gmp")
    }

    "have a the attachments service url" in {
      config must include("http://localhost:8895/attachments-internal/uploader")
    }

//    "have a success url parameter" in {
//      config must include("onSuccess=http%3A%2F%2Ftest.com%2Fguaranteed-minimum-pension%2Ffile-upload%2Femail-and-reference")
//    }
//
//    "have a failure url parameter" in {
//      config must include("onFailure=http%3A%2F%2Ftest.com%2Fguaranteed-minimum-pension%2Ffile-upload%2Ffailure")
//    }
//
//    "have a callback url parameter" in {
//      config must include("callbackUrl=http%3A%2F%2Ftest.com%2Fguaranteed-minimum-pension%2Ffile-upload%2Fcallback")
//    }

    "accept .csv" in {
      config must include("accepts=.csv")
    }


//    "returns the correct url with parameters" in {
//      config must be(
//        "http://localhost:8895/attachments-internal/uploader?" +
//          "callbackUrl=http%3A%2F%2Ftest.com%2Fguaranteed-minimum-pension%2Ffile-upload%2Fcallback" +
//          "&onSuccess=http%3A%2F%2Ftest.com%2Fguaranteed-minimum-pension%2Ffile-upload%2Femail-and-reference" +
//          "&onFailure=http%3A%2F%2Ftest.com%2Fguaranteed-minimum-pension%2Ffile-upload%2Ffailure" +
//          "&accepts=.csv" +
//          "&collection=gmp"
//      )
//    }


    Play.stop()
  }


}
