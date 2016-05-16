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

package connectors

import java.util.UUID

import config.ApplicationConfig
import helpers.RandomNino
import metrics.Metrics
import models._
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfter
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.{OneServerPerSuite, PlaySpec}
import play.api.libs.json.{JsValue, Json}
import play.api.test.Helpers._
import uk.gov.hmrc.domain.{PsaId, PspId}
import uk.gov.hmrc.play.frontend.auth.AuthContext
import uk.gov.hmrc.play.frontend.auth.connectors.domain._
import uk.gov.hmrc.play.http.{HeaderCarrier, _}
import uk.gov.hmrc.play.http.logging.SessionId

import scala.concurrent.Future


class GmpConnectorSpec extends PlaySpec with OneServerPerSuite with MockitoSugar with BeforeAndAfter {

  val email = "Bill@Gill.com"
  val reference = "Smith's Reference"
  val groupId = "S1401234A"
  val link = "some-link"
  val psaId = "B1234567"
  val pspId = "11111111"
  val nino = RandomNino.generate

  val mockHttpPost = mock[HttpPost]
  val mockHttpGet = mock[HttpGet]
  val mockHttpPut = mock[HttpPut]
  val mockApplicationConfig = mock[ApplicationConfig]

  object TestGmpConnector extends GmpConnector {
    override val httpPost: HttpPost = mockHttpPost
    override val httpGet: HttpGet = mockHttpGet
    override val httpPut: HttpPut = mockHttpPut
    override val applicationConfig: ApplicationConfig = mockApplicationConfig
    override def metrics = Metrics
  }

  before {
    reset(mockHttpPost)
    reset(mockHttpGet)
    reset(mockHttpPut)
  }

