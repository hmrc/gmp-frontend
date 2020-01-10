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

import config.ApplicationConfig
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.{OneServerPerSuite, PlaySpec}
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.ExecutionContext

class ServiceUnavailableControllerSpec extends PlaySpec with OneServerPerSuite with MockitoSugar {

  implicit val mcc = app.injector.instanceOf[MessagesControllerComponents]
  implicit val ec = app.injector.instanceOf[ExecutionContext]
  implicit val messagesAPI=app.injector.instanceOf[MessagesApi]
  implicit val messagesProvider=MessagesImpl(Lang("en"), messagesAPI)
  implicit val ac=app.injector.instanceOf[ApplicationConfig]

  object TestController extends ServiceUnavailableController(mcc, FakeGmpContext,ac) {
    override implicit val context = FakeGmpContext
  }

  "GET" must {

    "be accessible without authorisation" in {
      val result = TestController.get(FakeRequest())

      status(result) must be(OK)
    }

    "display the service unavailable message" in {
      val result = TestController.get(FakeRequest())

      contentAsString(result) must include(Messages("gmp.serviceunavailable.message"))
      contentAsString(result) must include(Messages("gmp.serviceunavailable.title"))
      contentAsString(result) must not include(Messages("gmp.back_to_dashboard"))
    }
  }

}
