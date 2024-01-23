/*
 * Copyright 2024 HM Revenue & Customs
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

package forms

import models.PensionDetailsScon
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import org.scalatestplus.play.PlaySpec
import play.api.data.FormError
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.api.libs.json.Json
import play.api.mvc.MessagesControllerComponents

class PensionDetailsSconFormSpec extends PlaySpec with GuiceOneAppPerSuite with MockitoSugar{

  implicit lazy val messagesAPI=app.injector.instanceOf[MessagesApi]
  implicit lazy val messagesProvider=MessagesImpl(Lang("en"), messagesAPI)
  lazy val mcc = app.injector.instanceOf[MessagesControllerComponents]
  lazy val pensionDetailsForm = new PensionDetails_no_longer_used_Form(mcc).pensionDetailsForm
  val fromJsonMaxChars: Int = 102400

  "Pension details form" must {

    "return no errors with valid scon" in {

      val gmpRequest = Json.toJson(PensionDetailsScon("S1301234T"))
      val pensionDetailsResult = pensionDetailsForm.bind(gmpRequest, fromJsonMaxChars)

      assert(!pensionDetailsResult.errors.contains(FormError("scon",List("error.invalid"))))

    }

    "return error when scon is empty" in {

      val gmpRequest = Json.toJson(PensionDetailsScon(""))
      val pensionDetailsResult = pensionDetailsForm.bind(gmpRequest, fromJsonMaxChars)

      assert(pensionDetailsResult.errors.contains(FormError("scon", List("error.required"))))

    }

    "return error when scon is invalid" in {

      val gmpRequest = Json.toJson(PensionDetailsScon("ABCD"))
      val pensionDetailsResult = pensionDetailsForm.bind(gmpRequest, fromJsonMaxChars)

      assert(pensionDetailsResult.errors.contains(FormError("scon", List("error.invalid"))))

    }
  }
}
