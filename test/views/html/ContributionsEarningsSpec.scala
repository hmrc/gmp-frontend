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

import models.{CalculationPeriod, CalculationResponse, ContributionsAndEarnings}
import java.time.LocalDate
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.html.components.{GovukTable}
import utils.GmpViewSpec
import views.html.includes.{member_details_result, request_another_button}

class ContributionsEarningsSpec extends GmpViewSpec {
  lazy val layout = app.injector.instanceOf[views.html.Layout]
  lazy val requestAnotherButton = app.injector.instanceOf[request_another_button]
  lazy val memberDetailsResult = app.injector.instanceOf[member_details_result]
   val govUkTable = app.injector.instanceOf[GovukTable]
  private val calculationResponse: CalculationResponse = CalculationResponse("name", "nino", "scon", Some("revaluationRate"),
    Some(LocalDate.now), List(CalculationPeriod(Some(LocalDate.now), LocalDate.now(), "gmpTotal", "post", 1, 2, Some(3), Some("string"),
      Some("string2"), Some(4), Some(List(ContributionsAndEarnings(2018, "2000"))))), 0, Some(LocalDate.now), Some(LocalDate.now), Some(LocalDate.now), true, 1)
  override def view: Html = new views.html.contributions_earnings(layout, requestAnotherButton, memberDetailsResult, govUkTable)(calculationResponse)

  "Contributions Earnings page" must {
    behave like pageWithTitle(messages("gmp.contributions_earnings.header"))
    behave like pageWithHeader(messages("gmp.contributions_earnings.header"))
    behave like pageWithH2Header(messages("gmp.entered_details.title"))


    "have a valid back link" in {
      doc must haveLinkWithText("Back")
    }

    "have a scheme member summary list" in {
      doc must haveDescriptionListWithId("member-details-table")
      doc must haveElementAtPathWithText("dt", messages("gmp.name"))
      doc must haveElementAtPathWithText("dt", messages("gmp.scon.noabrv"))
      doc must haveElementAtPathWithText("dt", messages("gmp.nino"))
      doc must haveElementAtPathWithText("dd", "name")
      doc must haveElementAtPathWithText("dd", "scon")
      doc must haveElementAtPathWithText("dd", "nino")
    }

    "have a valid submit button" in {
      doc must haveSubmitButton(messages("gmp.button.request-another"))
    }

    "have a valid print page link" in {
      doc must haveLinkWithText(messages("gmp.print"))
    }

  }
}
