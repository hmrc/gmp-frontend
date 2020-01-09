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

import com.google.inject.Singleton
import config.ApplicationConfig
import controllers.auth.{AuthAction, FakeAuthAction, UUIDGenerator}
import org.mockito.ArgumentCaptor
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.{OneServerPerSuite, PlaySpec}
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SessionService
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.model.DataEvent

import scala.concurrent.ExecutionContext

@Singleton
class ApplicationControllerSpec extends PlaySpec
  with OneServerPerSuite
  with BeforeAndAfterEach
  with ScalaFutures
  with MockitoSugar
  {

  val mockAuthConnector: AuthConnector = mock[AuthConnector]
  val mockAuditConnector: AuditConnector = mock[AuditConnector]
  val mockUUIDGenerator: UUIDGenerator = mock[UUIDGenerator]
  val mockAuthAction: AuthAction = mock[AuthAction]
    implicit val mcc = app.injector.instanceOf[MessagesControllerComponents]
    implicit val ec = app.injector.instanceOf[ExecutionContext]
     implicit val ac=app.injector.instanceOf[ApplicationConfig]
    implicit val ss=app.injector.instanceOf[SessionService]

    object TestController extends ApplicationController(FakeAuthAction, mockAuditConnector, mockAuthConnector, mockUUIDGenerator,
                      ss,FakeGmpContext,mcc,ec,ac) {

  }

  override def beforeEach(): Unit = {
    reset(mockAuditConnector, mockUUIDGenerator)

    when(mockUUIDGenerator.generate).thenReturn("fake-uuid")
  }

  "ApplicationController" must {
    "get /unauthorised" must {

      "have a status of OK" in {
        val result = TestController.unauthorised(FakeRequest())
        status(result) must be(OK)
      }


      "have some text on the page" in {
        val result = TestController.unauthorised(FakeRequest())
        contentAsString(result) must include("You are not authorised to view this page")
      }
    }

    "get /signout" must {
      "redirect to feedback survey" in {
          val result = TestController.signout(FakeRequest())
          redirectLocation(result) must be(Some("http://localhost:9514/feedback/GMP"))
      }

      "send the data to splunk" in {
        when(mockUUIDGenerator.generate).thenReturn("test-uuid")

          val result = TestController.signout(FakeRequest())

          whenReady(result) { _ =>
            val argument = ArgumentCaptor.forClass(classOf[DataEvent])

            verify(mockAuditConnector, times(1)).sendEvent(argument.capture())(any(), any())

            argument.getValue.auditSource mustBe "GMP"
            argument.getValue.auditType mustBe "signout"
            argument.getValue.detail mustBe Map("feedbackId" -> "test-uuid")
        }
      }
    }
  }
}
