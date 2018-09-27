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

class MoreBulkResultsSpec extends GmpViewSpec {

  "MoreBulkResults page" must {

    behave like pageWithTitle(messages("gmp.more_bulk_results.header"))
    behave like pageWithHeader(messages("gmp.more_bulk_results.header"))
    behave like pageWithBackLink

    "display recent calculations table" in {
      doc must haveThWithText(messages("gmp.th.reference"))
      doc must haveThWithText(messages("gmp.th.upload_date"))
      doc must haveThWithText(messages("gmp.th.time_left"))

      doc must haveTdWithText("fake")
      doc must haveTdWithText(LocalDateTime.now.toString("dd MMMM yyyy"))
      //doc must haveTdWithText("30 Days")
    }


  }
  override def view: Html = views.html.more_bulk_results(bulkPreviousRequestsList)
  private val bulkPreviousRequestsList: List[models.BulkPreviousRequest] = List(BulkPreviousRequest(uploadReference = "upload",
    reference = "fake", timestamp = LocalDateTime.now, processedDateTime = LocalDateTime.now),BulkPreviousRequest(uploadReference = "upload",
    reference = "fake", timestamp = LocalDateTime.now, processedDateTime = LocalDateTime.now),BulkPreviousRequest(uploadReference = "upload",
    reference = "fake", timestamp = LocalDateTime.now, processedDateTime = LocalDateTime.now),BulkPreviousRequest(uploadReference = "upload",
    reference = "fake", timestamp = LocalDateTime.now, processedDateTime = LocalDateTime.now))

}
