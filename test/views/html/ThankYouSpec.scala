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

package views.html

import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.html.components.GovukButton
import utils.GmpViewSpec
import views.html.includes.request_another_button

class ThankYouSpec extends GmpViewSpec{
  lazy val layout = app.injector.instanceOf[views.html.Layout]
  lazy val govUkButton = app.injector.instanceOf[GovukButton]

  override def view: Html = new views.html.thank_you(layout, govUkButton)()

  "Thank you page" must {
    behave like pageWithTitle(messages("gmp.thank_you.header"))
    behave like pageWithHeader(messages("gmp.thank_you.header"))
    behave like pageWithH2Header(messages("gmp.thank_you.what_now"))

    "have a correct submit button" in {
      doc must haveSubmitButton(messages("gmp.button.check_another"))
    }

    "have a paragraph with text" in {
      doc must haveParagraphWithText(messages("gmp.or.go.to") + " " + "GOV.UK homepage.")
    }
  }
}
