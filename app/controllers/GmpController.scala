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

package controllers


import com.google.inject.{Inject, Singleton}
import config.{ApplicationConfig, GmpContext}
import models.{CalculationType, GmpSession, Leaving}
import play.api.Logging
import play.api.i18n.{Messages, MessagesImpl}
import play.api.mvc.{MessagesControllerComponents, Result}
import services.SessionService
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.time.TaxYear

@Singleton
class GmpController @Inject()(val messagesControllerComponents: MessagesControllerComponents,
                              ac: ApplicationConfig,
                              sessionService: SessionService,
                              context: GmpContext)
                  extends FrontendController(messagesControllerComponents){

  implicit val applicationConfig: config.ApplicationConfig  = ac
  implicit lazy val messages: Messages = MessagesImpl(messagesControllerComponents.langs.availables.head, messagesApi)
}

object PageType {
  val REVALUATION = "RevaluationController"
  val EQUALISE = "EqualiseController"
  val REVALUATION_RATE = "RevaluationRateController"
  val PENSION_DETAILS = "PensionDetailsController"
  val MEMBER_DETAILS = "MemberDetailsController"
  val SCENARIO = "ScenarioController"
  val DATE_OF_LEAVING = "DateOfLeavingController"
  val RESULTS = "ResultsController"
  val INFLATION_PROOF = "InflationProofController"
}

class GmpPageFlow @Inject()(val authConnector: AuthConnector,
                            sessionService: SessionService,
                            implicit val context: GmpContext,
                            messagesControllerComponents: MessagesControllerComponents,
                            applicationConfig: ApplicationConfig)
                          extends GmpController(messagesControllerComponents,applicationConfig,sessionService,context) {


  val forwardNavigation: Map[String, GmpSession => Result] = Map(
    PageType.INFLATION_PROOF -> { (session: GmpSession) => Redirect(routes.ResultsController.get) },
    PageType.REVALUATION -> { (session: GmpSession) =>
      if (session.leaving.leaving.isDefined && session.leaving.leaving.get.equals(Leaving.NO))
        Redirect(controllers.routes.EqualiseController.get)
      else if (session.leaving.leaving.isDefined && session.leaving.leaving.get.equals(Leaving.YES_BEFORE))
        Redirect(controllers.routes.RevaluationRateController.get)
      else if (sameTaxYear(session))
        Redirect(routes.EqualiseController.get)
      else
        Redirect(routes.RevaluationRateController.get)
    },
    PageType.EQUALISE -> { (session: GmpSession) => Redirect(routes.ResultsController.get) },

    PageType.REVALUATION_RATE -> { (session: GmpSession) => {
      session.scenario match {
        case (CalculationType.PAYABLE_AGE | CalculationType.SPA | CalculationType.REVALUATION) => Redirect(routes.EqualiseController.get)
        case CalculationType.SURVIVOR => Redirect(controllers.routes.InflationProofController.get)
      }
    }
    },
    PageType.PENSION_DETAILS -> { (session: GmpSession) => Redirect(routes.MemberDetailsController.get) },
    PageType.MEMBER_DETAILS -> { (session: GmpSession) => Redirect(routes.ScenarioController.get) },
    PageType.SCENARIO -> { (session: GmpSession) => Redirect(routes.DateOfLeavingController.get) },
    PageType.DATE_OF_LEAVING -> { (session: GmpSession) => {
      session.scenario match {
        case CalculationType.DOL => Redirect(controllers.routes.EqualiseController.get)
        case CalculationType.PAYABLE_AGE | CalculationType.SPA => {
          session.leaving.leaving match {
            case Some(Leaving.YES_AFTER) | Some(Leaving.YES_BEFORE) => Redirect(controllers.routes.RevaluationRateController.get)
            case _ => Redirect(controllers.routes.EqualiseController.get)
          }
        }
        case CalculationType.REVALUATION => Redirect(controllers.routes.RevaluationController.get)
        case CalculationType.SURVIVOR => {
          session.leaving.leaving match {
            case Some(Leaving.YES_AFTER) | Some(Leaving.YES_BEFORE) => Redirect(routes.RevaluationRateController.get)
            case _ => Redirect(controllers.routes.InflationProofController.get)
          }
        }
      }
    }
    }
  )

  def nextPage(fromController: String, gmpSession: GmpSession): Result = {
    val fn = forwardNavigation.get(fromController)
    fn match {
      case Some(redirect) => redirect(gmpSession)
      case None => NotFound
    }
  }

  val backNavigation: Map[String, GmpSession => Result] = Map(
    PageType.MEMBER_DETAILS -> { (session: GmpSession) => Redirect(routes.PensionDetailsController.get) },
    PageType.SCENARIO -> { (session: GmpSession) => Redirect(routes.MemberDetailsController.get) },
    PageType.REVALUATION -> { (session: GmpSession) => Redirect(routes.DateOfLeavingController.get) },
    PageType.REVALUATION_RATE -> { (session: GmpSession) => {
      session.scenario match {
        case (CalculationType.SPA | CalculationType.PAYABLE_AGE | CalculationType.SURVIVOR) => Redirect(routes.DateOfLeavingController.get)
        case (CalculationType.REVALUATION) => Redirect(routes.RevaluationController.get)
      }
    }
    },
    PageType.INFLATION_PROOF -> { (session: GmpSession) =>
      session.leaving.leaving match {
        case Some(Leaving.YES_AFTER) | Some(Leaving.YES_BEFORE) => Redirect(routes.RevaluationRateController.get)
        case _ => Redirect(routes.DateOfLeavingController.get)
      }
    },
    PageType.EQUALISE -> { (session: GmpSession) => {
      session.scenario match {
        case (CalculationType.REVALUATION) => {

          if (sameTaxYear(session) || (session.leaving.leaving.isDefined && session.leaving.leaving.get.equals(Leaving.NO)))
            Redirect(routes.RevaluationController.get)
          else
            Redirect(routes.RevaluationRateController.get)

        }
        case (CalculationType.SPA | CalculationType.PAYABLE_AGE) =>
          session.leaving.leaving match {
            case Some(Leaving.YES_AFTER) | Some(Leaving.YES_BEFORE) => Redirect(routes.RevaluationRateController.get)
            case _ => Redirect(routes.DateOfLeavingController.get)
          }

        case (CalculationType.DOL) => Redirect(routes.DateOfLeavingController.get)

      }
    }
    },
    PageType.DATE_OF_LEAVING -> {
      (session: GmpSession) => Redirect(routes.ScenarioController.get)
    }

  )

  def previousPage(fromController: String, gmpSession: GmpSession): Result = {
    val bn = backNavigation.get(fromController)
    bn match {
      case Some(redirect) => redirect(gmpSession)
      case None => NotFound
    }
  }

  def sameTaxYear(session: GmpSession): Boolean = {

    session.revaluationDate match {
      case Some(rDate) => {
        (rDate.getAsLocalDate, session.leaving.leavingDate.getAsLocalDate) match {
          case (Some(revDate), Some(lDate)) => {
            TaxYear.taxYearFor(revDate) == TaxYear.taxYearFor(lDate)
          }
          case _ => false
        }
      }
      case _ => false
    }
  }


}
