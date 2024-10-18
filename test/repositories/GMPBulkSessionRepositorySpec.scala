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


import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.model.{IndexModel, IndexOptions, Indexes}
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}
import models.upscan.{UploadStatus, UploadedSuccessfully}
import models.GMPBulkSession
import java.util.concurrent.TimeUnit

class GMPBulkSessionRepositorySpec
  extends AnyFreeSpec with Matchers with ScalaFutures with IntegrationPatience with OptionValues
    with GuiceOneAppPerSuite with FutureAwaits with DefaultAwaitTimeout with BeforeAndAfterEach {

  val repository: GMPBulkSessionRepository = app.injector.instanceOf[GMPBulkSessionRepository]
  val id: String = "id"
  val callBackData: UploadStatus = UploadedSuccessfully("testReference", "testFileName", "testUrl")
  val emailAddress: String = "testData"
  val reference: String = "testData"
  val GMPBulkSession: GMPBulkSession =
    new GMPBulkSession(id, Some(callBackData), Some(emailAddress), Some(reference))

  override def beforeEach(): Unit = {
    await(repository.collection.deleteMany(BsonDocument()).toFuture())
    super.beforeEach()
  }

  "indexes" - {
    "are correct" in {
      repository.indexes.toList.toString() mustBe List(
        IndexModel(
          Indexes.ascending("lastModified"),
          IndexOptions()
            .name("lastModified")
            .expireAfter(900, TimeUnit.SECONDS)
        )
      ).toString()
    }
  }

  ".set" - {
    "Must successfully save a record to the DB" - {
      val result = await(repository.set(GMPBulkSession))

      result mustEqual true

      val insertedModel = await(repository.get(GMPBulkSession.id)).get
      insertedModel.id mustBe GMPBulkSession.id
      insertedModel.reference mustBe GMPBulkSession.reference
      insertedModel.emailAddress mustBe GMPBulkSession.emailAddress
      insertedModel.callBackData mustBe GMPBulkSession.callBackData
    }
  }

  ".get" - {
    "when there is a record for this id" - {
      "must return the correct record" in {
        await(repository.set(GMPBulkSession : GMPBulkSession))

        val insertedRecord = await(repository.get(GMPBulkSession.id)).get
        insertedRecord.id mustBe GMPBulkSession.id
        insertedRecord.reference mustBe GMPBulkSession.reference
        insertedRecord.emailAddress mustBe GMPBulkSession.emailAddress
        insertedRecord.callBackData mustBe GMPBulkSession.callBackData
      }
    }

    "when there is no record for this id" - {
      "must return None" in {
        repository.get("id that does not exist").futureValue must not be defined
      }
    }

  }
}
