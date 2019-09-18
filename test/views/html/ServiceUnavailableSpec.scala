/*
 * Copyright 2019 HM Revenue & Customs
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

class ServiceUnavailableSpec extends GmpViewSpec{
  override def view: Html = views.html.service_unavailable()

  "ServiceUnavailable page" must {
    behave like pageWithTitle("Sorry, there is a problem with the service - Guaranteed Minimum Pension - GOV.UK")
    behave like pageWithHeader(messages("gmp.serviceunavailable.title"))

  }
}
