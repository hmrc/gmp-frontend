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

abstract class BulkResultsSpec extends GmpViewSpec{

  val bulkResultsSummary:BulkResultsSummary
  val uploadReference: String
  val comingFromPage: Int

  override def view = views.html.bulk_results(bulkResultsSummary, uploadReference, comingFromPage)
}


class BulkResultsSpecSuccess extends BulkResultsSpec {

  override val bulkResultsSummary:BulkResultsSummary =  BulkResultsSummary("fake reference", 2, 0)
  override val uploadReference: String = "Fakereference"
  override val comingFromPage: Int = 1

  "BulkResults page for success" must {

    behave like pageWithTitle(messages("gmp.bulk.results.title"))
    behave like pageWithHeader(messages("gmp.bulk.results.banner"))
    behave like pageWithH2Header(messages("gmp.bulk.results.reference", bulkResultsSummary.reference))

    "have a div with results text" in {
      doc must haveDivWithText(messages("gmp.bulk.results"))
    }

    "have a div with csv results text" in {
      doc must haveDivWithText(messages("gmp.bulk.results.csv"))
    }

    // Success-specific

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

    "have a heading level 3 with conts and earnings" in {
      doc must haveHeadingH3WithText(messages("gmp.bulk.subheaders.contsandearnings"))
    }

    "have correct list item with const earnings" in {
      doc must haveListItemWithText(messages("gmp.bulk.explanations.contsandearnings"))
    }

    "have a download conts anchor with correct URL and text" in {
      doc.select("#download-conts").first must haveLinkURL(routes.BulkResultsController.getContributionsAndEarningsAsCsv(uploadReference).url)
      doc must haveLinkWithText(messages("gmp.download.link"))
    }

    // Not have Failure specific
    " not have a heading level 3 with failed page" in {
      doc mustNot haveHeadingH3WithText(messages("gmp.bulk.subheaders.failedcalculations") + " (2)")
    }

    "not have failure list item number 1" in {
      doc mustNot haveListItemWithText(messages("gmp.bulk.explanations.failed.1"))
    }

    "not have failure list item number 2" in {
      doc mustNot haveListItemWithText(messages("gmp.bulk.explanations.failed.2"))
    }

    "not have failure list item number 3" in {
      doc mustNot haveListItemWithText(messages("gmp.bulk.explanations.failed.3"))
    }
  }
}

class BulkResultsSpecFailure extends BulkResultsSpec {

  override val bulkResultsSummary:BulkResultsSummary =  BulkResultsSummary("fake reference", 2, 2)
  override val uploadReference: String = "Fakereference"
  override val comingFromPage: Int = 1

  "BulkResults page for failure" must {

    // Failure-specific
    "have a heading level 3 with failed page" in {
        doc must haveHeadingH3WithText(messages("gmp.bulk.subheaders.failedcalculations") + " (2)")
    }

    "have failure list item number 1" in {
      doc must haveListItemWithText(messages("gmp.bulk.explanations.failed.1"))
    }

    "have failure list item number 2" in {
      doc must haveListItemWithText(messages("gmp.bulk.explanations.failed.2"))
    }

    "have failure list item number 3" in {
      doc must haveListItemWithText(messages("gmp.bulk.explanations.failed.3"))
    }

    "have a download failure anchor with correct URL and text" in {
      doc.select("#download-failed").first must haveLinkURL(routes.BulkResultsController.getResultsAsCsv(uploadReference, "failed").url)
      doc must haveLinkWithText(messages("gmp.download.link"))
    }

    "have a heading level 3 with failure conts and earnings" in {
      doc must haveHeadingH3WithText(messages("gmp.bulk.subheaders.contsandearnings"))
    }

    "have correct failure list item with const earnings" in {
      doc must haveListItemWithText(messages("gmp.bulk.explanations.contsandearnings"))
    }

    "have a failure download conts anchor with correct URL and text" in {
      doc.select("#download-conts").first must haveLinkURL(routes.BulkResultsController.getContributionsAndEarningsAsCsv(uploadReference).url)
      doc must haveLinkWithText(messages("gmp.download.link"))
    }

    // Not have Success-specific
    "not have a heading level 3 with csv" in {
      doc mustNot haveHeadingH3WithText(messages("gmp.bulk.subheaders.successfulcalculations") + " (2)")
    }

    "not have correct list item number 1" in {
      doc mustNot haveListItemWithText(messages("gmp.bulk.explanations.successful.1"))
    }

    "not have correct list item number 2" in {
      doc mustNot haveListItemWithText(messages("gmp.bulk.explanations.successful.2"))
    }

  }
}

class BulkResultsSpecAll extends BulkResultsSpec {

  override val bulkResultsSummary:BulkResultsSummary =  BulkResultsSummary("fake reference", 3, 1)
  override val uploadReference: String = "Fakereference"
  override val comingFromPage: Int = 1

  "BulkResults page for failure" must {

  //All Specific

    "have a heading level 3 with all page" in {
      doc must haveHeadingH3WithText(messages("gmp.bulk.subheaders.allcalculations") + " (3)")
    }
  }
}