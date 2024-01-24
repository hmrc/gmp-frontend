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

import play.api.i18n.Messages
import play.twirl.api.Html
import utils.GmpViewSpec

class IncorrectlyEncodedSpec extends GmpViewSpec {

  val header = Messages("gmp.bulk.incorrectlyEncoded.header")
  val title = s"$header - ${Messages("service.name")} - ${Messages("gov.uk")}"
  val message = Messages("gmp.bulk.incorrectlyEncoded")

  lazy val layout = app.injector.instanceOf[views.html.Layout]
  override def view: Html = new views.html.incorrectlyEncoded(layout)(message, header)

  "Incorrectly encoded page" must {
    behave like pageWithTitle(title)
    behave like pageWithHeader(header)

    "have correct paragraph text" in {
      doc must haveParagraphWithText(message)
    }

    "have a back link" in {
      doc must haveBackLink
    }
  }
}