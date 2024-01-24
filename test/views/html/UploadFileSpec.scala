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

import models.upscan.{Reference, UpscanInitiateRequest, UpscanInitiateResponse}
import play.twirl.api.Html
import utils.GmpViewSpec
import views.ViewHelpers

class UploadFileSpec extends GmpViewSpec {

  val upscanInitiate = UpscanInitiateResponse(Reference("reference"), "download", Map())
  lazy val layout = app.injector.instanceOf[views.html.Layout]
  lazy val viewHelper = app.injector.instanceOf[ViewHelpers]
  override def view: Html = new views.html.upscan_csv_file_upload(layout, viewHelper)(upscanInitiate)

  "UploadFiles page" must {
    behave like pageWithTitle(messages("gmp.fileupload.header"))
    behave like pageWithHeader(messages("gmp.fileupload.header"))


    "display an explanation text paragraph" in {
      doc must haveParagraphWithText("The file must contain mandatory information and be in a specific format. " +
        "To create your file you can download a template and instructions as a ZIP file (3Kb).")
    }
  }

}
