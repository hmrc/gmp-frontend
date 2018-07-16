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

class ResultsSpec extends GmpViewSpec{
  override def view: Html = views.html.results(applicationConfig, calculationResponse, Some("revalRateSubheader"), Some("survivorSubheader"))

  private val applicationConfig: config.ApplicationConfig = config.ApplicationConfig
  private val calculationResponse : CalculationResponse = CalculationResponse(
    "name", "nino", "scon", Some("revaluationRate"), Some(LocalDate.now),
    List(CalculationPeriod(Some(LocalDate.now), LocalDate.now(), "gmpTotal", "post", 1, 2, Some(3), Some("string"),
      Some("string2"), Some(4), Some(List(ContributionsAndEarnings(2018, "2000")))),CalculationPeriod(Some(LocalDate.now), LocalDate.now(),"gmpTotal","post", 1, 2, Some(3), Some("string"),
      Some("string2"), Some(4), Some(List(ContributionsAndEarnings(2018, "2000"))))),
    0, Some(LocalDate.now), Some(LocalDate.now), Some(LocalDate.now), true, 2)

  "Results page" must {
    behave like pageWithTitle(messages("gmp.results.title"))

    "have a span" in {
      print(doc.body())
      doc must haveParagraphWithText(messages("If you disagree with this calculation, contact HM Revenue and Customs by creating a new entry in the single queries database in Shared Workspace's eRoom."))
    }
  }


}