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

package services

import connectors.UpscanConnector
import helpers.BaseSpec
import models.upscan.{Reference, UpscanInitiateRequest, UpscanInitiateResponse}
import org.mockito.ArgumentCaptor
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.inject.bind
import org.mockito.Mockito._
import org.mockito.Matchers.any
import org.scalatest.concurrent.ScalaFutures
import play.api.mvc.Request
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.SessionId

import scala.concurrent.Future


class UpscanServiceSpec extends BaseSpec with GuiceOneAppPerSuite with MockitoSugar with ScalaFutures {

  override def fakeApplication: Application = new GuiceApplicationBuilder()
    .overrides(bind[UpscanConnector].toInstance(mockUpscanConnector))
    .build()

  def upscanService: UpscanService = app.injector.instanceOf[UpscanService]

  "getUpscanFormDataOds" must {
    "get form data from Upscan Connector with an initiate request" in {
      implicit val request: Request[AnyRef] = FakeRequest("GET", "http://localhost:9941/")
      val hc = HeaderCarrier(sessionId = Some(SessionId("sessionid")))
      val callback = controllers.routes.FileUploadController.callback(hc.sessionId.get.value).absoluteURL()
      val success = controllers.routes.FileUploadController.showResult().absoluteURL()
      val failure = "http://localhost:9941/guaranteed-minimum-pension/upload-csv/failure"
      val expectedInitiateRequest = UpscanInitiateRequest(callback, success, failure)

      val upscanInitiateResponse = UpscanInitiateResponse(Reference("reference"), "postTarget", formFields = Map.empty[String, String])
      val initiateRequestCaptor = ArgumentCaptor.forClass(classOf[UpscanInitiateRequest])

      when(mockUpscanConnector.getUpscanFormData(initiateRequestCaptor.capture())(any[HeaderCarrier]))
        .thenReturn(Future.successful(upscanInitiateResponse))

      (upscanService.getUpscanFormData()(hc, request)).futureValue

      initiateRequestCaptor.getValue shouldBe expectedInitiateRequest
    }
  }

  val mockUpscanConnector: UpscanConnector = mock[UpscanConnector]

}
