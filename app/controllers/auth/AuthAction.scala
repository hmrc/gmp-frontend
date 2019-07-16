/*
 * Copyright 2019 HM Revenue & Customs
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

import com.google.inject.{ImplementedBy, Inject}
import config.WSHttp
import play.api.Mode.Mode
import play.api.mvc._
import play.api.{Configuration, Environment}
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter
import uk.gov.hmrc.play.config.ServicesConfig

import scala.concurrent.{ExecutionContext, Future}

case class AuthenticatedRequest[A](link: String, request:Request[A]) extends WrappedRequest[A](request)

class AuthActionImpl @Inject()(val authConnector: GmpAuthConnector, configuration: Configuration)
                              (implicit ec: ExecutionContext) extends AuthAction with AuthorisedFunctions {

  override def invokeBlock[A](request: Request[A], block: AuthenticatedRequest[A] => Future[Result]): Future[Result] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))

    authorised(ConfidenceLevel.L50 and (Enrolment("HMRC-PSA-ORG") or Enrolment("HMRC-PP-ORG")))
      .retrieve(Retrievals.authorisedEnrolments) {
        case Enrolments(enrolments) => {

          val psaid = enrolments.find(_.key == "HMRC-PSA-ORG").flatMap {
            enrolment => enrolment.identifiers.find(id => id.key == "PSAID").map(_.value)
          }
          val ppid = enrolments.find(_.key == "HMRC-PP-ORG").flatMap {
            enrolment => enrolment.identifiers.find(id => id.key == "PPID").map(_.value)
          }

          val link = (psaid, ppid) match {
            case (Some(id),_)     => s"psa/$id"
            case (None, Some(id)) => s"psp/$id"
            case _                => throw new RuntimeException("User Authorisation failed")
          }

          block(AuthenticatedRequest(link, request))
        }
        case _ => throw new RuntimeException("Can't find credentials for user")
      }
  } recover {
    case ex: NoActiveSession => Results.Redirect(ExternalUrls.signIn)

    case _ => Results.Redirect(controllers.routes.ApplicationController.unauthorised().url)
  }
}

@ImplementedBy(classOf[AuthActionImpl])
trait AuthAction extends ActionBuilder[AuthenticatedRequest] with ActionFunction[Request, AuthenticatedRequest]


class GmpAuthConnector @Inject()( val http: WSHttp,
                                  environment: Environment,
                                  val runModeConfiguration: Configuration
                                ) extends PlayAuthConnector with ServicesConfig {

  val serviceUrl: String = baseUrl("auth")

  override protected def mode: Mode = environment.mode
}
