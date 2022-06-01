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

package views.helpers

import play.twirl.api.Html

case class MainParams(title: String,
                      pageScripts: Option[Html] = None,
                      scriptElem: Option[Html] = None,
                      userLoggedIn: Boolean = true,
                      supportLinkEnabled: Boolean = true,
                      linkElement: Option[Html] = None,
                      scriptElement: Option[Html] = None,
                      mainClass : Option[String] = None,
                      signOutEnabled: Boolean = true,
                      showSignOutLink: Boolean = true,
                      showUserResearchBanner: Boolean = false,
                      dashboardLinkEnabled:Boolean = false,
                      pageHeading: Option[String] = None,
                      customHead: Option[Html] = None,
                      isUserResearchBannerVisible:Boolean = false)
