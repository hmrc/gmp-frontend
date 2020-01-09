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

package controllers

import com.google.inject.{Inject, Singleton}
import config.{ApplicationConfig, GmpContext}
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.mvc.{Action, Controller}

@Singleton
class ServiceUnavailableController @Inject()(gmpContext: GmpContext,ac:ApplicationConfig) extends Controller {

  implicit val context: GmpContext = gmpContext
  implicit  val applicationConfig:ApplicationConfig=ac

  def get = Action {
    implicit request => {
      Ok(views.html.service_unavailable())
    }
  }
}
