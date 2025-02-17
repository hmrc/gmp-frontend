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

import config.ApplicationConfig
import models.SingleCalculationSessionCache
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model._
import play.api.libs.json.Format
import services.Encryption
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats
import java.time.{Clock, Instant}
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SingleCalculationSessionRepository @Inject()(
                                                mongoComponent: MongoComponent,
                                                appConfig: ApplicationConfig,
                                                clock: Clock
                                              )(implicit ec: ExecutionContext, encryption: Encryption)
  extends PlayMongoRepository[SingleCalculationSessionCache](
    collectionName = "single-calculation-session-cache",
    mongoComponent = mongoComponent,
    domainFormat = SingleCalculationSessionCache.MongoFormats.formats,
    indexes = Seq(
      IndexModel(
        Indexes.ascending("lastModified"),
        IndexOptions()
          .name("lastModifiedIdx")
          .expireAfter(appConfig.cacheTtl, TimeUnit.SECONDS)
      )
    )
  ) {
  implicit val instantFormat: Format[Instant] = MongoJavatimeFormats.instantFormat

  private def byId(id: String): Bson = Filters.equal("_id", id)

  private def keepAlive(id: String): Future[Boolean] =
    collection
      .updateOne(
        filter = byId(id),
        update = Updates.set("lastModified", Instant.now(clock))
      )
      .toFuture()
      .map(_ => true)

  def get(id: String): Future[Option[SingleCalculationSessionCache]] = {
    for {
      _ <- keepAlive(id)
      optUserAnswers <- collection.find(byId(id)).headOption()
    } yield optUserAnswers
  }

  def set(answers: SingleCalculationSessionCache): Future[Boolean] = {

    val updatedAnswers = answers copy (lastModified = Instant.now(clock))

    collection
      .replaceOne(
        filter = byId(updatedAnswers.id),
        replacement = updatedAnswers,
        options = ReplaceOptions().upsert(true)
      )
      .toFuture()
      .map(_ => true)
  }

  def clear(id: String): Future[Boolean] =
    collection
      .deleteOne(byId(id))
      .toFuture()
      .map(_ => true)

}


