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

package controllers

import config.GmpContext
import connectors.ContactFrontendConnector
import org.scalatest.mockito.MockitoSugar._
import org.mockito.Matchers._
import org.mockito.Mockito.when
import play.api.Play

import scala.concurrent.Future
import uk.gov.hmrc.http.HeaderCarrier

object FakeGmpContext extends GmpContext(Play.current.injector.instanceOf[ContactFrontendConnector]){

  def apply() = {
    val m = mock[GmpContext]

    when(m.getPageHelpPartial()(any[HeaderCarrier])) thenReturn Future.successful("<div id=\"help_partial\"></div>")

    m
  }

}
