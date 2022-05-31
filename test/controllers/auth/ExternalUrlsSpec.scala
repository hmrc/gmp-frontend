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

package controllers.auth

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite


class ExternalUrlsSpec extends PlaySpec with GuiceOneServerPerSuite {

  lazy val externalUrls = app.injector.instanceOf[ExternalUrls]
  "ExternalUrls" must {

    "have basGatewayHost " in {
      externalUrls.basGatewayHost must be("http://localhost:9553")
    }

    "have loginCallback " in {
      externalUrls.loginCallback must be("http://localhost:9941/guaranteed-minimum-pension/dashboard")
    }

    "have signOutCallback " in {
      externalUrls.signOutCallback must be("http://localhost:9514/feedback/GMP")
    }

    "have loginPath " in {
      externalUrls.loginPath must be("sign-in")
    }

    "have signIn " in {
      externalUrls.signIn must be(s"""http://localhost:9553/bas-gateway/sign-in?continue_url=http://localhost:9941/guaranteed-minimum-pension/dashboard""")
    }

    "have signoutPath" in {
      externalUrls.signOutPath must be("sign-out-without-state")
    }

    "have signout" in {
      externalUrls.signOut must be(s"""http://localhost:9553/bas-gateway/sign-out-without-state?continue=http://localhost:9514/feedback/GMP""")
    }
  }
  
}
