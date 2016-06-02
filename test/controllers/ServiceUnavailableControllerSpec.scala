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

package controllers

import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.{OneServerPerSuite, PlaySpec}
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.api.test.Helpers._


class ServiceUnavailableControllerSpec extends PlaySpec with OneServerPerSuite with MockitoSugar {

  "Service unavailable controller" must {

    "respond to GET /guaranteed-minimum-pension/service-unavailable" in {
      val result = route(FakeRequest(GET, "/guaranteed-minimum-pension/service-unavailable"))
      status(result.get) must not equal (NOT_FOUND)

    }
  }

  "GET" must {

    "be accessible without authorisation" in {
      val result = controllers.ServiceUnavailableController.get.apply(FakeRequest())
      status(result) must be(OK)
    }

    "display the service unavailable message" in {
      val result = controllers.ServiceUnavailableController.get.apply(FakeRequest())
      contentAsString(result) must include(Messages("gmp.serviceunavailable.message"))
      contentAsString(result) must include(Messages("gmp.serviceunavailable.title"))
      contentAsString(result) must not include(Messages("gmp.back_to_dashboard"))
    }
  }



}
