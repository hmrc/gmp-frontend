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

package forms

import forms.EqualiseForm
import models.Equalise
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.Json
import play.api.mvc.MessagesControllerComponents

class EqualiseFormSpec extends PlaySpec with GuiceOneAppPerSuite{

  lazy val mcc = app.injector.instanceOf[MessagesControllerComponents]
  lazy val equaliseForm = new EqualiseForm(mcc).equaliseForm


  "Equalise Form" must {

    "return errors when revaluation yes/no not entered" in {

      val equaliseFormResults = equaliseForm.bind(Json.toJson(Equalise(None)))

      assert(equaliseFormResults.errors.size == 1)

    }
  }
}
