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

import org.scalatestplus.play.{OneServerPerSuite, PlaySpec}
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.api.test.Helpers._

class ApplicationControllerSpec extends PlaySpec with OneServerPerSuite {

  "ApplicationController" must {

    "respond to /unauthorised" in {
      val result = route(FakeRequest(GET, "/guaranteed-minimum-pension/unauthorised"))
      status(result.get) must not equal (NOT_FOUND)
    }
  }

  "get /unauthorised" must {

    "have a status of OK" in {
      val result = ApplicationController.unauthorised.apply(FakeRequest())
      status(result) must be(OK)
    }

    "have a title of Unauthorised" in {
      val result = ApplicationController.unauthorised.apply(FakeRequest())
      contentAsString(result) must include(Messages("gmp.unauthorised.title"))
    }

    "have some text on the page" in {
      val result = ApplicationController.unauthorised.apply(FakeRequest())
      contentAsString(result) must include("You are not authorised to view this page")
    }
  }
}
