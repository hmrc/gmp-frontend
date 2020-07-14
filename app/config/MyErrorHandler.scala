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

package config

import com.google.inject.Singleton
import javax.inject.Inject
import play.api.Configuration
import play.api.i18n.MessagesApi
import play.api.mvc.Request
import play.twirl.api.Html
import uk.gov.hmrc.play.bootstrap.http.FrontendErrorHandler
import views.Views

@Singleton
class MyErrorHandler @Inject()(
                                val messagesApi: MessagesApi,
                                val configuration: Configuration,
                                implicit val applicationConfig: ApplicationConfig,
                                views: Views
                              ) (implicit val gmpContext: GmpContext)extends FrontendErrorHandler {


  override def standardErrorTemplate(pageTitle: String, heading: String, message: String)
                                    (implicit request: Request[_])=
    views.globalError(pageTitle, heading, message)

  override def notFoundTemplate(implicit request: Request[_]): Html = {
    views.globalPageNotFound()
  }

}