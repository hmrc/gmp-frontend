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

import java.util.UUID
import config.ApplicationConfig
import connectors.GmpBulkConnector
import controllers.auth.FakeAuthAction
import models.BulkResultsSummary
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{GMPSessionService, SessionService}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, SessionId, UpstreamErrorResponse}
import views.Views

import scala.concurrent.{ExecutionContext, Future}

class BulkResultsControllerSpec extends PlaySpec with GuiceOneServerPerSuite with MockitoSugar {

  val mockAuthConnector = mock[AuthConnector]
  val mockGmpBulkConnector = mock[GmpBulkConnector]

  implicit val hc: HeaderCarrier = new HeaderCarrier(sessionId = Some(SessionId(s"session-${UUID.randomUUID}")))
  implicit val mcc: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]
  implicit val ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]
  implicit val messagesAPI: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit val messagesProvider: MessagesImpl = MessagesImpl(Lang("en"), messagesAPI)

  implicit val ac: ApplicationConfig = app.injector.instanceOf[ApplicationConfig]
  implicit val ss: GMPSessionService = app.injector.instanceOf[GMPSessionService]
  lazy val views = app.injector.instanceOf[Views]

  object TestBulkResultsController extends BulkResultsController(FakeAuthAction,mockAuthConnector, mockGmpBulkConnector,mcc,ac,ss,FakeGmpContext,ec,views) {}

  "Bulk Results Controller" must {
    val comingFromDashboard = 0
    val bulkResultsSummary = BulkResultsSummary("0001.csv", 50, 25)

    "GET" must {

      "when authorised" must {

        "respond with a status of OK" in {
          when(mockGmpBulkConnector.getBulkResultsSummary(any(),any())(any())).thenReturn(Future.successful(bulkResultsSummary))

            val result = TestBulkResultsController.get("",comingFromDashboard)(FakeRequest())
            status(result) must equal(OK)
        }

        "load the results page" in {
          when(mockGmpBulkConnector.getBulkResultsSummary(any(),any())(any())).thenReturn(Future.successful(bulkResultsSummary))

            val result = TestBulkResultsController.get("",comingFromDashboard)(FakeRequest())

            contentAsString(result) must include(Messages("gmp.bulk.results.banner"))
            contentAsString(result) must include(Messages("gmp.back.link"))
            contentAsString(result) must include(Messages("gmp.bulk.explanations.contsandearnings"))
            contentAsString(result) must include(Messages("gmp.bulk.query_handling_message.header"))
            contentAsString(result) must include(Messages("gmp.bulk.query_handling_message.body"))
        }

        "contain correct successful count" in {


          when(mockGmpBulkConnector.getBulkResultsSummary(any(),any())(any())).thenReturn(Future.successful(bulkResultsSummary))

            val result = TestBulkResultsController.get("",comingFromDashboard)(FakeRequest())

            contentAsString(result) must include(Messages("gmp.bulk.subheaders.successfulcalculations") + " (" + (bulkResultsSummary.total - bulkResultsSummary.failed) + ")")

        }

        "show the incorrect user page" in {
          when(mockGmpBulkConnector.getBulkResultsSummary(any(),any())(any())).thenReturn(Future.failed(UpstreamErrorResponse("", FORBIDDEN, 0, Map())))
            val result = TestBulkResultsController.get("",comingFromDashboard)(FakeRequest())

            contentAsString(result) must include(Messages("gmp.bulk.wrong_user.login_text"))
        }

        "show the calc not found page" in {
          when(mockGmpBulkConnector.getBulkResultsSummary(any(),any())(any())).thenReturn(Future.failed(UpstreamErrorResponse("Not Found", 404)))

            val result = TestBulkResultsController.get("",comingFromDashboard)(FakeRequest())

            contentAsString(result) must include(Messages("gmp.bulk.results_not_found"))
        }
      }
    }

    "getCsv" must {

      "download the results summary in csv format" in {

        when(mockGmpBulkConnector.getResultsAsCsv(any(),any(),any())(any())).thenReturn(Future.successful(HttpResponse(200, "CSV STRING")))

          val result = TestBulkResultsController.getResultsAsCsv("","")(FakeRequest())

          contentAsString(result) must be("CSV STRING")
      }

    }


    "getContributionsAsCsv" must {

      "download the contributions and earnings in csv format" in {

        when(mockGmpBulkConnector.getContributionsAndEarningsAsCsv(any(),any())(any())).thenReturn(Future.successful(HttpResponse(200, "CSV STRING")))

          val result = TestBulkResultsController.getContributionsAndEarningsAsCsv("")(FakeRequest())

          contentAsString(result) must be("CSV STRING")
      }

    }
  }

}
