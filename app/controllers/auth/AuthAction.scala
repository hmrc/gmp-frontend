/*
 * Copyright 2021 HM Revenue & Customs
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

package controllers.auth

import com.google.inject.{Inject, Singleton}
import play.api.Mode
import play.api.mvc._
import play.api.{Configuration, Environment}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.http.HttpClient
import scala.concurrent.{ExecutionContext, Future}

case class AuthenticatedRequest[A](linkId: String, request:Request[A]) extends WrappedRequest[A](request)

@Singleton
class AuthAction @Inject()(override val authConnector: AuthConnector,
                           configuration: Configuration,
                           messagesControllerComponents: MessagesControllerComponents,
                           externalUrls: ExternalUrls)(implicit ec: ExecutionContext)
  extends ActionBuilder[AuthenticatedRequest, AnyContent] with AuthorisedFunctions{

  override val parser: BodyParser[AnyContent] = messagesControllerComponents.parsers.defaultBodyParser
  override protected val executionContext: ExecutionContext = messagesControllerComponents.executionContext


  override def invokeBlock[A](request: Request[A], block: AuthenticatedRequest[A] => Future[Result]): Future[Result] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    authorised(ConfidenceLevel.L50 and (Enrolment("HMRC-PSA-ORG") or Enrolment("HMRC-PP-ORG") or Enrolment("HMRC-PODS-ORG")))
      .retrieve(Retrievals.authorisedEnrolments) {
        case Enrolments(enrolments) => {

          val psaid = enrolments.find(_.key == "HMRC-PSA-ORG").flatMap {
            enrolment => enrolment.identifiers.find(id => id.key == "PSAID").map(_.value)
          }
          val ppid = enrolments.find(_.key == "HMRC-PP-ORG").flatMap {
            enrolment => enrolment.identifiers.find(id => id.key == "PPID").map(_.value)
          }

          val podsPsaid = enrolments.find(_.key == "HMRC-PODS-ORG").flatMap {
            enrolment => enrolment.identifiers.find(id => id.key == "PSAID").map(_.value)
          }

          psaid.orElse(ppid).orElse(podsPsaid).fold(Future.successful(Results.Redirect(externalUrls.signIn)))(id => block(AuthenticatedRequest(id, request)))

        }
        case _ => throw new RuntimeException("Can't find credentials for user")
      }
  } recover {
    case ex: NoActiveSession => Results.Redirect(externalUrls.signIn)

    case ex: InsufficientConfidenceLevel => Results.Redirect(controllers.routes.ApplicationController.unauthorised.url)

    case ex: InsufficientEnrolments => Results.Redirect(controllers.routes.ApplicationController.unauthorised.url)
  }
}


@Singleton
class GmpAuthConnector @Inject()(val http: HttpClient,
                                 environment: Environment,
                                 val runModeConfiguration: Configuration,
                                servicesConfig: ServicesConfig) extends PlayAuthConnector {

  val serviceUrl: String = servicesConfig.baseUrl("auth")

  protected def mode: Mode = environment.mode
}
