/*
 * Copyright 2019 HM Revenue & Customs
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

///*
// * Copyright 2019 HM Revenue & Customs
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package controllers
//
//import java.util.UUID
//
//import controllers.auth.AuthenticatedRequest
//import play.api.mvc.AnyContentAsEmpty
//import play.api.test.FakeRequest
//import uk.gov.hmrc.http.SessionKeys
//
//trait GmpUsers {
//
//
//  def withAuthorisedUser(test: FakeRequest[AnyContentAsEmpty.type] => Any) {
//    val userId = s"user-${UUID.randomUUID}"
//
//    val sessionId = s"session-${UUID.randomUUID}"
//    lazy val request = FakeRequest().withSession(
//      SessionKeys.sessionId -> sessionId,
//      SessionKeys.token -> "RANDOMTOKEN",
//      SessionKeys.userId -> userId)
//    AuthenticatedRequest("gmp/B1234567", request)
//  }
//
//  def withAuthorisedUserAndPath(test: FakeRequest[AnyContentAsEmpty.type] => Any, method: String, path: String) {
//    val userId = s"user-${UUID.randomUUID}"
//
//    val sessionId = s"session-${UUID.randomUUID}"
//    lazy val request = FakeRequest(method, path).withSession(
//      SessionKeys.sessionId -> sessionId,
//      SessionKeys.token -> "RANDOMTOKEN",
//      SessionKeys.userId -> userId)
//    AuthenticatedRequest("gmp/B1234567", request)
//  }
//
//  def withAuthorisedUserLowConfidenceLevel(test: FakeRequest[AnyContentAsEmpty.type] => Any) {
//    val userId = s"user-${UUID.randomUUID}"
//
//    val sessionId = s"session-${UUID.randomUUID}"
//    lazy val request = FakeRequest().withSession(
//      SessionKeys.sessionId -> sessionId,
//      SessionKeys.token -> "RANDOMTOKEN",
//      SessionKeys.userId -> userId)
//    AuthenticatedRequest("gmp/B1234567", request)
//  }
//
//}
