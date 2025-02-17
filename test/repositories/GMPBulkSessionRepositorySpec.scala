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

import com.google.inject.Inject
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.model.{IndexModel, IndexOptions, Indexes}
import org.mongodb.scala.{Document, MongoCollection}
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}
import models.upscan.{UploadStatus, UploadedSuccessfully}
import models.{GMPBulkSessionCache, GmpBulkSession}
import config.ApplicationConfig

import java.util.concurrent.TimeUnit
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class GMPBulkSessionRepositorySpec extends AnyFreeSpec with Matchers with ScalaFutures with IntegrationPatience with OptionValues
    with GuiceOneAppPerSuite with FutureAwaits with DefaultAwaitTimeout with BeforeAndAfterEach  {

  val repository: GMPBulkSessionRepository = app.injector.instanceOf[GMPBulkSessionRepository]
  val appConfig: ApplicationConfig = app.injector.instanceOf[ApplicationConfig]
  val id: String = "id"
  val callBackData: UploadStatus = UploadedSuccessfully("testReference", "testFileName", "testUrl")
  val emailAddress: String = "testData"
  val reference: String = "testData"
  val GMPBulkSession: GmpBulkSession =
    new GmpBulkSession(Some(callBackData), Some(emailAddress), Some(reference))
  val gmpBulkSessionCache: GMPBulkSessionCache = new GMPBulkSessionCache(id, GMPBulkSession)

  override def beforeEach(): Unit = {
    await(repository.collection.deleteMany(BsonDocument()).toFuture())
    super.beforeEach()
  }

//  "indexes" - {
//    "are correct" in {
//      repository.indexes.toList.toString() mustBe List(
//        IndexModel(
//          Indexes.ascending("lastModified"),
//          IndexOptions()
//            .name("lastModifiedIdx")
//            .expireAfter(900, TimeUnit.SECONDS)
//        )
//      ).toString()
//    }
//  }

  "resetIndexes" - {
    "should drop and recreate the lastModifiedIdx index" in {
      // Initial state: Ensure the index exists
      await(repository.resetIndexes())

      // Check indexes after resetting
      val indexesAfterReset = await(repository.collection.listIndexes().toFuture())
      indexesAfterReset.exists(_.getString("name") == "lastModifiedIdx") mustBe true

      // Drop all indexes
      await(repository.collection.dropIndexes().toFuture())

      // Ensure indexes are dropped
      val indexesAfterDrop = await(repository.collection.listIndexes().toFuture())
      indexesAfterDrop.exists(_.getString("name") == "lastModifiedIdx") mustBe false

      // Call resetIndexes again
      await(repository.resetIndexes())

      // Check indexes again after resetting
      val indexesAfterRecreation = await(repository.collection.listIndexes().toFuture())
      indexesAfterRecreation.exists(_.getString("name") == "lastModifiedIdx") mustBe true
    }
  }


  ".set" - {
    "Must successfully save a record to the DB" - {
      val result = await(repository.set(gmpBulkSessionCache))

      result mustEqual true
      val insertedModel = await(repository.get(gmpBulkSessionCache.id)).get
      insertedModel.id mustBe gmpBulkSessionCache.id
      insertedModel.gmpBulkSession.reference mustBe gmpBulkSessionCache.gmpBulkSession.reference
      insertedModel.gmpBulkSession.emailAddress mustBe gmpBulkSessionCache.gmpBulkSession.emailAddress
      insertedModel.gmpBulkSession.callBackData mustBe gmpBulkSessionCache.gmpBulkSession.callBackData
    }
  }

  ".get" - {
    "when there is a record for this id" - {
      "must return the correct record" in {
        await(repository.set(gmpBulkSessionCache))

        val insertedRecord = await(repository.get(gmpBulkSessionCache.id)).get
        insertedRecord.id mustBe gmpBulkSessionCache.id
        insertedRecord.gmpBulkSession.reference mustBe gmpBulkSessionCache.gmpBulkSession.reference
        insertedRecord.gmpBulkSession.emailAddress mustBe gmpBulkSessionCache.gmpBulkSession.emailAddress
        insertedRecord.gmpBulkSession.callBackData mustBe gmpBulkSessionCache.gmpBulkSession.callBackData
      }
    }

    "when there is no record for this id" - {
      "must return None" in {
        val nonExistingCache = gmpBulkSessionCache.copy(id = "non-existing-id")
        val result = await(repository.get(nonExistingCache.id))
        result mustBe None
      }
    }
  }


}
