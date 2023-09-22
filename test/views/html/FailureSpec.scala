/*
 * Copyright 2023 HM Revenue & Customs
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

class FailureSpec extends GmpViewSpec{
  lazy val layout = app.injector.instanceOf[views.html.Layout]
  override def view: Html = new views.html.failure(layout)(message, header, title)
  private val message : String = "message"
  private val header : String = "header"
  private val title : String = "header - "

  "Failure page" must {
    behave like pageWithTitle(title)
    behave like pageWithHeader(header)

    "have correct paragraph text" in {
      doc must haveParagraphWithText(message)
    }
  }
}
