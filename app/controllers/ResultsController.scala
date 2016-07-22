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

package controllers

import config.{ApplicationGlobal, GmpFrontendAuthConnector}
import connectors.GmpConnector
import controllers.auth.GmpRegime
import events.{ContributionsAndEarningsEvent, ExitQuestionnaireEvent}
import metrics.Metrics
import models._
import org.joda.time.LocalDate
import play.api.Logger
import play.api.i18n.Messages
import play.api.mvc.Request
import play.twirl.api.HtmlFormat
import services.SessionService
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

trait ResultsController extends GmpPageFlow {

  val sessionService: SessionService
  val calculationConnector: GmpConnector
  val auditConnector : AuditConnector = ApplicationGlobal.auditConnector

  def resultsView(response: CalculationResponse, subheader: Option[String])(implicit request: Request[_]): HtmlFormat.Appendable

  def metrics: Metrics

  def get = AuthorisedFor(GmpRegime, pageVisibilityPredicate).async {
    implicit user =>
      implicit request =>{

        sessionService.fetchGmpSession() flatMap {
          sessionOpt: Option[GmpSession] =>
            sessionOpt match {
              case Some(session) =>
                calculationConnector.calculateSingle(createCalculationRequest(session)) map {
                  response:CalculationResponse => {
                    if (response.globalErrorCode != 0) metrics.countNpsError(response.globalErrorCode.toString)
                    for (period <- response.calculationPeriods) {
                      if (period.errorCode != 0) metrics.countNpsError(period.errorCode.toString)
                    }

                    val subhead = subheader(response,session.leaving)

                    Ok(resultsView(response, subhead))
                  }
                }
              case _ => throw new RuntimeException
            }
        }
      }
  }


  def getContributionsAndEarnings = AuthorisedFor(GmpRegime, pageVisibilityPredicate).async {
    implicit user =>
      implicit request => {
        sessionService.fetchGmpSession() flatMap {
          sessionOpt: Option[GmpSession] =>
            sessionOpt match {
              case Some(session) =>
                calculationConnector.calculateSingle(createCalculationRequest(session)).map {
                  response: CalculationResponse => {
                    if (response.globalErrorCode != 0) metrics.countNpsError(response.globalErrorCode.toString)
                    for (period <- response.calculationPeriods) {
                      if (period.errorCode != 0) metrics.countNpsError(period.errorCode.toString)
                    }

                    val contsAndEarningsResult = auditConnector.sendEvent(new ContributionsAndEarningsEvent(calculationConnector.getUser(user), response.nino))
                    contsAndEarningsResult.onFailure {
                      case e: Throwable => Logger.warn("[ResultsController][post] : contsAndEarningsResult: " + e.getMessage(), e)
                    }

                    Ok(views.html.contributions_earnings(response))
                  }
                }
              case _ => throw new RuntimeException
            }
        }
      }
  }

  def createCalculationRequest(gmpSession: GmpSession): CalculationRequest = {

    CalculationRequest(
      gmpSession.scon,
      gmpSession.memberDetails.nino.replaceAll("\\s", ""),
      gmpSession.memberDetails.surname,
      gmpSession.memberDetails.firstForename,
      gmpSession.scenario.toInt,
      gmpSession.revaluationDate match {
        case Some(rDate: GmpDate) if (rDate.day.isDefined && rDate.month.isDefined && rDate.year.isDefined) =>
          Some(new LocalDate(rDate.year.get.toInt, rDate.month.get.toInt, rDate.day.get.toInt))
        case _ => None
      }, gmpSession.rate match {
        case Some(RevaluationRate.HMRC) => Some(0)
        case Some(RevaluationRate.S148) => Some(1)
        case Some(RevaluationRate.FIXED) => Some(2)
        case Some(RevaluationRate.LIMITED) => Some(3)
        case _ => None
      }, Some(1),
      gmpSession.equalise,
      gmpSession.leaving.leavingDate match {
        case lDate: GmpDate if (lDate.day.isDefined && lDate.month.isDefined && lDate.year.isDefined) =>
          Some(new LocalDate(lDate.year.get.toInt, lDate.month.get.toInt, lDate.day.get.toInt))
        case _ => None
      })
  }

  private def subheader(response: CalculationResponse, leaving:Leaving): Option[String] = {
    response.calcType match {

      case 0 => {
        if(response.calculationPeriods.length > 1)
          Some(Messages("gmp.notrevalued.multi.subheader"))
        else
          Some(Messages("gmp.notrevalued.subheader"))
      }

      case 1 => {
        if(response.revaluationUnsuccessful)
          Some(Messages("gmp.notrevalued.subheader"))
        else if(response.revaluationRate.isDefined){
          if (response.revaluationRate == Some("0"))
            Some(Messages("gmp.reval_rate.subheader", Messages(s"gmp.revaluation_rate.type_${response.revaluationRate.get}")) + " (" + Messages(s"gmp.revaluation_rate.type_${response.calculationPeriods.head.revaluationRate}") + ").")
          else
            Some(Messages("gmp.reval_rate.subheader", Messages(s"gmp.revaluation_rate.type_${response.revaluationRate.get}")) + ".")
        }
        else None
      }

      case 2 | 4 => {
        leaving.leaving match{
          case Some(Leaving.NO) => None
          case _ => determineRevalRateSubheader(response)
        }

      }

      case 3 => {
        if(response.calculationPeriods.head.inflationProofBeyondDod == Some(0) && response.dodInSameTaxYearAsRevaluationDate)
          Some(Messages("gmp.no_inflation.subheader"))
        else if(response.calculationPeriods.head.endDate.isBefore(LocalDate.now()))
          determineRevalRateSubheader(response)
        else None
      }

    }
  }

  private def determineRevalRateSubheader(response: CalculationResponse): Some[String] = {
    if (response.revaluationRate.isDefined && response.revaluationRate == Some("0"))
      Some(Messages("gmp.chosen_rate.subheader", Messages(s"gmp.revaluation_rate.type_${response.revaluationRate.get}")) + " (" + Messages(s"gmp.revaluation_rate.type_${response.calculationPeriods.head.revaluationRate}") + ").")
    else if(response.revaluationRate.isDefined)
      Some(Messages("gmp.chosen_rate.subheader", Messages(s"gmp.revaluation_rate.type_${response.calculationPeriods.head.revaluationRate}")) + ".")
    else
      Some(Messages("gmp.held_rate.subheader", Messages(s"gmp.revaluation_rate.type_${response.calculationPeriods.head.revaluationRate}")) + ".")
  }

}


object ResultsController extends ResultsController {
  val authConnector = GmpFrontendAuthConnector
  override val calculationConnector = GmpConnector

  // $COVERAGE-OFF$Trivial and never going to be called by a test that uses it's own object implementation
  override def metrics = Metrics

  override def resultsView(response: CalculationResponse, subheader: Option[String])(implicit request: Request[_]): HtmlFormat.Appendable = {
    views.html.results(applicationConfig = config.ApplicationConfig, response, subheader)
  }

  // $COVERAGE-ON$
}
