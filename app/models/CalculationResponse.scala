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
import uk.gov.hmrc.time.TaxYearResolver
import play.api.libs.json.Json

case class ContributionsAndEarnings(taxYear: Int, contEarnings: String)

object ContributionsAndEarnings {
  implicit val formats = Json.format[ContributionsAndEarnings]
}

case class CalculationPeriod(startDate: Option[LocalDate],
                             endDate: LocalDate,
                             gmpTotal: String,
                             post88GMPTotal: String,
                             revaluationRate: Int,
                             errorCode: Int,
                             revalued: Option[Int],
                             dualCalcPost90TrueTotal: Option[String] = None,
                             dualCalcPost90OppositeTotal: Option[String] = None,
                             inflationProofBeyondDod: Option[Int] = None,
                             contsAndEarnings: Option[List[ContributionsAndEarnings]] = None
                              ){
  def endTaxYear : Int = {
    CalculationResponse.getTaxYear(Some(endDate))
  }

  def startTaxYear : Int = {
    CalculationResponse.getTaxYear(startDate)
  }

}

object CalculationPeriod {
  implicit val formats = Json.format[CalculationPeriod]
}

case class CalculationResponse(
                                name: String,
                                nino: String,
                                scon: String,
                                revaluationRate: Option[String],
                                revaluationDate: Option[LocalDate],
                                calculationPeriods: List[CalculationPeriod],
                                globalErrorCode: Int,
                                spaDate: Option[LocalDate],
                                payableAgeDate: Option[LocalDate],
                                dateOfDeath: Option[LocalDate],
                                dualCalc: Boolean,
                                calcType: Int) {


  def leavingDate : LocalDate = {
    if (revaluationDate.isDefined) revaluationDate.get
    else calculationPeriods.head.endDate
  }

  def hasErrors: Boolean = calculationPeriods.foldLeft(globalErrorCode){_ + _.errorCode} > 0

  def revaluationUnsuccessful: Boolean = calculationPeriods.foldLeft(0){_ + _.revalued.getOrElse(0)} == calculationPeriods.length

  def totalGmp: BigDecimal = calculationPeriods.foldLeft(BigDecimal(0)){ (sum, period) => sum + BigDecimal(period.gmpTotal)}

  def post88Gmp: BigDecimal = calculationPeriods.foldLeft(BigDecimal(0)){ (sum, period) => sum + BigDecimal(period.post88GMPTotal)}

  def errorCodes: List[Int] = {
    hasErrors match {
      case false => List[Int]()
      case true =>
        var errors = calculationPeriods
            .filter(_.errorCode > 0)
            .map(_.errorCode)
        if (globalErrorCode > 0)
          errors = errors :+ globalErrorCode

        errors
    }
  }

  def numPeriodsInError: Int = {
      calculationPeriods.filter(_.errorCode > 0).size
  }


  def dodInSameTaxYearAsRevaluationDate: Boolean = {

    if(CalculationResponse.getTaxYear(dateOfDeath) != 0 &&
       CalculationResponse.getTaxYear(dateOfDeath) == CalculationResponse.getTaxYear(revaluationDate)) {
      true
    }
    else
      false

  }


}

object CalculationResponse {
  implicit val formats = Json.format[CalculationResponse]

  def getTaxYear(date: Option[LocalDate]): Int = {
    date match {
      case Some(d) => TaxYearResolver.taxYearFor(d)
      case _ => 0
    }
  }
}
