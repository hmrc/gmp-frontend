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

import models.GmpDate
import validation.DateValidate

package object forms {

  val YEAR_FIELD_LENGTH: Int = 4
  val nameRegex = "^[-a-zA-Z'][-a-zA-Z' ]+$".r


  def checkValidDate(data: GmpDate): Boolean = {

    def checkValidDate(day: Option[String], month: Option[String], year: Option[String]): Boolean = {
      val tDay: String = day.getOrElse("")
      val tMonth: String = month.getOrElse("")
      val tYear: String = year.getOrElse("")
      if ((tDay.length + tMonth.length + tYear.length) == 0) {
        true
      } else {
        DateValidate.isValid(tDay + "/" + tMonth + "/" + tYear)
      }
    }

    val day = data.day
    val month = data.month
    val year = data.year
    checkValidDate(day, month, year)

  }

  def checkYearLength(year: Option[String]): Boolean = {
    year match {
      case Some(value) =>  if (value forall Character.isDigit) {
        value.length == YEAR_FIELD_LENGTH
      }else{
        true
      }
      case None => true
    }
  }

  def checkDateOnOrAfterGMPStart(data: GmpDate): Boolean = {
    def checkDateOnOrAfterGMPStart(day: Option[String], month: Option[String], year: Option[String]): Boolean = {
      val tDay: String = day.getOrElse("")
      val tMonth: String = month.getOrElse("")
      val tYear: String = year.getOrElse("")
      if ((tDay.length + tMonth.length + tYear.length) == 0) {
        true
      } else {
        DateValidate.isOnOrAfterGMPStart(tDay + "/" + tMonth + "/" + tYear)
      }
    }

    val day = data.day
    val month = data.month
    val year = data.year
    checkDateOnOrAfterGMPStart(day, month, year)
  }

  def checkDateOnOBeforeGMPEnd(data: GmpDate): Boolean = {
    def checkDateOnOBeforeGMPEnd(day: Option[String], month: Option[String], year: Option[String]): Boolean = {
      val tDay: String = day.getOrElse("")
      val tMonth: String = month.getOrElse("")
      val tYear: String = year.getOrElse("")
      if ((tDay.length + tMonth.length + tYear.length) == 0) {
        true
      } else {
        DateValidate.isOnOrBeforeGMPEnd(tDay + "/" + tMonth + "/" + tYear)
      }
    }

    val day = data.day
    val month = data.month
    val year = data.year
    checkDateOnOBeforeGMPEnd(day, month, year)

  }

  def checkForNumber(optionValue: Option[String]): Boolean = {
    optionValue match {
      case Some(value) => value forall Character.isDigit
      case None => true
    }
  }

  def checkDayRange(optionValue: Option[String]): Boolean = {
    optionValue match {
      case Some(value) => {
        if (value forall Character.isDigit) {
          val number = value.toInt
          number > 0 && number < 32
        } else {
          true
        }
      }
      case None => true
    }
  }

  def checkMonthRange(optionValue: Option[String]): Boolean = {
    optionValue match {
      case Some(value) => {
        if (value forall Character.isDigit) {
          val number = value.toInt
          number > 0 && number < 13
        } else {
          true
        }
      }
      case None => true
    }
  }
}
