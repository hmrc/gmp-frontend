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

package connectors

import java.util.UUID
import helpers.RandomNino
import metrics.ApplicationMetrics
import models._
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfter
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Environment
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.SessionId
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.http.HttpClient

import scala.concurrent.Future


class GmpConnectorSpec extends PlaySpec with GuiceOneServerPerSuite with MockitoSugar with BeforeAndAfter {

  val email = "Bill@Gill.com"
  val reference = "Smith's Reference"
  val groupId = "S1401234A"
  val link = "some-link"
  val psaId = "B1234567"
  val pspId = "11111111"
  val nino = RandomNino.generate

  val mockHttpPost = mock[HttpClient]
  val mockHttpGet = mock[HttpClient]
  val mockHttpPut = mock[HttpClient]
  val metrics = app.injector.instanceOf[ApplicationMetrics]
  object TestGmpConnector extends GmpConnector(
    app.injector.instanceOf[Environment],
    app.configuration,
    metrics,
    mockHttpPost,
    mockHttpGet,
    mockHttpPut,
    app.injector.instanceOf[ServicesConfig]
  )

  before {
    reset(mockHttpPost)
    reset(mockHttpGet)
    reset(mockHttpPut)
  }

  "The GMP Connector" must {

    implicit val hc = new HeaderCarrier(sessionId = Some(SessionId(s"session-${UUID.randomUUID}")))

    "performing a single calculation" must {

      "return a calculation response" in {

        val calcResponseJson = Json.parse(
          s"""
             {"name":"Bill Smith",
             "nino":"$nino",
             "scon":"S1234567T",
             "revalulationRate":1,
             "revaluationDate":"2015-11-10",
             "calculationPeriods":[
                {
                  "startDate":"2015-11-10",
                  "endDate":"2015-11-10",
                  "gmpTotal":"1.11",
                  "post88GMPTotal":"2.22",
                  "revaluationRate":1,
                  "errorCode":0}],
                "globalErrorCode" : 0,
                "dualCalc" : false,
                "calcType" : 1
              }
          """
        )

        val calculationRequest: CalculationRequest = CalculationRequest(scon = "S1234567T", nino = nino, surname = "Smith", firstForename = "Bill",
          1, None, Some(1))
        when(mockHttpPost.POST[CalculationRequest, CalculationResponse](Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful((calcResponseJson.as[CalculationResponse])))

        val result = TestGmpConnector.calculateSingle(calculationRequest,link)
        val calcResponse = await(result)

        calcResponse.calculationPeriods.length must be(1)

      }

      "return a calculation response when practitioner" in {

        val calcResponseJson = Json.parse(
          s"""
             {"name":"Adam Coles",
             "nino":"$nino",
             "scon":"S1234567T",
             "revalulationRate":1,
             "revaluationDate":"2015-11-10",
             "calculationPeriods":[
                {
                  "startDate":"2015-11-10",
                  "endDate":"2015-11-10",
                  "gmpTotal":"1.11",
                  "post88GMPTotal":"2.22",
                  "revaluationRate":1,
                  "errorCode":0}],
                "globalErrorCode" : 0,
                "dualCalc" : false,
                "calcType" : 1
              }
          """
        )

        val calculationRequest: CalculationRequest = CalculationRequest(scon = "S1401234Z", nino = "CB433298A", surname = "Smith", firstForename = "Bill",
          1, None, Some(1))
        when(mockHttpPost.POST[CalculationRequest, CalculationResponse](Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful((calcResponseJson.as[CalculationResponse])))

        val result = TestGmpConnector.calculateSingle(calculationRequest,link)
        val calcResponse = await(result)

        calcResponse.calculationPeriods.length must be(1)

      }

      "return a calculation response when start date is null" in {

        val calcResponseJson = Json.parse(
          s"""
             {"name":"Bill Smith",
             "nino":"$nino",
             "scon":"S1234567T",
             "revalulationRate":1,
             "revaluationDate":"2015-11-10",
             "calculationPeriods":[
                {
                  "startDate": null,
                  "endDate":"2015-11-10",
                  "gmpTotal":"1.11",
                  "post88GMPTotal":"2.22",
                  "revaluationRate":1,
                  "errorCode":0}],
                "globalErrorCode" : 0,
                "dualCalc" : false,
                "calcType" : 1
              }
          """
        )

        val calculationRequest: CalculationRequest = CalculationRequest(scon = "S1234567T", nino = nino, surname = "Smith", firstForename = "Bill",
          1, None, Some(1))
        when(mockHttpPost.POST[CalculationRequest, CalculationResponse](Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful((calcResponseJson.as[CalculationResponse])))

        val result = TestGmpConnector.calculateSingle(calculationRequest,link)
        val calcResponse = await(result)

        calcResponse.calculationPeriods.length must be(1)

      }

      "return an error when scon incorrect" in {

        val calcRequestBody =
          s"""{
                                         "scon":"S1401234A",
                                         "nino":"$nino",
                                         "surname":"Smith",
                                         "firstForename":"Bill",
                                         "calctype":1

                                      }"""


        when(mockHttpPost.POST[CalculationRequest, CalculationResponse](Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.failed(UpstreamErrorResponse("Scon not found", 500, 500)))

        val result = TestGmpConnector.calculateSingle(Json.fromJson[CalculationRequest](Json.parse(calcRequestBody)).get,link)
        intercept[UpstreamErrorResponse] {
                await(result)
              }

      }

      "throw an exception with invalid user" in {

        val nino = RandomNino.generate
        val calculationRequest: CalculationRequest = CalculationRequest(scon = "S1401234Z", nino = nino, surname = "Smith", firstForename = "Bill", 1)

        intercept[RuntimeException] {
          TestGmpConnector.calculateSingle(calculationRequest,link)
        }
      }
    }

    "performing scon validation" must {
      "return a validateScon response" in {

        val validateSconResponseJson = Json.parse(
          """{
             "sconExists":false
             }"""
        )

        val validateSconRequest: ValidateSconRequest = ValidateSconRequest(scon = "S1401234Z")
        when(mockHttpPost.POST[ValidateSconRequest, ValidateSconResponse](Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful((validateSconResponseJson.as[ValidateSconResponse])))

        val result = TestGmpConnector.validateScon(validateSconRequest,link)
        val validateSconResponse = await(result)

        validateSconResponse.sconExists must be(false)
      }

      "throw an exception with invalid user" in {
        val validateSconRequest: ValidateSconRequest = ValidateSconRequest(scon = "S1401234Z")

        intercept[RuntimeException] {
          TestGmpConnector.validateScon(validateSconRequest,link)
        }
      }
    }

    "performing scon validation on practitioner" must {
      "return a validateScon response" in {

        val validateSconResponseJson = Json.parse(
          """{
             "sconExists":false
             }"""
        )

        val validateSconRequest: ValidateSconRequest = ValidateSconRequest(scon = "S1401234Z")
        when(mockHttpPost.POST[ValidateSconRequest, ValidateSconResponse](Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful((validateSconResponseJson.as[ValidateSconResponse])))

        val result = TestGmpConnector.validateScon(validateSconRequest,link)
        val validateSconResponse = await(result)

        validateSconResponse.sconExists must be(false)
      }

      "throw an exception with invalid user" in {
        val validateSconRequest: ValidateSconRequest = ValidateSconRequest(scon = "S1401234Z")

        intercept[RuntimeException] {
          TestGmpConnector.validateScon(validateSconRequest,link)
        }
      }
    }

    "DualCalc" must {
      "return dualCalc indicated in request" in {

        val calcResponseJson = Json.parse(
          s"""
             {"name":"Bill Smith",
             "nino":"$nino",
             "scon":"S1234567T",
             "revalulationRate":1,
             "revaluationDate":"2015-11-10",
             "calculationPeriods":[
                {
                  "startDate": null,
                  "endDate":"2015-11-10",
                  "gmpTotal":"1.11",
                  "post88GMPTotal":"2.22",
                  "revaluationRate":1,
                  "errorCode":0}],
                "globalErrorCode" : 0,
                "dualCalc" : true,
                "calcType" : 1
             }
          """
        )

        val calculationRequest: CalculationRequest = CalculationRequest(scon = "S1401234Z", nino = nino, surname = "Smith", firstForename = "Bill",
          1, None, Some(1), dualCalc = Some(1))
        when(mockHttpPost.POST[CalculationRequest, CalculationResponse](Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful((calcResponseJson.as[CalculationResponse])))

        val result = TestGmpConnector.calculateSingle(calculationRequest,link)
        val calcResponse = await(result)

        calcResponse.dualCalc must be(true)
      }
    }
  }
}
