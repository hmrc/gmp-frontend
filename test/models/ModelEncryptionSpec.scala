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
import repositories.DatedCacheMap
import services.Encryption

import java.time.{Instant, LocalDate, LocalDateTime}

class ModelEncryptionSpec extends PlaySpec with GuiceOneServerPerSuite {
  implicit val encryption: Encryption = app.injector.instanceOf[Encryption]

  val currentDateGmp: GmpDate = {
    val now = LocalDate.now
    GmpDate(Some(now.getDayOfMonth.toString), Some(now.getMonthValue.toString), Some(now.getYear.toString))
  }

  val memberDetails: MemberDetails = MemberDetails("John", "Doe", RandomNino.generate)
  val leaving: Leaving = Leaving(currentDateGmp, Some(Leaving.YES_BEFORE))

  val singleCalculationSessionCache: SingleCalculationSessionCache = SingleCalculationSessionCache(
    id = "id",
    memberDetails = memberDetails,
    scon = "S123456789T",
    scenario = "Scenario 1",
    revaluationDate = Some(currentDateGmp),
    rate = Some("4.5%"),
    leaving = leaving,
    equalise = Some(1),
    lastModified = Instant.ofEpochSecond(1)
  )

  "SingleCalculationSessionCacheEncryption" should {

    "Encrypt SingleCalculationSessionCache data" in {
      val result = ModelEncryption.encryptSingleCalculationSessionCache(singleCalculationSessionCache)

      result._1 mustBe singleCalculationSessionCache.id
      Json.parse(encryption.crypto.decrypt(result._2, singleCalculationSessionCache.id)).as[MemberDetails] mustBe singleCalculationSessionCache.memberDetails
      encryption.crypto.decrypt(result._3, singleCalculationSessionCache.id) mustBe singleCalculationSessionCache.scon
      encryption.crypto.decrypt(result._4, singleCalculationSessionCache.id) mustBe singleCalculationSessionCache.scenario
      result._5.map(date => Json.parse(encryption.crypto.decrypt(date, singleCalculationSessionCache.id)).as[GmpDate]) mustBe singleCalculationSessionCache.revaluationDate
      result._6.map(rate => encryption.crypto.decrypt(rate, singleCalculationSessionCache.id)) mustBe singleCalculationSessionCache.rate
      Json.parse(encryption.crypto.decrypt(result._7, singleCalculationSessionCache.id)).as[Leaving] mustBe singleCalculationSessionCache.leaving
      result._8.map(equalise => encryption.crypto.decrypt(equalise, singleCalculationSessionCache.id).toInt) mustBe singleCalculationSessionCache.equalise
      result._9 mustBe singleCalculationSessionCache.lastModified
    }

    "Decrypt SingleCalculationSessionCache data" in {
      val result = ModelEncryption.decryptSingleCalculationSessionCache(
        id = singleCalculationSessionCache.id,
        encryptedMemberDetails = encryption.crypto.encrypt(Json.toJson(singleCalculationSessionCache.memberDetails).toString, singleCalculationSessionCache.id),
        encryptedScon = encryption.crypto.encrypt(singleCalculationSessionCache.scon, singleCalculationSessionCache.id),
        encryptedScenario = encryption.crypto.encrypt(singleCalculationSessionCache.scenario, singleCalculationSessionCache.id),
        encryptedRevaluationDate = singleCalculationSessionCache.revaluationDate.map(date => encryption.crypto.encrypt(Json.toJson(date).toString, singleCalculationSessionCache.id)),
        encryptedRate = singleCalculationSessionCache.rate.map(rate => encryption.crypto.encrypt(rate, singleCalculationSessionCache.id)),
        encryptedLeaving = encryption.crypto.encrypt(Json.toJson(singleCalculationSessionCache.leaving).toString, singleCalculationSessionCache.id),
        encryptedEqualise = singleCalculationSessionCache.equalise.map(equalise => encryption.crypto.encrypt(equalise.toString, singleCalculationSessionCache.id)),
        lastModified = singleCalculationSessionCache.lastModified
      )

      result mustBe singleCalculationSessionCache
    }
  }

  "DatedCacheMapEncrpytion" should {
    "Encrypt Data" in {
      val datedCacheMap: DatedCacheMap = DatedCacheMap(
        "foo",
        Map("string" -> Json.obj("foo" -> "bar")),
        Instant.now()
      )

      val result = ModelEncryption.encryptDatedCacheMap(datedCacheMap)
      result._1 mustBe datedCacheMap.id
      result._2.head._1 mustBe datedCacheMap.data.head._1
      Json.parse(encryption.crypto.decrypt(result._2.head._2, datedCacheMap.id)) mustBe datedCacheMap.data.head._2
      result._3 mustBe result._3
    }
    "Decrypt Data" in {
      val datedCacheMap: DatedCacheMap = DatedCacheMap(
        "foo",
        Map("string" -> Json.obj("foo" -> "bar")),
        Instant.now()
      )

      val result = ModelEncryption.decryptDatedCacheMap(
        datedCacheMap.id,
        datedCacheMap.data.map(item => item._1 -> encryption.crypto.encrypt(item._2.toString(), datedCacheMap.id)),
        datedCacheMap.lastUpdated
      )
      result mustBe datedCacheMap
    }
  }
}
