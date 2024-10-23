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

package repositories

import helpers.RandomNino
import models.{GmpDate, Leaving, MemberDetails, SingleCalculationSessionCache}
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.model.{IndexModel, IndexOptions, Indexes}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{Format, JsObject, Json}
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}
import services.Encryption
import uk.gov.hmrc.crypto.EncryptedValue
import uk.gov.hmrc.crypto.json.CryptoFormats

import java.time._
import java.util.concurrent.TimeUnit

class SingleCalculationSessionRepositorySpec
  extends AnyFreeSpec
    with Matchers
    with ScalaFutures
    with IntegrationPatience
    with OptionValues with GuiceOneAppPerSuite with FutureAwaits with DefaultAwaitTimeout with BeforeAndAfterEach {

  override lazy val app: Application = GuiceApplicationBuilder()
    .overrides(bind[Clock].toInstance(Clock.systemDefaultZone().withZone(ZoneOffset.UTC)))
    .build()

  val repository: SingleCalculationSessionRepository = app.injector.instanceOf[SingleCalculationSessionRepository]
  val encryption: Encryption = app.injector.instanceOf[Encryption]
  implicit val cryptEncryptedValueFormats: Format[EncryptedValue]  = CryptoFormats.encryptedValueFormat

  override def beforeEach(): Unit = {
    await(repository.collection.deleteMany(BsonDocument()).toFuture())
    super.beforeEach()
  }

  val currentDateGmp: GmpDate = {
    val now = LocalDate.now
    GmpDate(Some(now.getDayOfMonth.toString), Some(now.getMonthValue.toString), Some(now.getYear.toString))
  }

  val memberDetails: MemberDetails = MemberDetails("John", "Doe", RandomNino.generate)
  val leaving: Leaving = Leaving(currentDateGmp, Some(Leaving.YES_BEFORE))

  "indexes" - {
    "are correct" in {
      repository.indexes.toList.toString() mustBe List(IndexModel(
        Indexes.ascending("lastModified"),
        IndexOptions()
          .name("lastModifiedIdx")
          .expireAfter(900, TimeUnit.SECONDS)
      )).toString()
    }
  }

  ".set" - {
    "must set the last updated time on the supplied session cache to `now`, and save it" in {
      val sessionCacheBefore: SingleCalculationSessionCache = SingleCalculationSessionCache(
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
      val timeBeforeTest = Instant.now()
      val setResult = await(repository.set(sessionCacheBefore))
      val updatedRecord = await(repository.get(sessionCacheBefore.id)).get
      lazy val timeAfterTest = Instant.now()

      setResult mustEqual true
      assert(updatedRecord.lastModified.toEpochMilli > timeBeforeTest.toEpochMilli || updatedRecord.lastModified.toEpochMilli == timeBeforeTest.toEpochMilli)
      assert(updatedRecord.lastModified.toEpochMilli < timeAfterTest.toEpochMilli || updatedRecord.lastModified.toEpochMilli == timeAfterTest.toEpochMilli)

      updatedRecord.id mustBe sessionCacheBefore.id
      updatedRecord.memberDetails mustBe sessionCacheBefore.memberDetails
      updatedRecord.scon mustBe sessionCacheBefore.scon
      updatedRecord.scenario mustBe sessionCacheBefore.scenario
      updatedRecord.revaluationDate mustBe sessionCacheBefore.revaluationDate
      updatedRecord.rate mustBe sessionCacheBefore.rate
      updatedRecord.leaving mustBe sessionCacheBefore.leaving
      updatedRecord.equalise mustBe sessionCacheBefore.equalise
    }

    "correctly encrypt the session cache data" in {
      val sessionCacheBefore: SingleCalculationSessionCache = SingleCalculationSessionCache(
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

      val setResult = await(repository.set(sessionCacheBefore))
      setResult mustEqual true

      val updatedRecord = await(repository.collection.find[BsonDocument](BsonDocument()).toFuture()).head
      val resultParsedToJson = Json.parse(updatedRecord.toJson).as[JsObject]

      val memberDetailsDecrypted = {
        Json.parse(encryption.crypto.decrypt((resultParsedToJson \ "memberDetails").as[EncryptedValue], sessionCacheBefore.id)).as[MemberDetails]
      }

      memberDetailsDecrypted mustBe sessionCacheBefore.memberDetails
    }
  }

  ".get" - {

    "when there is a record for this id" - {

      "must update the lastModified time and get the record" in {
        val sessionCacheBefore: SingleCalculationSessionCache = SingleCalculationSessionCache(
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
        await(repository.set(sessionCacheBefore))

        val timeBeforeTest = Instant.now()
        val updatedRecord = await(repository.get(sessionCacheBefore.id)).get
        lazy val timeAfterTest = Instant.now()

        assert(updatedRecord.lastModified.toEpochMilli > timeBeforeTest.toEpochMilli || updatedRecord.lastModified.toEpochMilli == timeBeforeTest.toEpochMilli)
        assert(updatedRecord.lastModified.toEpochMilli < timeAfterTest.toEpochMilli || updatedRecord.lastModified.toEpochMilli == timeAfterTest.toEpochMilli)

        updatedRecord.id mustBe sessionCacheBefore.id
        updatedRecord.memberDetails mustBe sessionCacheBefore.memberDetails
        updatedRecord.scon mustBe sessionCacheBefore.scon
        updatedRecord.scenario mustBe sessionCacheBefore.scenario
        updatedRecord.revaluationDate mustBe sessionCacheBefore.revaluationDate
        updatedRecord.rate mustBe sessionCacheBefore.rate
        updatedRecord.leaving mustBe sessionCacheBefore.leaving
        updatedRecord.equalise mustBe sessionCacheBefore.equalise
      }
    }

    "when there is no record for this id" - {
      "must return None" in {
        repository.get("id that does not exist").futureValue must not be defined
      }
    }
  }

  ".clear" - {

    "must remove a record" in {
      val sessionCacheBefore: SingleCalculationSessionCache = SingleCalculationSessionCache(
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
      repository.set(sessionCacheBefore).futureValue

      val result = repository.clear(sessionCacheBefore.id).futureValue

      result mustEqual true
      repository.get(sessionCacheBefore.id).futureValue must not be defined
    }

    "must return true when there is no record to remove" in {
      val result = repository.clear("id that does not exist").futureValue

      result mustEqual true
    }
  }
}
