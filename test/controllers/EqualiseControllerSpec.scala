/*
 * Copyright 2024 HM Revenue & Customs
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

import config.{ApplicationConfig, GmpSessionCache}
import controllers.auth.{AuthAction, FakeAuthAction}
import forms.EqualiseForm
import models._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import org.scalatestplus.play.PlaySpec
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.api.libs.json.Json
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SessionService
import uk.gov.hmrc.auth.core.AuthConnector
import views.Views

import scala.concurrent.{ExecutionContext, Future}

class EqualiseControllerSpec extends PlaySpec with GuiceOneServerPerSuite with MockitoSugar {

  val mockAuthConnector = mock[AuthConnector]
  val mockSessionService = mock[SessionService]
  val mockAuthAction = mock[AuthAction]
  implicit val mcc: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]
  implicit val ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]
  implicit val messagesAPI: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit val messagesProvider: MessagesImpl = MessagesImpl(Lang("en"), messagesAPI)
  implicit val ac: ApplicationConfig = app.injector.instanceOf[ApplicationConfig]
  implicit val gmpSessionCache: GmpSessionCache = app.injector.instanceOf[GmpSessionCache]
  lazy val equaliseForm = new EqualiseForm(mcc)
  lazy val views = app.injector.instanceOf[Views]

  object TestEqualiseController extends EqualiseController(FakeAuthAction, mockAuthConnector, mockSessionService,FakeGmpContext,equaliseForm,mcc,ac,ec,gmpSessionCache,views) {
    }

  "EqualiseController GET" must {

    "respond with ok" in {

      val result = TestEqualiseController.get(FakeRequest())
      status(result) must equal(OK)
      contentAsString(result) must include(Messages("gmp.equalise_header"))
    }

    "present the equalise page" in {
      when(mockSessionService.fetchMemberDetails()(any())).thenReturn(Future.successful(None))

        val result = TestEqualiseController.get(FakeRequest())
        contentAsString(result) must include(Messages("gmp.equalise_header"))
        contentAsString(result) must include(Messages("gmp.back.link"))
        contentAsString(result) must include(Messages("gmp.continue.button"))
    }

  }

  "BACK" must {

    "authorised users redirect" in {

      val memberDetails = MemberDetails("", "", "")
      val session = GmpSession(memberDetails, "", CalculationType.REVALUATION, None, None, Leaving(GmpDate(None, None, None), None), None)

        when(mockSessionService.fetchGmpSession()(any())).thenReturn(Future.successful(Some(session)))
        val result = TestEqualiseController.back(FakeRequest())
        status(result) must equal(SEE_OTHER)
    }

    "throw an exception when session not fetched" in {

        when(mockSessionService.fetchGmpSession()(any())).thenReturn(Future.successful(None))
        val result = TestEqualiseController.back(FakeRequest())
        intercept[RuntimeException] {
          status(result)
      }
    }

  }

  val gmpSession = GmpSession(MemberDetails("", "", ""), "S1301234T", "", None, Some(""), Leaving(GmpDate(None, None, None), None), equalise = Some(1))


  "EqualiseController POST" must {

    "with invalid data" must {

      "authenticated users" must {

        "with invalid data" must {

          "respond with bad request must choose option" in {

                val result = TestEqualiseController.post()(FakeRequest())

                status(result) must equal(BAD_REQUEST)
                contentAsString(result) must include(Messages("gmp.error.equalise.error_message"))
          }
        }

        "with valid data" must {

          val gmpSession = GmpSession(MemberDetails("", "", ""), "S1301234T", CalculationType.REVALUATION, None, Some(""), Leaving(GmpDate(None, None, None), None), None)

          "redirect" in {

            when(mockSessionService.cacheEqualise(any())(any())).thenReturn(Future.successful(Some(gmpSession)))
              val result = TestEqualiseController.post(FakeRequest().withMethod("POST")
                .withFormUrlEncodedBody("equalise" -> "1"))
              status(result) mustBe(SEE_OTHER)
          }

          "respond with error when rate not stored" in {
            when(mockSessionService.cacheEqualise(any())(any())).thenReturn(Future.successful(None))
              intercept[RuntimeException] {
                await(TestEqualiseController.post(FakeRequest().withMethod("POST")
                  .withFormUrlEncodedBody("equalise" -> "1")))
            }
          }
        }

      }
    }
  }
}
