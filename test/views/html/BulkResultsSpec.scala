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

import controllers.routes
import models.BulkResultsSummary
import utils.GmpViewSpec

class BulkResultsSpec extends GmpViewSpec{

  val bulkResultsSummary:BulkResultsSummary =  BulkResultsSummary("fake reference", 2, 0)
  val uploadReference: String = "Fakereference"
  val comingFromPage: Int = 1

  "BulkResults page" must {
    behave like pageWithTitle(messages("gmp.bulk.results.title"))
    behave like pageWithHeader(messages("gmp.bulk.results.banner"))
    behave like pageWithH2Header(messages("gmp.bulk.results.reference", bulkResultsSummary.reference))

    "have a div with results text" in {
      doc must haveDivWithText(messages("gmp.bulk.results"))
    }

    "have a div with csv results text" in {
      doc must haveDivWithText(messages("gmp.bulk.results.csv"))
    }

    "have a heading level 3 with csv" in {
      doc must haveHeadingH3WithText(messages("gmp.bulk.subheaders.successfulcalculations") + " (2)")
    }

    "have correct list item number 1" in {
      doc must haveListItemWithText(messages("gmp.bulk.explanations.successful.1"))
    }

    "have correct list item number 2" in {
      doc must haveListItemWithText(messages("gmp.bulk.explanations.successful.2"))
    }

    "have a download success anchor with correct URL and text" in {
      doc.select("#download-success").first must haveLinkURL(routes.BulkResultsController.getResultsAsCsv(uploadReference, "successful").url)
      doc must haveLinkWithText(messages("gmp.download.link"))
    }
  }

  override def view = views.html.bulk_results(bulkResultsSummary, uploadReference, comingFromPage)
}