  "The GMP Connector" must {

    implicit val hc = new HeaderCarrier(sessionId = Some(SessionId(s"session-${UUID.randomUUID}")))


    "performing a single calculation" must {

      "return a calculation response" in {
        implicit val user = AuthContext(authority = Authority("1234", Accounts(psa = Some(PsaAccount(link, PsaId(psaId)))), None, None, CredentialStrength.None, ConfidenceLevel.L50))

        val te = user.principal.accounts.psa.map(_.link).getOrElse("")

        println("***** print ****** " + te)

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
        when(mockHttpPost.POST[CalculationRequest, CalculationResponse](Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful((calcResponseJson.as[CalculationResponse])))

        val result = TestGmpConnector.calculateSingle(calculationRequest)
        val calcResponse = await(result)

        calcResponse.calculationPeriods.length must be(1)

      }

      "return a calculation response when practitioner" in {
        implicit val user = AuthContext(authority = Authority("1234", Accounts(psp = Some(PspAccount(link, PspId(pspId)))), None, None, CredentialStrength.None, ConfidenceLevel.L50))

        val calcResponseJson = Json.parse(
          """
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
        when(mockHttpPost.POST[CalculationRequest, CalculationResponse](Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful((calcResponseJson.as[CalculationResponse])))

        val result = TestGmpConnector.calculateSingle(calculationRequest)
        val calcResponse = await(result)

        calcResponse.calculationPeriods.length must be(1)

      }

      "return a calculation response when start date is null" in {
        implicit val user = AuthContext(authority = Authority("1234", Accounts(psa = Some(PsaAccount(link, PsaId(psaId)))), None, None, CredentialStrength.None, ConfidenceLevel.L50))

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
        when(mockHttpPost.POST[CalculationRequest, CalculationResponse](Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful((calcResponseJson.as[CalculationResponse])))

        val result = TestGmpConnector.calculateSingle(calculationRequest)
        val calcResponse = await(result)

        calcResponse.calculationPeriods.length must be(1)

      }

      "return an error when scon incorrect" in {
        implicit val user = AuthContext(authority = Authority("1234", Accounts(psa = Some(PsaAccount(link, PsaId(psaId)))), None, None, CredentialStrength.None, ConfidenceLevel.L50))

        val calcRequestBody =
          s"""{
                                         "scon":"S1401234A",
                                         "nino":"$nino",
                                         "surname":"Smith",
                                         "firstForename":"Bill",
                                         "calctype":1

                                      }"""


        when(mockHttpPost.POST[CalculationRequest, CalculationResponse](Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.failed(new Upstream5xxResponse("Scon not found", 500, 500)))

        val result = TestGmpConnector.calculateSingle(Json.fromJson[CalculationRequest](Json.parse(calcRequestBody)).get)
        intercept[Upstream5xxResponse] {
                await(result)
              }

      }

      "throw an exception with invalid user" in {

        val nino = RandomNino.generate
        implicit val user = AuthContext(authority = Authority("1234", Accounts(psa = None), None, None, CredentialStrength.None, ConfidenceLevel.L200))
        val calculationRequest: CalculationRequest = CalculationRequest(scon = "S1401234Z", nino = nino, surname = "Smith", firstForename = "Bill", 1)

        intercept[RuntimeException] {
          val result = TestGmpConnector.calculateSingle(calculationRequest)
        }
      }
    }

    "performing scon validation" must {
      "return a validateScon response" in {
        implicit val user = AuthContext(authority = Authority("1234", Accounts(psa = Some(PsaAccount(link, PsaId(psaId)))), None, None, CredentialStrength.None, ConfidenceLevel.L50))

        val validateSconResponseJson = Json.parse(
          """{
             "sconExists":false
             }"""
        )

        val validateSconRequest: ValidateSconRequest = ValidateSconRequest(scon = "S1401234Z")
        when(mockHttpPost.POST[ValidateSconRequest, ValidateSconResponse](Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful((validateSconResponseJson.as[ValidateSconResponse])))

        val result = TestGmpConnector.validateScon(validateSconRequest)
        val validateSconResponse = await(result)

        validateSconResponse.sconExists must be(false)
      }

      "throw an exception with invalid user" in {
        implicit val user = AuthContext(authority = Authority("1234", Accounts(psa = None), None, None, CredentialStrength.None, ConfidenceLevel.L200))
        val validateSconRequest: ValidateSconRequest = ValidateSconRequest(scon = "S1401234Z")

        intercept[RuntimeException] {
          val result = TestGmpConnector.validateScon(validateSconRequest)
        }
      }
    }

    "performing scon validation on practitioner" must {
      "return a validateScon response" in {
        implicit val user = AuthContext(authority = Authority("1234", Accounts(psp = Some(PspAccount(link, PspId(pspId)))), None, None, CredentialStrength.None, ConfidenceLevel.L50))

        val validateSconResponseJson = Json.parse(
          """{
             "sconExists":false
             }"""
        )

        val validateSconRequest: ValidateSconRequest = ValidateSconRequest(scon = "S1401234Z")
        when(mockHttpPost.POST[ValidateSconRequest, ValidateSconResponse](Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful((validateSconResponseJson.as[ValidateSconResponse])))

        val result = TestGmpConnector.validateScon(validateSconRequest)
        val validateSconResponse = await(result)

        validateSconResponse.sconExists must be(false)
      }

      "throw an exception with invalid user" in {
        implicit val user = AuthContext(authority = Authority("1234", Accounts(psa = None), None, None, CredentialStrength.None, ConfidenceLevel.L200))
        val validateSconRequest: ValidateSconRequest = ValidateSconRequest(scon = "S1401234Z")

        intercept[RuntimeException] {
          val result = TestGmpConnector.validateScon(validateSconRequest)
        }
      }
    }

    "DualCalc" must {
      "return dualCalc indicated in request" in {
        implicit val user = AuthContext(authority = Authority("1234", Accounts(psa = Some(PsaAccount(link, PsaId(psaId)))), None, None, CredentialStrength.None, ConfidenceLevel.L50))

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
        when(mockHttpPost.POST[CalculationRequest, CalculationResponse](Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful((calcResponseJson.as[CalculationResponse])))

        val result = TestGmpConnector.calculateSingle(calculationRequest)
        val calcResponse = await(result)

        calcResponse.dualCalc must be(true)
      }
    }
  }

  private def successfulCalcHttpResponse(responseJson: Option[JsValue]): JsValue = {
    responseJson.get
  }

}
