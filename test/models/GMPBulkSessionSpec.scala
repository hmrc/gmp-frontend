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

package models

import play.api.libs.json.Json
import models.GMPBulkSession._
import services.Encryption
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}
import models.upscan.{UploadStatus, UploadedSuccessfully}
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper


class GMPBulkSessionSpec extends  AnyFreeSpec with Matchers with ScalaFutures with IntegrationPatience with OptionValues
with GuiceOneAppPerSuite with FutureAwaits with DefaultAwaitTimeout with BeforeAndAfterEach {

  implicit val encryption: Encryption = app.injector.instanceOf[Encryption]
  val id: String = "id"
  val callBackData: UploadStatus = UploadedSuccessfully("testReference", "testFileName", "testUrl")
  val emailAddress: String = "testData"
  val reference: String = "testData"

  val gmpBulkSession: GMPBulkSession = GMPBulkSession(
    id = "id",
    callBackData = Some(callBackData),
    emailAddress = Some(emailAddress),
    reference = Some(reference)
  )

  val gmpSessionCache: GMPBulkSessionCache = GMPBulkSessionCache(
    id = "id",
    gmpSession = gmpBulkSession
  )


    "Encrypt data" in {
      val result = ModelEncryption.encryptSessionCache(gmpSessionCache)

      result._1 mustBe gmpSessionCache.id
      Json
        .parse(encryption.crypto.decrypt(result._2, gmpSessionCache.id))
        .as[GMPBulkSession] mustBe gmpSessionCache.gmpSession
    }

    "Decrypt data into model" in {
      val result = ModelEncryption.decryptSessionCache(
        id = gmpSessionCache.id,
        gmpSession = encryption.crypto.encrypt(Json.toJson(gmpSessionCache.gmpSession).toString, gmpSessionCache.id)
      )
      result mustBe gmpSessionCache
    }

}









