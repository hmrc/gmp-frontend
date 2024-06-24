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

import cats.syntax.either._
import models.{GmpDate, Leaving}
import play.api.data.FormError
import play.api.data.format.Formatter
import play.api.mvc.Call

import java.time.LocalDate
import scala.util.{Left, Try}

class GMPDateFormatter(maximumDateInclusive: Option[LocalDate],
                       minimumDateInclusive: Option[LocalDate],
                       dayKey: String,
                       monthKey: String,
                       yearKey: String,
                       dateKey: String,
                       tooRecentArgs: Seq[String] = Seq.empty,
                       tooFarInPastArgs: Seq[String] = Seq.empty,
                       onlyRequiredIf: Option[Map[String, String] => Boolean] = None) extends Formatter[GmpDate] {
  val YEAR_FIELD_LENGTH: Int = 4

  def isValidDate(x: GmpDate): Boolean =
    try {
      x.getAsLocalDate.isDefined
    } catch {
      case e: Throwable => false
    }

  def dateFieldStringValues(
                             data: Map[String, String]
                           ): Either[Seq[FormError], (String, String, String)] =
    List(dayKey, monthKey, yearKey)
      .map(data.get(_).map(_.trim).filter(_.nonEmpty)) match {
      case Some(dayString) :: Some(monthString) :: Some(yearString) :: Nil =>
        Right((dayString, monthString, yearString))
      case None :: Some(_) :: Some(_) :: Nil =>
        Left(Seq(FormError(dayKey, "error.dayRequired")))
      case Some(_) :: None :: Some(_) :: Nil =>
        Left(Seq(FormError(monthKey, "error.monthRequired")))
      case Some(_) :: Some(_) :: None :: Nil =>
        Left(Seq(FormError(yearKey, "error.yearRequired")))
      case Some(_) :: None :: None :: Nil =>
        val errorMessage = "error.monthAndYearRequired"
        Left(Seq(FormError(monthKey, errorMessage), FormError(yearKey, errorMessage)))
      case None :: Some(_) :: None :: Nil =>
        val errorMessage = "error.dayAndYearRequired"
        Left(Seq(FormError(dayKey, errorMessage), FormError(yearKey, errorMessage)))
      case None :: None :: Some(_) :: Nil =>
        val errorMessage = "error.dayAndMonthRequired"
        Left(Seq(FormError(dayKey, errorMessage), FormError(monthKey, errorMessage)))
      case _ =>
        Left(Seq(FormError(dateKey, "error.required")))
    }

  def toValidInt(
                  stringValue: String,
                  maxValue: Option[Int],
                  key: String
                ): Either[FormError, Int] =
    Either.fromOption(
      Try(BigDecimal(stringValue).toIntExact).toOption.filter(i => i > 0 && maxValue.forall(i <= _)),
      FormError(key, "error.invalid")
    )

  def bind(
            key: String,
            data: Map[String, String]
          ): Either[Seq[FormError], GmpDate] = {
    if(onlyRequiredIf.isEmpty || onlyRequiredIf.get.apply(data)) {
      for {
        dateFields <- dateFieldStringValues(data)
        (dayStr, monthStr, yearStr) = dateFields
        month <- toValidInt(monthStr, Some(12), monthKey).leftMap(Seq(_))
        year <- toValidInt(yearStr, None, yearKey).leftMap(Seq(_))
        date <- toValidInt(dayStr, Some(31), dayKey).leftMap(Seq(_))
          .flatMap(_ =>
            Either
              .fromTry(Try(GmpDate(Some(dayStr), Some(monthStr), Some(yearStr))))
              .leftMap(_ => Seq(FormError(dateKey, "error.invalid")))
          )
        _ <-
          if (yearStr.nonEmpty && (yearStr.length < YEAR_FIELD_LENGTH || yearStr.length > YEAR_FIELD_LENGTH)) {
            Left(Seq(FormError(yearKey, "error.yearLength")))
          } else if (!isValidDate(date)) {
            Left(Seq(FormError(dateKey, "error.invalid")))
          } else if (maximumDateInclusive.exists(_.isBefore(LocalDate.of(year, month, dayStr.toInt)))) {
            Left(Seq(FormError(dateKey, "error.tooFuture", tooRecentArgs)))
          } else if (minimumDateInclusive.exists(_.isAfter(LocalDate.of(year, month, dayStr.toInt)))) {
            Left(Seq(FormError(dateKey, "error.tooFarInPast", tooFarInPastArgs)))
          } else {
            Right(date)
          }

      } yield date
    } else {
      Right(GmpDate.emptyDate)
    }

  }

  def unbind(key: String, value: GmpDate): Map[String, String] =
    Map(
      dayKey -> value.day.getOrElse(""),
      monthKey -> value.month.getOrElse(""),
      yearKey -> value.year.getOrElse("")
    )

}
