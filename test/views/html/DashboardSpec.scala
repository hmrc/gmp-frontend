/*
 * Copyright 2018 HM Revenue & Customs
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

import models.BulkPreviousRequest
import org.joda.time.LocalDateTime
import play.twirl.api.Html
import utils.GmpViewSpec

class DashboardSpec extends GmpViewSpec{

  "MoreBulkResults page" must {

    behave like pageWithTitle(messages("gmp.dashboard.title"))
    behave like pageWithHeader(messages("gmp.dashboard_header"))
    behave like pageWithH2Header(messages("gmp.dashboard.choose_calculation_type"))

    "have a single calculation link" in {
      doc must haveLinkWithText(messages("gmp.single_calculation_link"))
      doc must haveParagraphWithText(messages("Get a pension scheme member's GMP calculation."))
    }

    "have a bulk calculation link" in {
      doc must haveLinkWithText(messages("gmp.bulk_calculation_link"))
      doc must haveParagraphWithText(messages("gmp.bulk_calculation_text"))
    }

    "have a download template link" in {
      doc must haveLinkWithText(messages("gmp.download_templates_link"))
    }
    
  }


  override def view: Html = views.html.dashboard(bulkPreviousRequestsList)
  private val bulkPreviousRequestsList: List[models.BulkPreviousRequest] = List(BulkPreviousRequest(uploadReference = "upload",
    reference = "fake", timestamp = LocalDateTime.now, processedDateTime = LocalDateTime.now),BulkPreviousRequest(uploadReference = "upload",
    reference = "fake", timestamp = LocalDateTime.now, processedDateTime = LocalDateTime.now),BulkPreviousRequest(uploadReference = "upload",
    reference = "fake", timestamp = LocalDateTime.now, processedDateTime = LocalDateTime.now),BulkPreviousRequest(uploadReference = "upload",
    reference = "fake", timestamp = LocalDateTime.now, processedDateTime = LocalDateTime.now))
}
