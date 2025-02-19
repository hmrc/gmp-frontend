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

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json._
import uk.gov.hmrc.crypto.EncryptedValue
import uk.gov.hmrc.crypto.json.CryptoFormats
import services.Encryption

import java.time.Instant

case class GMPBulkSessionCache(
   id: String,
   gmpBulkSession: GmpBulkSession,
   lastModified: Instant = Instant.now()
)

object GMPBulkSessionCache {
  object MongoFormats {
    import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats.Implicits._
    implicit val cryptEncryptedValueFormats: Format[EncryptedValue] = CryptoFormats.encryptedValueFormat

    def reads()(implicit encryption: Encryption): Reads[GMPBulkSessionCache] =
      (
        (__ \ "id").read[String] and
          (__ \ "gmpBulkSession").read[GmpBulkSession] and
          (__ \ "lastModified").read[Instant]
        )(ModelEncryption.decryptSessionCache _)

    def writes(implicit encryption: Encryption): OWrites[GMPBulkSessionCache] =
      new OWrites[GMPBulkSessionCache] {

        override def writes(sessionCache: GMPBulkSessionCache): JsObject = {
          val encryptedValue: (String, GmpBulkSession, Instant) =
            ModelEncryption.encryptSessionCache(sessionCache)
          Json.obj(
            "id" -> encryptedValue._1,
            "gmpBulkSession" -> encryptedValue._2,
            "lastModified" -> encryptedValue._3
          )
        }
      }

    def formats(implicit encryption: Encryption): OFormat[GMPBulkSessionCache] = OFormat(reads(), writes)
  }
}
