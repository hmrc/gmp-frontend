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

import controllers.auth.ExternalUrls
import play.twirl.api.Html
import utils.GmpViewSpec

class BulkWrongUserSpec extends GmpViewSpec{
  lazy val layout = app.injector.instanceOf[views.html.Layout]
  lazy val externalUrls = app.injector.instanceOf[ExternalUrls]
  override def view: Html = new views.html.bulk_wrong_user(layout, externalUrls)()

  "BulkWrongUser page" must {
    behave like pageWithTitle(messages("gmp.bulk.problem.header"))
    behave like pageWithHeader(messages("gmp.bulk.problem.header"))

    "have a paragraph with text 1" in {
      doc must haveParagraphWithText(messages("gmp.bulk.wrong_user.login_text"))

    }

    "have a bulk wrong user ign out anchor with correct URL and text" in {
      doc.select("#bulk_wrong_user_sign_out").first must haveLinkURL(externalUrls.signOut)
      doc must haveLinkWithText(messages("gmp.signout"))
    }
  }
}
