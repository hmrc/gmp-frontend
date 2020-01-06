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

class UploadFileSpec extends GmpViewSpec {

  override def view: Html = views.html.upload_file(uploadForm)

  "UploadFiles page" must {
    behave like pageWithTitle(messages("gmp.fileupload.header"))
    behave like pageWithHeader(messages("gmp.fileupload.header"))


    "display an explanation text paragraph" in {
      doc must haveParagraphWithText("The file must contain mandatory information and in a specific format. " +
        "You can download a template and instructions as a ZIP file (3Kb) on how to create your file first.")
    }
  }

  private val uploadForm = Html("")

}
