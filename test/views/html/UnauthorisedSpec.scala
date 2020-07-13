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

package views.html

import play.twirl.api.Html
import utils.GmpViewSpec

class UnauthorisedSpec extends GmpViewSpec {
  lazy val gmpMain = app.injector.instanceOf[gmp_main]
  override def view: Html = new views.html.unauthorised(gmpMain)()

  "Unauthorised page" must {
    behave like pageWithTitle(messages("gmp.unauthorised.message"))
    behave like pageWithHeader(messages("gmp.unauthorised.message"))
  }

}
