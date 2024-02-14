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

package controllers

import com.google.inject.{Inject, Singleton}
import config.{ApplicationConfig, GmpContext, GmpSessionCache}
import connectors.GmpConnector
import controllers.auth.AuthAction
import events.ContributionsAndEarningsEvent
import metrics.ApplicationMetrics
import models._
import java.time.LocalDate
import play.api.Logging
import play.api.i18n.Messages
import play.api.mvc.{MessagesControllerComponents, Request}
import play.twirl.api.HtmlFormat
import services.SessionService
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import views.Views

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ResultsController @Inject()(authAction: AuthAction,
                                  override val authConnector: AuthConnector,
                                  sessionService: SessionService,
                                  implicit override val context: GmpContext,
                                  calculationConnector: GmpConnector,
                                  auditConnector: AuditConnector,
                                  metrics: ApplicationMetrics,
                                  ac:ApplicationConfig,
                                  override val messagesControllerComponents: MessagesControllerComponents,
                                  implicit val executionContext: ExecutionContext,
                                  implicit val gmpSessionCache: GmpSessionCache,
                                  views: Views) extends GmpPageFlow(authConnector,sessionService,context,messagesControllerComponents,ac) with Logging{



   def resultsView(response: CalculationResponse, revalRateSubheader: Option[String], survivorSubheader: Option[String])(implicit request: Request[_]): HtmlFormat.Appendable = {
    views.results(response, revalRateSubheader,survivorSubheader)
  }

  def get = authAction.async {
      implicit request => {
        val link = request.linkId
        sessionService.fetchGmpSession() flatMap {
          sessionOpt: Option[GmpSession] =>
            sessionOpt match {
              case Some(session) => session match {
                case _ if session.scon == "" => Future.successful(Ok(views.failure(Messages("gmp.error.session_parts_missing", "/guaranteed-minimum-pension/pension-details"), Messages("gmp.cannot_calculate.gmp"), Messages("gmp.session_missing.title"))))
                case _ if session.memberDetails.nino == "" || session.memberDetails.firstForename == "" || session.memberDetails.surname == "" => Future.successful(Ok(views.failure(Messages("gmp.error.session_parts_missing", "/guaranteed-minimum-pension/member-details"), Messages("gmp.cannot_calculate.gmp"), Messages("gmp.session_missing.title"))))
                case _ if session.scenario == "" => Future.successful(Ok(views.failure(Messages("gmp.error.session_parts_missing", "/guaranteed-minimum-pension/calculation-reason"), Messages("gmp.cannot_calculate.gmp"), Messages("gmp.session_missing.title"))))
                case _ if session.leaving.leaving.isEmpty => Future.successful(Ok(views.failure(Messages("gmp.error.session_parts_missing", "/guaranteed-minimum-pension/left-scheme"), Messages("gmp.cannot_calculate.gmp"), Messages("gmp.session_missing.title"))))
                case _ =>
                  val calcRequest = createCalculationRequest(session)
                  calculationConnector.calculateSingle(calcRequest, link) map { response: CalculationResponse => {
                    if (response.globalErrorCode != 0) metrics.countNpsError(response.globalErrorCode.toString)
                    for (period <- response.calculationPeriods) {
                      if (period.errorCode != 0) metrics.countNpsError(period.errorCode.toString)
                    }

                    Ok(resultsView(response, revalRateSubheader(response, session.leaving), survivorSubheader(session, response)))
                  }}
              }
              case _ => Future.successful(Ok(views.failure(Messages("gmp.error.session_parts_missing", "/guaranteed-minimum-pension/dashboard"), Messages("gmp.cannot_calculate.gmp"), Messages("gmp.session_missing.title"))))
            }
        }
      }
  }

  def getContributionsAndEarnings = authAction.async {
      implicit request => {
        val link = request.linkId

        sessionService.fetchGmpSession() flatMap {
          sessionOpt: Option[GmpSession] =>
            sessionOpt match {
              case Some(session) => session match {
                case _ if session.scon == "" => Future.successful(Ok(views.failure(Messages("gmp.error.session_parts_missing", "/guaranteed-minimum-pension/pension-details"), Messages("gmp.cannot_calculate.gmp"), Messages("gmp.session_missing.title"))))
                case _ if session.memberDetails.nino == "" || session.memberDetails.firstForename == "" || session.memberDetails.surname == "" => Future.successful(Ok(views.failure(Messages("gmp.error.session_parts_missing", "/guaranteed-minimum-pension/member-details"), Messages("gmp.cannot_calculate.gmp"), Messages("gmp.session_missing.title"))))
                case _ if session.scenario == "" => Future.successful(Ok(views.failure(Messages("gmp.error.session_parts_missing", "/guaranteed-minimum-pension/calculation-reason"), Messages("gmp.cannot_calculate.gmp"), Messages("gmp.session_missing.title"))))
                case _ if !session.leaving.leaving.isDefined => Future.successful(Ok(views.failure(Messages("gmp.error.session_parts_missing", "/guaranteed-minimum-pension/left-scheme"), Messages("gmp.cannot_calculate.gmp"), Messages("gmp.session_missing.title"))))
                case _ =>
                  val calcRequest = createCalculationRequest(session)
                  calculationConnector.calculateSingle(calcRequest, link) map { response: CalculationResponse => {
                    if (response.globalErrorCode != 0) metrics.countNpsError(response.globalErrorCode.toString)
                    for (period <- response.calculationPeriods) {
                      if (period.errorCode != 0) metrics.countNpsError(period.errorCode.toString)
                    }

                    val contsAndEarningsResult = auditConnector.sendEvent(new ContributionsAndEarningsEvent(link, response.nino))

                    contsAndEarningsResult.failed.foreach({
                      case e: Throwable => logger.error(s"[ResultsController][post] contsAndEarningsResult ${e.getMessage}", e)
                    })

                    Ok(views.contributionsEarnings(response))
                  }}
              }
              case _ => Future.successful(Ok(views.failure(Messages("gmp.error.session_parts_missing", "/guaranteed-minimum-pension/dashboard"), Messages("gmp.cannot_calculate.gmp"), Messages("gmp.session_missing.title"))))
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


  private def revalRateSubheader(response: CalculationResponse, leaving:Leaving): Option[String] = {

    if(response.calculationPeriods.nonEmpty) {

      response.calcType match {
        case 0 => {
          if (response.calculationPeriods.length > 1)
            Some(Messages("gmp.notrevalued.multi.subheader"))
          else
            Some(Messages("gmp.notrevalued.subheader"))
        }

        case 1 => {
          if (response.revaluationUnsuccessful)
            Some(Messages("gmp.notrevalued.subheader"))
          else if (response.revaluationRate.isDefined) {
            if (response.revaluationRate == Some("0"))
              Some(Messages("gmp.reval_rate.subheader", Messages(s"gmp.revaluation_rate.type_${response.revaluationRate.get}")) + " <b class='bold'>(" + Messages(s"gmp.revaluation_rate.type_${response.calculationPeriods.head.revaluationRate}") + ")</b>")
            else
              Some(Messages("gmp.reval_rate.subheader", Messages(s"gmp.revaluation_rate.type_${response.revaluationRate.get}")))
          }
          else None
        }

        case 2 | 3 | 4 => {
          leaving.leaving match {
            case Some(Leaving.NO) => None
            case _ => {
              if (response.revaluationRate.isDefined && response.revaluationRate == Some("0"))
                Some(Messages("gmp.chosen_rate.subheader", Messages(s"gmp.revaluation_rate.type_${response.revaluationRate.get}")) + " <b class='bold'>(" + Messages(s"gmp.revaluation_rate.type_${response.calculationPeriods.head.revaluationRate}") + ")</b>")
              else if (response.revaluationRate.isDefined)
                Some(Messages("gmp.chosen_rate.subheader", Messages(s"gmp.revaluation_rate.type_${response.calculationPeriods.head.revaluationRate}")))
              else
                Some(Messages("gmp.held_rate.subheader", Messages(s"gmp.revaluation_rate.type_${response.calculationPeriods.head.revaluationRate}")))
            }
          }
        }

      }
    }
    else
      None
  }

  private def survivorSubheader(session: GmpSession, response: CalculationResponse): Option[String] = {

    if (response.calculationPeriods.nonEmpty) {
      response.calcType match {
        case 3 => {
          session.leaving.leaving match {
            case Some(Leaving.NO) => None
            case _ => {
              if (response.calculationPeriods.head.inflationProofBeyondDod == Some(0) && response.dodInSameTaxYearAsRevaluationDate)
                Some(Messages("gmp.no_inflation.subheader"))
              else
                None
            }
          }
        }
        case _ => None
      }
    }
    else
      None
  }

}


