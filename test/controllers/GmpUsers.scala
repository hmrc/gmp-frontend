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

import java.util.UUID

import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.PsaId
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.frontend.auth.connectors.domain._
import uk.gov.hmrc.play.http.SessionKeys

import scala.concurrent.Future

trait GmpUsers {

  implicit val mockAuthConnector: AuthConnector

  def withAuthorisedUser(test: FakeRequest[AnyContentAsEmpty.type] => Any) {
    val userId = s"user-${UUID.randomUUID}"
    when(mockAuthConnector.currentAuthority(Matchers.any())) thenReturn {
      Future.successful(Some(Authority(userId, Accounts(psa = Some(PsaAccount("gmp/B1234567", PsaId("B1234567")))), None, None, CredentialStrength.None, ConfidenceLevel.L50, None, None)))
    }
    val sessionId = s"session-${UUID.randomUUID}"
    lazy val request = FakeRequest().withSession(
      SessionKeys.sessionId -> sessionId,
      SessionKeys.token -> "RANDOMTOKEN",
      SessionKeys.userId -> userId)
    test(request)
  }

  def withAuthorisedUserAndPath(test: FakeRequest[AnyContentAsEmpty.type] => Any, method: String, path: String) {
    val userId = s"user-${UUID.randomUUID}"
    when(mockAuthConnector.currentAuthority(Matchers.any())) thenReturn {
      Future.successful(Some(Authority(userId, Accounts(psa = Some(PsaAccount("gmp/B1234567", PsaId("B1234567")))), None, None, CredentialStrength.None, ConfidenceLevel.L50, None, None)))
    }
    val sessionId = s"session-${UUID.randomUUID}"
    lazy val request = FakeRequest(method, path).withSession(
      SessionKeys.sessionId -> sessionId,
      SessionKeys.token -> "RANDOMTOKEN",
      SessionKeys.userId -> userId)
    test(request)
  }

  def withAuthorisedUserLowConfidenceLevel(test: FakeRequest[AnyContentAsEmpty.type] => Any) {
    val userId = s"user-${UUID.randomUUID}"
    when(mockAuthConnector.currentAuthority(Matchers.any())) thenReturn {
      Future.successful(Some(Authority(userId, Accounts(psa = Some(PsaAccount("gmp/B1234567", PsaId("B1234567")))), None, None, CredentialStrength.None,  ConfidenceLevel.L0, None, None)))
    }
    val sessionId = s"session-${UUID.randomUUID}"
    lazy val request = FakeRequest().withSession(
      SessionKeys.sessionId -> sessionId,
      SessionKeys.token -> "RANDOMTOKEN",
      SessionKeys.userId -> userId)
    test(request)
  }

}
