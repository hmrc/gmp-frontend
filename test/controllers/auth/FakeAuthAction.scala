/*
 * Copyright 2020 HM Revenue & Customs
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

import play.api.Configuration
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{MessagesControllerComponents, Request, Result}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.domain.Generator

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future
import scala.util.Random

object FakeAuthAction extends AuthAction(
  authConnector = new GuiceApplicationBuilder().injector().instanceOf[AuthConnector],
  configuration = new GuiceApplicationBuilder().injector().instanceOf[Configuration],
  messagesControllerComponents = new GuiceApplicationBuilder().injector().instanceOf[MessagesControllerComponents],
  externalUrls = new GuiceApplicationBuilder().injector().instanceOf[ExternalUrls]) {


  val nino = new Generator(new Random).nextNino

  override def invokeBlock[A](request: Request[A], block: AuthenticatedRequest[A] => Future[Result]): Future[Result] = {

    block(AuthenticatedRequest("testID", request))
  }
}
