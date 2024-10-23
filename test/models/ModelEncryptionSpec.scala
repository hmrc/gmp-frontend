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
      result._3 mustBe singleCalculationSessionCache.scon
      result._4 mustBe singleCalculationSessionCache.scenario
      result._5 mustBe singleCalculationSessionCache.revaluationDate
      result._6 mustBe singleCalculationSessionCache.rate
      result._7 mustBe singleCalculationSessionCache.leaving
      result._8 mustBe singleCalculationSessionCache.equalise
      result._9 mustBe singleCalculationSessionCache.lastModified
    }

    "Decrypt SingleCalculationSessionCache data" in {
      val result = ModelEncryption.decryptSingleCalculationSessionCache(
        id = singleCalculationSessionCache.id,
        encryptedMemberDetails = encryption.crypto.encrypt(Json.toJson(singleCalculationSessionCache.memberDetails).toString, singleCalculationSessionCache.id),
        scon = singleCalculationSessionCache.scon,
        scenario = singleCalculationSessionCache.scenario,
        revaluationDate = singleCalculationSessionCache.revaluationDate,
        rate = singleCalculationSessionCache.rate,
        leaving = singleCalculationSessionCache.leaving,
        equalise = singleCalculationSessionCache.equalise,
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
