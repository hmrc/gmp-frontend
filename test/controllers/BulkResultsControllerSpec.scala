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

import connectors.GmpBulkConnector
import models.BulkResultsSummary
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.{OneServerPerSuite, PlaySpec}
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.http.Upstream4xxResponse

import scala.concurrent.Future

class BulkResultsControllerSpec extends PlaySpec with OneServerPerSuite with MockitoSugar with GmpUsers {

  val mockAuthConnector = mock[AuthConnector]
  val mockGmpBulkConnector = mock[GmpBulkConnector]

  object TestBulkResultsController extends BulkResultsController {
    override protected def authConnector: AuthConnector = mockAuthConnector

    override val gmpBulkConnector: GmpBulkConnector = mockGmpBulkConnector
  }

  "Bulk Results Controller" must {
    val bulkResultsSummary = BulkResultsSummary("0001.csv", 50, 25)

    "respond to GET /guaranteed-minimum-pension/bulk/results" in {
      val result = route(FakeRequest(GET, "/guaranteed-minimum-pension/bulk/results/athing"))
      status(result.get) must not equal (NOT_FOUND)
    }


    "GET" must {

      "require authorisation" in {

        val result = TestBulkResultsController.get("").apply(FakeRequest())
        status(result) must equal(SEE_OTHER)
        redirectLocation(result).get must include("/account/sign-in")
      }

      "when authorised" must {

        "respond with a status of OK" in {
          when(mockGmpBulkConnector.getBulkResultsSummary(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(bulkResultsSummary))
          withAuthorisedUser { request =>
            val result = TestBulkResultsController.get("").apply(request)
            status(result) must equal(OK)
          }
        }

        "load the results page" in {
          when(mockGmpBulkConnector.getBulkResultsSummary(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(bulkResultsSummary))
          withAuthorisedUser { request =>
            val result = TestBulkResultsController.get("").apply(request)

            contentAsString(result) must include(Messages("gmp.bulk.results.banner"))
          }
        }

        "contain correct successful count" in {


          when(mockGmpBulkConnector.getBulkResultsSummary(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(bulkResultsSummary))

          withAuthorisedUser { request =>
            val result = TestBulkResultsController.get("").apply(request)

            contentAsString(result) must include(Messages("gmp.bulk.subheaders.successfulcalculations") + " (" + (bulkResultsSummary.total - bulkResultsSummary.failed) + ")")
          }

        }

        "show the incorrect user page" in {
          when(mockGmpBulkConnector.getBulkResultsSummary(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.failed(new Upstream4xxResponse("", FORBIDDEN, 0, Map())))
          withAuthorisedUser { request =>
            val result = TestBulkResultsController.get("").apply(request)

            contentAsString(result) must include(Messages("gmp.bulk.wrong_user.login_text"))
          }
        }
      }
    }

    "getCsv" must {

      "require authorisation" in {

        val result = TestBulkResultsController.getResultsAsCsv("").apply(FakeRequest())
        status(result) must equal(SEE_OTHER)
        redirectLocation(result).get must include("/account/sign-in")
      }

      "download the results summary in csv format" in {

        when(mockGmpBulkConnector.getResultsAsCsv(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful("CSV STRING"))

        withAuthorisedUser { request =>
          val result = TestBulkResultsController.getResultsAsCsv("").apply(request)

          contentAsString(result) must be("CSV STRING")
        }
      }

    }
  }

}
