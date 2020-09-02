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

class BulkResultsNotFoundSpec extends GmpViewSpec {
  lazy val gmpMain = app.injector.instanceOf[gmp_main]
  override def view: Html = new views.html.bulk_results_not_found(gmpMain)()

  "BulkResultsNotFound page" must {
    behave like pageWithTitle(messages("gmp.bulk.results_not_found"))
    behave like pageWithHeader(messages("gmp.bulk.results_not_found"))

      "have correct paragraphs with text" in {
        doc must haveParagraphWithText("We only keep results for 30 days. If you still need these calculations, you will have to upload the file again.")
        doc must haveParagraphWithText("If you need access to a calculation result for longer than 30 days, you can save the file on your own computer or network after you have downloaded it.")
        doc must haveParagraphWithText("To check other calculation results, go to the GMP dashboard.")
      }
  }
}