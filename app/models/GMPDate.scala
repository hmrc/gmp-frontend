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

package models

import java.time.LocalDate
import play.api.libs.json.{Json, OFormat}

import java.time.format.DateTimeFormatter

case class GmpDate(day: Option[String], month: Option[String], year: Option[String]) {

  def getAsLocalDate: Option[LocalDate] = {
    (day, month, year) match {
      case (Some(d), Some(m), Some(y)) => Some(LocalDate.of(d.toInt, m.toInt, y.toInt))
      case _ => None
    }
  }

  def isOnOrAfter06042016: Boolean = {
    getAsLocalDate.exists(date => {
      val thatDate = LocalDate.of(2016, 4, 6)
      date.isBefore(thatDate) || date.isEqual(thatDate)
    })
  }

  def isOnOrAfter05041978: Boolean = {
    getAsLocalDate.exists(date => {
      val thatDate = LocalDate.of(1978, 4, 5)
      date.isBefore(thatDate) || date.isEqual(thatDate)
    })
  }

  def isBefore05042046: Boolean = getAsLocalDate.exists(_.isBefore(LocalDate.of(2046, 4, 5)))

  def getAsText: String = {
    getAsLocalDate match {
      case Some(date) => date.format( DateTimeFormatter.ofPattern("dd MMMM yyyy"))
      case _ => ""
    }
  }

  def isBefore(otherDate: GmpDate): Boolean = {
    (getAsLocalDate, otherDate.getAsLocalDate) match {
      case (Some(date), Some(otherDate)) => date.isBefore(otherDate)
      case _ => false
    }
  }
}

object GmpDate {
  val emptyDate = GmpDate(None, None, None)
  implicit val formats: OFormat[GmpDate] = Json.format[GmpDate]
}
