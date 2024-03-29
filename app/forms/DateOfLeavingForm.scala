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

package forms

import com.google.inject.Singleton
import forms.helper.Mappings
import models.Leaving
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{Messages, MessagesImpl}
import play.api.mvc.MessagesControllerComponents

import java.time.LocalDate
import javax.inject.Inject


@Singleton
class DateOfLeavingForm @Inject()(mcc: MessagesControllerComponents) extends Mappings {
  implicit lazy val messages: Messages = MessagesImpl(mcc.langs.availables.head, mcc.messagesApi)

  def dateCondition(data: Map[String, String]): Boolean = data.get("leaving").contains(Leaving.YES_AFTER)

  def dateOfLeavingForm(minYear: Int = 2016, maxYear: Int = 2046) = {
    Form(mapping(
      "leavingDate" -> gmpDate(
        maximumDateInclusive = Some(LocalDate.of(maxYear, 4, 5)),
        minimumDateInclusive = Some(LocalDate.of(minYear, 4, 6)),
        "leavingDate.day",
        "leavingDate.month",
        "leavingDate.year",
        "leavingDate",
        tooRecentArgs = Seq("5 April " + maxYear.toString),
        tooFarInPastArgs = Seq("6 April " + minYear.toString),
        onlyRequiredIf = Some(dateCondition)
      ),
      "leaving" -> optional(text).verifying("error.required", {
        _.isDefined
      }))(Leaving.apply)(Leaving.unapply))
  }

}

