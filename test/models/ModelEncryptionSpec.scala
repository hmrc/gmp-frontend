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

import helpers.RandomNino
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.libs.json.Json
import services.Encryption
import models.upscan.{UploadStatus, UploadedSuccessfully}

import java.time.{Instant, LocalDate}

class ModelEncryptionSpec extends PlaySpec with GuiceOneServerPerSuite {
  implicit val encryption: Encryption = app.injector.instanceOf[Encryption]

  val currentDateGmp: GmpDate = {
    val now = LocalDate.now
    GmpDate(Some(now.getDayOfMonth.toString), Some(now.getMonthValue.toString), Some(now.getYear.toString))
  }

  val memberDetails: MemberDetails = MemberDetails("John", "Doe", RandomNino.generate)
  val leaving: Leaving = Leaving(currentDateGmp, Some(Leaving.YES_BEFORE))
  val scon = "S123456789T"
  val scenario = "Scenario 1"
  val rate = "4.5%"
  val equalise = 1


  val gmpSession: GmpSession = GmpSession(
    memberDetails = memberDetails,
    scon = scon,
    scenario = scenario,
    revaluationDate = Some(currentDateGmp),
    rate = Some(rate),
    leaving = leaving,
    equalise = Some(equalise)
  )

  val singleCalculationSessionCache: SingleCalculationSessionCache = SingleCalculationSessionCache(
    id = "id",
    gmpSession = gmpSession,
    lastModified = Instant.ofEpochSecond(1)
  )

  val id: String = "id"
  val callBackData: UploadStatus = UploadedSuccessfully("testReference", "testFileName", "testUrl")
  val emailAddress: String = "testData"
  val reference: String = "testData"

  val gmpBulkSession: GmpBulkSession = GmpBulkSession(
    callBackData = Some(callBackData),
    emailAddress = Some(emailAddress),
    reference = Some(reference)
  )
  val gmpBulkSessionCache: GMPBulkSessionCache = GMPBulkSessionCache(
    id = "id",
    gmpBulkSession = gmpBulkSession,
    lastModified = Instant.ofEpochSecond(1)
  )

  "SingleCalculationSessionCacheEncryption" should {

    "Encrypt SingleCalculationSessionCache data" in {
      val result = ModelEncryption.encryptSingleCalculationSessionCache(singleCalculationSessionCache)

      result._1 mustBe singleCalculationSessionCache.id
      Json.parse(encryption.crypto.decrypt(result._2, singleCalculationSessionCache.id)).as[GmpSession] mustBe singleCalculationSessionCache.gmpSession
      result._3 mustBe singleCalculationSessionCache.lastModified
    }

    "Decrypt SingleCalculationSessionCache data" in {
      val result = ModelEncryption.decryptSingleCalculationSessionCache(
        id = singleCalculationSessionCache.id,
        gmpSession = encryption.crypto.encrypt(Json.toJson(singleCalculationSessionCache.gmpSession).toString, singleCalculationSessionCache.id),
        lastModified = singleCalculationSessionCache.lastModified
      )

      result mustBe singleCalculationSessionCache
    }
  }



  "GmpBulkSessionCacheEncryption" should {

    "Encrypt GmpBulkSessionCache data" in {
      val result = ModelEncryption.encryptSessionCache(gmpBulkSessionCache)
      result._1 mustBe gmpBulkSessionCache.id
      Json.parse(encryption.crypto.decrypt(result._2, gmpBulkSessionCache.id)).as[GmpBulkSession] mustBe gmpBulkSessionCache.gmpBulkSession
      result._3 mustBe gmpBulkSessionCache.lastModified
    }

    "Decrypt GmpBulkSessionCache data into model" in {
      val result = ModelEncryption.decryptSessionCache(
        id = gmpBulkSessionCache.id,
        gmpBulkSession = encryption.crypto.encrypt(Json.toJson(gmpBulkSessionCache.gmpBulkSession).toString, gmpBulkSessionCache.id),
        lastModified = gmpBulkSessionCache.lastModified
      )
      result mustBe gmpBulkSessionCache
    }
  }
}
