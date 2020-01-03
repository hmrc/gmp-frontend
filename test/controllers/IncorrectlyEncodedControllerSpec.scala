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

import controllers.auth.FakeAuthAction
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.{OneServerPerSuite, PlaySpec}
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.test.FakeRequest
import uk.gov.hmrc.auth.core.AuthConnector
import play.api.test.Helpers._

class IncorrectlyEncodedControllerSpec extends PlaySpec with OneServerPerSuite with MockitoSugar {

  val mockAuthConnector = mock[AuthConnector]

  object TestIncorrectlyEncodedController extends IncorrectlyEncodedController(FakeAuthAction, mockAuthConnector)

  "IncorrectlyEncodedController.get" should {

    "render the incorrectly encoded page" in {

      val result = TestIncorrectlyEncodedController.get(FakeRequest())

      status(result) mustBe INTERNAL_SERVER_ERROR

      contentAsString(result) must include (Messages("gmp.bulk.incorrectlyEncoded.header"))
    }
  }
}
