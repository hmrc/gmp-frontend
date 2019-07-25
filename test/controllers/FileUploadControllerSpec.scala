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

package controllers

import connectors.AttachmentsConnector
import controllers.auth.{AuthAction, FakeAuthAction, GmpAuthConnector}
import models._
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.{OneServerPerSuite, PlaySpec}
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.Helpers._
import play.api.test.{FakeHeaders, FakeRequest}
import play.twirl.api.Html
import services.SessionService
import uk.gov.hmrc.play.partials.HtmlPartial

import scala.concurrent.Future

class FileUploadControllerSpec extends PlaySpec with OneServerPerSuite with MockitoSugar {
  val mockAuthConnector = mock[GmpAuthConnector]
  val mockAttachmentsConnector = mock[AttachmentsConnector]
  val mockSessionService = mock[SessionService]
  val mockAuthAction = mock[AuthAction]

  val gmpBulkSession = GmpBulkSession(Some(CallBackData(collection = "gmp", id = "id", length = 1L, name = None,
    customMetadata = None, contentType = None, sessionId = "THING")), None, None)

  val fakeRequest = FakeRequest(method = "POST", uri = "", headers = FakeHeaders(Seq("Content-type" -> ("application/json"))), body = Json.toJson(gmpBulkSession.callBackData.get))

  object TestFileUploadController extends FileUploadController(FakeAuthAction, mockAuthConnector, mockSessionService, mockAttachmentsConnector) {
    override val context = FakeGmpContext
  }

  "File upload controller GET " must {

    "authenticated users" must {
      "respond with ok" in {

          getFileUploadPartial(FakeRequest()) {
            result =>
              status(result) must equal(OK)
              contentAsString(result) must include(Messages("gmp.fileupload.header"))
              contentAsString(result) must include(Messages("gmp.back.link"))
        }
      }

      "be shown correct title for DOL" in {

          val result = TestFileUploadController.get(FakeRequest())
          status(result) must equal(OK)
          contentAsString(result) must include(Messages("gmp.fileupload.header"))
      }

    }

    "failure" must {

      "authorised users" must {
        "have a status of OK for generic error" in {
            val result = TestFileUploadController.failure()(FakeRequest())
              status(result) must be(OK)
              contentAsString(result) must include(Messages("gmp.bulk.failure.generic"))
              contentAsString(result) must include(Messages("gmp.bulk.problem.header"))
        }

        "show correct message for virus error" in {
          val result = TestFileUploadController.failure()(FakeRequest("GET", "/upload-csv/failure?error_message=VIRUS"))
              status(result) must be(OK)
              contentAsString(result) must include(Messages("gmp.bulk.failure.antivirus"))
              contentAsString(result) must include(Messages("gmp.bulk.problem.header"))
        }

        "show correct message for missing file error" in {
          val result = TestFileUploadController.failure()(FakeRequest("GET", "/upload-csv/failure?error_message=SELECT"))
              status(result) must be(OK)
              contentAsString(result) must include(Messages("gmp.bulk.failure.missing"))
              contentAsString(result) must include(Messages("gmp.bulk.problem.header"))
            }
      }
    }
  }

  "File upload controller callback " must {


    "successfully store callback data in session cache" in {
      when(mockSessionService.cacheCallBackData(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpBulkSession)))
      val result = TestFileUploadController.callback()(fakeRequest)
      status(result) must be(OK)

    }

    "throw exception when doesn't store callback data" in {
      when(mockSessionService.cacheCallBackData(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
      intercept[RuntimeException]{
       await(TestFileUploadController.callback()(fakeRequest))
      }
    }

    "recover from failures more" in {
      when(mockSessionService.cacheCallBackData(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.failed(new RuntimeException))
      intercept[RuntimeException]{
        await(TestFileUploadController.callback()(fakeRequest))
      }
    }

    "fail to store callback data in session cache when no sessionId" in {
      val callbackData = gmpBulkSession.callBackData.get.copy(sessionId = "")

      val fakeRequest = FakeRequest(method = "POST", uri = "", headers = FakeHeaders(Seq("Content-type" -> ("application/json"))), body = Json.toJson(callbackData))
      when(mockSessionService.cacheCallBackData(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.failed(new RuntimeException))
      intercept[RuntimeException]{
        await(TestFileUploadController.callback()(fakeRequest))
      }
    }

    "store data when valid session id" in {


      val fakeRequest = FakeRequest(method = "POST", uri = "", headers = FakeHeaders(Seq("Content-type" -> ("application/json"))), body = Json.toJson(gmpBulkSession.callBackData))
      when(mockSessionService.cacheCallBackData(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpBulkSession)))
      val result = TestFileUploadController.callback()(fakeRequest)
      status(result) must be(OK)

    }
  }

  def getFileUploadPartial(request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest())(handler: Future[Result] => Any) {
    val html =
      """
    <form id="file-uploader" method="post" action="/attachments/attach/gmp" enctype="multipart/form-data">
      <input name="fileToUpload" id="fileToUpload" type="file" accept=".csv"  />
      <input name="metadata" id="metadata" value="zTCFC5oK2j+ooVABIkaEoRzjcTt3FyyoCExq6tsYdYbGNjjq8zxM2n0si07PdWXiUGhG+4SZBK7CyNE4aLw8D+1pHDE4xzwDWxc70rELSKsgjPi9" type="hidden"/>
      <input name="onSuccessCallbackUrl" id="onSuccessCallbackUrl" value="qDjKUEySXZaT4hDttcSPiCRU1PH0CWu9tqe3sWPjlE8SQoyeJ/Wg0Sj+A88ALs3Yww+/ZIB3c3ZCGEjF3AGXeFHXDUqoCLKpBrArlOM8XjuZ7vAp42BfRpZGexsg334G" type="hidden"/>
      <input name="onSuccessRedirectUrl" id="onSuccessRedirectUrl" value="KB3jnZY9ia8OUhw+ThqM8pmLoX+/Dh5rtEl1ftdBZEUL34um86CVQFf4HSs/bmyC/qBW5rM52zNhKKbBIRLpMnOszo3ryexIumgPibw+LSjnrQ/zAOWFc7te94Ncyeg=" type="hidden"/>
      <input name="onFailureRedirectUrl" id="onFailureRedirectUrl" value="TZPWygBwtCWyJQRBF0UzfQqa5VKAKBNEBYKX+elCT5P0YZFkiEX0ESnOC/fDK2YgMoPHhhUVpvy7y75lhluFNycZDNjRqmoAOoZucl/zCwf8Jqzm4pFfvjblLpzGIAM=" type="hidden"/>
      <button type="submit">Upload</button>
    </form>"""

    when(mockAttachmentsConnector.getFileUploadPartial()(Matchers.any())).thenReturn(Future.successful(HtmlPartial.Success(Some("thepartial"), Html(""))))

    handler(TestFileUploadController.get()(request))
  }

}
