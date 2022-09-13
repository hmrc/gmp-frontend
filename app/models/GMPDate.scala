/*
 * Copyright 2022 HM Revenue & Customs
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

import org.joda.time.LocalDate
import play.api.libs.json.Json

case class GmpDate(day: Option[String], month: Option[String], year: Option[String]) {

  def getAsLocalDate: Option[LocalDate] = {

    if (day.isDefined && month.isDefined && year.isDefined)
      Some(new LocalDate(year.get.toInt, month.get.toInt, day.get.toInt))
    else
      None

  }

  def isOnOrAfter06042016: Boolean = {
    if (getAsLocalDate.isDefined) {
      val thatDate = new LocalDate(2016, 4, 6)
      getAsLocalDate.get.isAfter(thatDate) || getAsLocalDate.get.isEqual(thatDate)
    }
    else
      false
  }

  def isOnOrAfter05041978: Boolean = {
    if (getAsLocalDate.isDefined) {
      val thatDate = new LocalDate(1978, 4, 5)
      getAsLocalDate.get.isAfter(thatDate) || getAsLocalDate.get.isEqual(thatDate)
    }
    else
      false
  }

  def isBefore05042046: Boolean = {
    if (getAsLocalDate.isDefined) {
      val thatDate = new LocalDate(2046, 4, 5)
      getAsLocalDate.get.isBefore(thatDate)
    }
    else
      false
  }

  def getAsText: String = {
    getAsLocalDate match {
      case Some(date) => date.toString("dd MMMM yyyy")
      case _ => ""
    }
  }

  def isBefore(date2: GmpDate): Boolean = {
    if (getAsLocalDate.isDefined && date2.getAsLocalDate.isDefined) {
      getAsLocalDate.get.isBefore(date2.getAsLocalDate.get)
    }
    else{
      false
    }
  }
}

object GmpDate {
  implicit val formats = Json.format[GmpDate]
}
