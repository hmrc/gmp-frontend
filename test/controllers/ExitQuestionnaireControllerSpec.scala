/*
 * Copyright 2016 HM Revenue & Customs
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

import models.{ExitQuestionnaire, RevaluationRate}
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.{OneServerPerSuite, PlaySpec}
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.mvc.{Result, AnyContentAsEmpty}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SessionService
import uk.gov.hmrc.play.audit.http.connector.{AuditResult, AuditConnector}
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

import scala.concurrent.Future

class ExitQuestionnaireControllerSpec extends PlaySpec with OneServerPerSuite with MockitoSugar with GmpUsers {

  val mockAuthConnector = mock[AuthConnector]
  val mockSessionService = mock[SessionService]
  val mockAuditConnector = mock[AuditConnector]

  object TestExitQuestionnaireController extends ExitQuestionnaireController {
    val authConnector = mockAuthConnector
    override val sessionService = mockSessionService
    override val auditConnector = mockAuditConnector
  }

  "Exit Questionnaire controller" must {

    "respond to GET /guaranteed-minimum-pension/questionnaire" in {
      val result = route(FakeRequest(GET, "/guaranteed-minimum-pension/questionnaire"))
      status(result.get) must not equal (NOT_FOUND)
    }
  }

  "Thank You" must {
    "respond with ok" in {
      getThankYou() { result =>
        status(result) must equal(OK)
        contentAsString(result) must include(Messages("gmp.thank_you.title"))
        contentAsString(result) must include(Messages("gmp.thank_you.header"))
        contentAsString(result) must include(Messages("gmp.thank_you.what_now"))
        contentAsString(result) must include(Messages("gmp.button.check_another"))
      }
    }
  }

  "GET" must {

    "be authorised" in {
      get() { result =>
        status(result) must equal(SEE_OTHER)
        redirectLocation(result).get must include("/account/sign-in")
      }
    }

    "authenticated users" must {

      "respond with ok" in {

        withAuthorisedUser { user =>
          get(user) { result =>
            status(result) must equal(OK)
            contentAsString(result) must include(Messages("gmp.questionnaire.title"))
            contentAsString(result) must include(Messages("gmp.questionnaire.how_easy"))
            contentAsString(result) must include(Messages("gmp.questionnaire.very_easy"))
            contentAsString(result) must include(Messages("gmp.questionnaire.easy"))
            contentAsString(result) must include(Messages("gmp.questionnaire.difficult"))
            contentAsString(result) must include(Messages("gmp.questionnaire.very_difficult"))
            contentAsString(result) must include(Messages("gmp.questionnaire.how_satisfied"))
            contentAsString(result) must include(Messages("gmp.questionnaire.very_satisfied"))
            contentAsString(result) must include(Messages("gmp.questionnaire.satisfied"))
            contentAsString(result) must include(Messages("gmp.questionnaire.unsatisfied"))
            contentAsString(result) must include(Messages("gmp.questionnaire.very_unsatisfied"))
            contentAsString(result) must include(Messages("gmp.questionnaire.comments"))
          }
        }
      }
    }
  }

  "POST" must {
    "be authorised" in {
      val result = TestExitQuestionnaireController.get.apply(FakeRequest())
      status(result) must equal(SEE_OTHER)
      redirectLocation(result).get must include("/account/sign-in")
    }

    "authenticated users" must {

      "with invalid data" must {

        "respond with BAD_REQUEST" in {
          withAuthorisedUser { request =>
            val postData = Json.toJson(
              ExitQuestionnaire(Some(ExitQuestionnaire.VERY_EASY), Some(ExitQuestionnaire.VERY_SATISFIED), Some("I am writing something. Yes, I plan to make it the most boring thing ever written. I go to the store. A car is parked. Many cars are parked or moving. Some are blue. Some are tan. They have windows. In the store, there are items for sale. These include such things as soap, detergent, magazines, and lettuce. You can enhance your life with these products. Soap can be used for bathing, be it in a bathtub or in a shower. Apply the soap to your body and rinse. Detergent is used to wash clothes. Place your dirty clothes into a washing machine and add some detergent as directed on the box. Select the appropriate settings on your washing machine and you should be ready to begin. Magazines are stapled reading material made with glossy paper, and they cover a wide variety of topics, ranging from news and politics to business and stock market information. Some magazines are concerned with more recreational topics, like sports card collecting or different kinds of hairstyles. Lettuce is a vegetable. It is usually green and leafy, and is the main ingredient of salads. You may have an appliance at home that can quickly shred lettuce for use in salads. Lettuce is also used as an optional item for hamburgers and deli sandwiches. Some people even eat lettuce by itself. I have not done this. So you can purchase many types of things at stores."))
            )
            val result = TestExitQuestionnaireController.post.apply(request.withJsonBody(postData))
            status(result) must equal(BAD_REQUEST)
          }
        }
      }


      "with valid data" must {

        "redirect to thank you page" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          withAuthorisedUser { request =>
            val postData = Json.toJson(
              ExitQuestionnaire(Some(ExitQuestionnaire.VERY_EASY), Some(ExitQuestionnaire.VERY_SATISFIED), Some("These are the comments"))
            )
            val result = TestExitQuestionnaireController.post.apply(request.withJsonBody(postData))
            status(result) must equal(SEE_OTHER)
          }
        }

        "redirect to thank you page even when audit has failed" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.failed(new Exception()))
          withAuthorisedUser { request =>
            val postData = Json.toJson(
              ExitQuestionnaire(Some(ExitQuestionnaire.VERY_EASY), Some(ExitQuestionnaire.VERY_SATISFIED), Some("These are the comments"))
            )
            val result = TestExitQuestionnaireController.post.apply(request.withJsonBody(postData))
            status(result) must equal(SEE_OTHER)
            redirectLocation(result).get must include("/questionnaire/thankyou")
          }
        }
      }
    }
  }



  def get(request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest())(handler: Future[Result] => Any): Unit = {
    handler(TestExitQuestionnaireController.get.apply(request))
  }

  def getThankYou(request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest())(handler: Future[Result] => Any): Unit = {
    handler(TestExitQuestionnaireController.showThankYou.apply(request))
  }
}
