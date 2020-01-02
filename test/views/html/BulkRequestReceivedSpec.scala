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

class BulkRequestReceivedSpec extends GmpViewSpec{

  "BulkRequestReceived page" must {
    behave like pageWithTitle(messages("gmp.bulk_request_received.banner"))
    behave like pageWithHeader(messages("gmp.bulk_request_received.banner"))
    behave like pageWithH2Header(messages("gmp.bulk_request_received.header"))
  }

  "have bullet points with description, reference displaying at paragraph two" in {
    doc must haveBulletPointWithText("Your calculation should be complete in less than 72 hours")
    doc must haveBulletPointWithText(s"It will then appear on the GMP checker dashboard with the reference $reference")
  }

  "have a button to upload another file" in {
   doc must haveSubmitButton(messages("gmp.bulk_request_received.button"))
  }

  override def view: Html = views.html.bulk_request_received(reference)

  val reference: String = "Fake reference"
}
