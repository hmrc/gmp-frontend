/*
 * Copyright 2023 HM Revenue & Customs
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
import forms.DateOfLeavingForm.Fields.{leavingDate, radioButtons}

import scala.util.{Failure, Success, Try}
import javax.inject.Inject
import models.{GmpDate, Leaving}
import play.api.data.{Form, FormError}
import play.api.data.Forms.{mapping, of}
import play.api.data.Forms._
import cats.syntax.either._

import java.time.LocalDate
import play.api.data.format.Formatter
import play.api.i18n.{Messages, MessagesImpl}
import play.api.mvc.MessagesControllerComponents


@Singleton
class DateOfLeavingForm  @Inject()(mcc: MessagesControllerComponents) {
  implicit lazy val messages: Messages = MessagesImpl(mcc.langs.availables.head, mcc.messagesApi)

  val YEAR_FIELD_LENGTH: Int = 4

  def dateMustBePresentIfHaveDateOfLeavingAfterApril2016(x: Leaving): Boolean = {
    if (x.leaving.contains(Leaving.YES_AFTER)) {
      x.leavingDate.day.nonEmpty &&
        x.leavingDate.month.nonEmpty && x.leavingDate.year.nonEmpty
    } else {
      false
    }
  }
  def isValidDate(x: GmpDate): Boolean =
    try {
      x.getAsLocalDate.isDefined
    } catch {
      case e: Throwable => false
    }

  def leavingDateFormatter(
                            maximumDateInclusive: Option[LocalDate],
                            minimumDateInclusive: Option[LocalDate],
                            dayKey: String,
                            monthKey: String,
                            yearKey: String,
                            dateKey: String,
                            tooRecentArgs: Seq[String] = Seq.empty,
                            tooFarInPastArgs: Seq[String] = Seq.empty): Formatter[GmpDate] =
    new Formatter[GmpDate] {
      def dateFieldStringValues(
                                 data: Map[String, String]
                               ): Either[Seq[FormError], (String, String, String)] =
        List(dayKey, monthKey, yearKey)
          .map(data.get(_).map(_.trim).filter(_.nonEmpty)) match {
          case Some(dayString) :: Some(monthString) :: Some(yearString) :: Nil =>
            Right((dayString, monthString, yearString))
          case None :: Some(_) :: Some(_) :: Nil                               =>
            Left(Seq(FormError(dayKey, "error.dayRequired")))
          case Some(_) :: None :: Some(_) :: Nil                               =>
            Left(Seq(FormError(monthKey, "error.monthRequired")))
          case Some(_) :: Some(_) :: None :: Nil                               =>
            Left(Seq(FormError(yearKey, "error.yearRequired")))
          case Some(_) :: None :: None :: Nil                                  =>
            val errorMessage = "error.monthAndYearRequired"
            Left(Seq(FormError(monthKey, errorMessage), FormError(yearKey, errorMessage)))
          case None :: Some(_) :: None :: Nil                                  =>
            val errorMessage = "error.dayAndYearRequired"
            Left(Seq(FormError(dayKey, errorMessage), FormError(yearKey, errorMessage)))
          case None :: None :: Some(_) :: Nil                                  =>
            val errorMessage = "error.dayAndMonthRequired"
            Left(Seq(FormError(dayKey, errorMessage), FormError(monthKey, errorMessage)))
          case _                                                               =>
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

    override def bind(
                       key: String,
                       data: Map[String, String]
                     ): Either[Seq[FormError], GmpDate] = {
      if(data.get("leaving").contains(Leaving.YES_AFTER)) {
        for {
          dateFields                 <- dateFieldStringValues(data)
          (dayStr, monthStr, yearStr) = dateFields
          month                      <- toValidInt(monthStr, Some(12), monthKey).leftMap(Seq(_))
          year                       <- toValidInt(yearStr, None, yearKey).leftMap(Seq(_))
          date                       <- toValidInt(dayStr, Some(31), dayKey).leftMap(Seq(_))
            .flatMap(_ =>
              Either
                .fromTry(Try(GmpDate(Some(dayStr), Some(monthStr), Some(yearStr))))
                .leftMap(_ => Seq(FormError(dateKey, "error.invalid")))
            )
          _                          <-
            if (yearStr.nonEmpty && (yearStr.length < YEAR_FIELD_LENGTH || yearStr.length > YEAR_FIELD_LENGTH))
              Left(Seq(FormError(yearKey, "error.yearLength")))
            else if (!isValidDate(date))
              Left(Seq(FormError(dateKey, "error.invalid")))
            else if (maximumDateInclusive.exists(_.isBefore(LocalDate.of(year, month, dayStr.toInt))))
              Left(Seq(FormError(dateKey, "error.tooFuture", tooRecentArgs)))
            else if (minimumDateInclusive.exists(_.isAfter(LocalDate.of(year, month, dayStr.toInt))))
              Left(Seq(FormError(dateKey, "error.tooFarInPast", tooFarInPastArgs)))
            else
              Right(date)

        } yield date
      } else {
        Right(GmpDate.emptyDate)
      }

    }

      override def unbind(key: String, value: GmpDate): Map[String, String] =
      Map(
        dayKey   -> value.day.getOrElse(""),
        monthKey -> value.month.getOrElse(""),
        yearKey  -> value.year.getOrElse("")
      )
  }


  def dateOfLeavingForm(minYear: Int = 2016, maxYear: Int = 2046) = Form(
    mapping(
      "leavingDate" -> of(leavingDateFormatter(
          maximumDateInclusive = Some(LocalDate.of(maxYear, 4, 5)),
          minimumDateInclusive = Some(LocalDate.of(minYear,4,6)),
          "leavingDate.day",
          "leavingDate.month",
          "leavingDate.year",
          "leavingDate",
          tooRecentArgs = Seq("5 April " + maxYear.toString),
          tooFarInPastArgs = Seq("6 April " + minYear.toString)
        )),
      "leaving" -> optional(text).verifying("error.required",{_.isDefined})
      )(Leaving.apply)(Leaving.unapply)
  )

}

object DateOfLeavingForm {
  object Fields {
    val radioButtons = "leaving"
    val leavingDate = "leavingDate"
  }
}

