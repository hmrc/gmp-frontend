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
import play.api.Configuration
import play.api.mvc.Results._
import play.api.mvc._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter
import uk.gov.hmrc.play.frontend.auth.connectors.domain.{PsaAccount, PspAccount}

import scala.concurrent.{ExecutionContext, Future}

case class AuthenticatedRequest[A](psaAccount: Option[PsaAccount], pspAccount: Option[PspAccount], request:Request[A]) extends WrappedRequest[A](request)

class AuthActionImpl @Inject()(val authConnector: AuthConnector, configuration: Configuration)
                              (implicit ec: ExecutionContext) extends AuthAction with AuthorisedFunctions {

  override def invokeBlock[A](request: Request[A], block: AuthenticatedRequest[A] => Future[Result]): Future[Result] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))

    //Add authorisation parameters - Enrolment("") ???
    authorised(ConfidenceLevel.L50 and (Enrolment("HMRC-PSA-ORG") or Enrolment("HMRC-PP-ORG")))
      .retrieve(Retrievals.authorisedEnrolments) {
        case Enrolments(enrolments) => {

          val psaid = enrolments.find(_.key == "HMRC-PSA-ORG").flatMap {
            enrolment =>
              enrolment.identifiers.find(id => id.key == "PSAID").map(_.value)
          }
          val ppid = enrolments.find(_.key == "HMRC-PP-ORG").flatMap {
            enrolment =>
              enrolment.identifiers.find(id => id.key == "PPID").map(_.value)
          }

          block(AuthenticatedRequest(psaid, ppid, request))
        }
        case _ => throw new RuntimeException("Can't find credentials for user")
      }
  } recover {
    case ex: NoActiveSession => Redirect(configuration.getString("auth-sign-in").get)

    case ex: InsufficientEnrolments => Redirect(FrontendAppConfig.saUrl)

    case ex: InsufficientConfidenceLevel => Redirect(FrontendAppConfig.saUrl)
  }
}

@ImplementedBy(classOf[AuthActionImpl])
trait AuthAction extends ActionBuilder[AuthenticatedRequest] with ActionFunction[Request, AuthenticatedRequest]


//class C2NIAuthConnector @Inject()(val http: WSHttp, configuration: Configuration) extends PlayAuthConnector {
//
//  val host = configuration.getString("microservice.services.auth.host").get
//  val port = configuration.getString("microservice.services.auth.port").get
//
//  override val serviceUrl: String = s"http://$host:$port"
//
//}
