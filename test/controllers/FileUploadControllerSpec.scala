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

package controllers

import java.time.Instant

import config.{ApplicationConfig, GmpSessionCache}
import controllers.auth.{AuthAction, FakeAuthAction}
import models._
import models.upscan._
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.{OneServerPerSuite, PlaySpec}
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, MessagesControllerComponents, Result}
import play.api.test.Helpers._
import play.api.test.{FakeHeaders, FakeRequest}
import play.twirl.api.Html
import services.{SessionService, UpscanService}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.play.partials.HtmlPartial
import java.net.URL

import akka.stream.Materializer
import akka.util.ByteString
import org.scalatest.concurrent.ScalaFutures
import play.api.libs.streams.Accumulator
import views.Views

import scala.concurrent.{ExecutionContext, Future}

class FileUploadControllerSpec extends PlaySpec with OneServerPerSuite with MockitoSugar with ScalaFutures {
  val mockAuthConnector = mock[AuthConnector]
  val mockSessionService = mock[SessionService]
  val mockAuthAction = mock[AuthAction]
  val upscanService = mock[UpscanService]
  implicit val mcc = app.injector.instanceOf[MessagesControllerComponents]
  implicit val ec = app.injector.instanceOf[ExecutionContext]
  implicit val messagesAPI=app.injector.instanceOf[MessagesApi]
  implicit val messagesProvider=MessagesImpl(Lang("en"), messagesAPI)
  implicit val ac=app.injector.instanceOf[ApplicationConfig]
  implicit val gmpSessionCache=app.injector.instanceOf[GmpSessionCache]
  lazy val views = app.injector.instanceOf[Views]
  val uploadDetails = UploadDetails(Instant.now, "sum", "csv", "name1")
  val callBackData = UpscanReadyCallback("ref1", "READY", new URL("http://localhost:9991/download1"), uploadDetails)

  val emptyGmpBulkSession = GmpBulkSession(None, None, None)
  val gmpBulkSession = GmpBulkSession(Some(UploadedSuccessfully("ref1", "name1", "http://localhost:9991/download1")), None, None)

  val fakeRequest = FakeRequest(method = "POST", uri = "", headers = FakeHeaders(Seq("Content-type" -> ("application/json"))), body = Json.toJson(callBackData))

  object TestFileUploadController extends FileUploadController(FakeAuthAction, mockAuthConnector, mockSessionService, FakeGmpContext,upscanService,mcc,ac,ec,gmpSessionCache,views) {

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

        when(mockSessionService.resetGmpBulkSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(emptyGmpBulkSession)))
        when(upscanService.getUpscanFormData()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(UpscanInitiateResponse(Reference("ref1"), "to", Map())))
        when(mockSessionService.createCallbackRecord(Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
          val result = TestFileUploadController.get(FakeRequest())
          status(result) must equal(OK)
          contentAsString(result) must include(Messages("gmp.fileupload.header"))
      }

    }

    "failure" must {

      "authorised users" must {
        "have a status of OK for generic error" in {
            val result = TestFileUploadController.failure("", "", "")(FakeRequest())
              status(result) must be(OK)
              contentAsString(result) must include(Messages("gmp.bulk.failure.generic"))
              contentAsString(result) must include(Messages("gmp.bulk.problem.header"))
        }
      }
    }
  }

  "File upload controller callback " must {


    "successfully store callback data in session cache" in {
      when(mockSessionService.cacheCallBackData(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpBulkSession)))
      when(mockSessionService.updateCallbackRecord(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful())
      val result = TestFileUploadController.callback("session1")(fakeRequest)
      status(result) must be(OK)

    }

    "throw exception when doesn't store callback data" in {
      when(mockSessionService.cacheCallBackData(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.failed(new RuntimeException("Failed to update cache")))
      intercept[RuntimeException]{
       await(TestFileUploadController.callback("1")(fakeRequest))
      }
    }

    "recover from failures more" in {
      when(mockSessionService.cacheCallBackData(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.failed(new RuntimeException))
      intercept[RuntimeException]{
        await(TestFileUploadController.callback("1")(fakeRequest))
      }
    }


    "store data when valid session id" in {


      val fakeRequest = FakeRequest(method = "POST", uri = "", headers = FakeHeaders(Seq("Content-type" -> ("application/json"))), body = Json.toJson(callBackData))
      when(mockSessionService.cacheCallBackData(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(gmpBulkSession)))
      val result = TestFileUploadController.callback("1")(fakeRequest)
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

  }

}
