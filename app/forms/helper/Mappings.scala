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

package forms.helper

import models.GmpDate
import play.api.data.FieldMapping
import play.api.data.Forms.of
import play.api.mvc.Call

import java.time.LocalDate

trait Mappings {
  protected def gmpDate(
                         maximumDateInclusive: Option[LocalDate],
                         minimumDateInclusive: Option[LocalDate],
                         dayKey: String,
                         monthKey: String,
                         yearKey: String,
                         dateKey: String,
                         tooRecentArgs: Seq[String] = Seq.empty,
                         tooFarInPastArgs: Seq[String] = Seq.empty,
                         onlyRequiredIf: Option[Map[String, String] => Boolean] = None): FieldMapping[GmpDate] =

    of(new GMPDateFormatter(maximumDateInclusive, minimumDateInclusive, dayKey, monthKey, yearKey, dateKey, tooRecentArgs, tooFarInPastArgs, onlyRequiredIf))
}

