/*
 * Copyright 2016 HM Revenue & Customs
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

case class CalculationRequestLine (scon: String,
                                  nino: String,
                                  firstForename: String,
                                  surname: String,
                                  memberReference: Option[String],
                                  calctype: Option[Int],
                                  terminationDate: Option[String] = None,
                                  revaluationDate: Option[String] = None,
                                  revaluationRate: Option[Int] = None,
                                  dualCalc: Int
                                  ) {
  override def toString = {
    val dc = dualCalc match {
      case 1 => "Y"
      case 0 => "N"
      case _ => ""
    }

    s"""$scon,$nino,$firstForename,$surname,${memberReference.getOrElse("")},${calctype.getOrElse("")},${terminationDate.getOrElse("")},${revaluationDate.getOrElse("")},${revaluationRate.getOrElse("")},$dc"""
  }
}

object CalculationRequestLine {
  implicit val formats = Json.format[CalculationRequestLine]
}

case class BulkCalculationRequestLine(lineId: Int,
                                      validCalculationRequest: Option[CalculationRequestLine],
                                      globalError: Option[String],
                                      validationErrors: Option[Map[String,String]]
                                     )

object BulkCalculationRequestLine {
  implicit val formats = Json.format[BulkCalculationRequestLine]
}

case class BulkCalculationRequest(uploadReference: String,
                                  email: String,
                                  reference: String,
                                  calculationRequests: List[BulkCalculationRequestLine],
                                  userId: String = "",
                                  timestamp: LocalDate = LocalDate.now()
                                 )

object BulkCalculationRequest {
  implicit val formats = Json.format[BulkCalculationRequest]
}

