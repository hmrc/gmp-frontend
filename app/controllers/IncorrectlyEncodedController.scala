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

import com.google.inject.Inject
import controllers.auth.AuthAction
import forms.InflationProofForm.inflationProofForm
import uk.gov.hmrc.auth.core.AuthConnector
import play.api.i18n.Messages.Implicits._
import com.google.inject.{Inject, Singleton}
import controllers.auth.AuthAction
import forms.InflationProofForm._
import play.api.Logger
import play.api.Play.current
import play.api.i18n.Messages
import uk.gov.hmrc.auth.core.AuthConnector

import scala.concurrent.Future
import scala.concurrent.Future

class IncorrectlyEncodedController @Inject()( authAction: AuthAction,
                                              override val authConnector: AuthConnector
                                            ) extends GmpPageFlow(authConnector){
  def get = authAction.async {
    implicit request => {
      Future.successful(InternalServerError(views.html.incorrectlyEncoded(Messages("gmp.bulk.incorrectlyEncoded"), Messages("gmp.bulk.incorrectlyEncoded.header"), Messages("gmp.bulk_failure_incorrectlyEncoded.title"))))
    }
  }
}
