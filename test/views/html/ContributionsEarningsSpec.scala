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

import models.{CalculationPeriod, CalculationResponse, ContributionsAndEarnings}
import org.joda.time.{LocalDate, LocalDateTime}
import play.twirl.api.Html
import utils.GmpViewSpec

class ContributionsEarningsSpec extends GmpViewSpec {
  override def view: Html = views.html.contributions_earnings(calculationResponse)

  private val calculationResponse: CalculationResponse = CalculationResponse("name", "nino", "scon", Some("revaluationRate"),
    Some(LocalDate.now), List(CalculationPeriod(Some(LocalDate.now), LocalDate.now(), "gmpTotal", "post", 1, 2, Some(3), Some("string"),
      Some("string2"), Some(4), Some(List(ContributionsAndEarnings(2018, "2000"))))), 0, Some(LocalDate.now), Some(LocalDate.now), Some(LocalDate.now), true, 1)

  "Contributions Earnings page" must {
    behave like pageWithTitle(messages("gmp.contributions_earnings.title"))
    behave like pageWithHeader(messages("gmp.contributions_earnings.header"))
    behave like pageWithH2Header(messages("gmp.entered_details.title"))

    "have a correct span with text" in {
      doc must haveSpanWithText(messages("gmp.queryhandling.contsandearnings"))
    }

    "have a valid back link" in {
      doc must haveLinkWithText("Back")
    }

    "have a table with td's" in {
      doc must haveTdWithText(messages("Name:"))
      doc must haveTdWithText(messages("Scheme Contracted Out Number:"))
      doc must haveTdWithText(messages("National Insurance number:"))
    }

    "have a valid submit button" in {
      doc must haveSubmitButton(messages("gmp.button.request-another"))
    }

    "have a valid print page link" in {
      doc must haveLinkWithText(messages("gmp.print"))
    }

  }
}
