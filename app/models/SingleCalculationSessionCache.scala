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
import services.Encryption
import uk.gov.hmrc.crypto.EncryptedValue
import uk.gov.hmrc.crypto.json.CryptoFormats

import java.time.Instant

case class SingleCalculationSessionCache(
                                          id: String,
                                          gmpSession: GmpSession,
                                          lastModified: Instant = Instant.now()
                                        )

object SingleCalculationSessionCache {
  object MongoFormats {
    import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats.Implicits._
    implicit val cryptEncryptedValueFormats: Format[EncryptedValue] = CryptoFormats.encryptedValueFormat

    def reads(implicit encryption: Encryption): Reads[SingleCalculationSessionCache] = {
      (
        (__ \ "id").read[String] and
          (__ \ "gmpSession").read[GmpSession] and
          (__ \ "lastModified").read[Instant]
        )(ModelEncryption.decryptSingleCalculationSessionCache _)
    }

    def writes(implicit encryption: Encryption): OWrites[SingleCalculationSessionCache] = new OWrites[SingleCalculationSessionCache] {
      override def writes(singleCalculationSessionCache: SingleCalculationSessionCache): JsObject = {
        val encryptedValues: (String, GmpSession, Instant) =
          ModelEncryption.encryptSingleCalculationSessionCache(singleCalculationSessionCache)
        Json.obj(
          "id" -> encryptedValues._1,
          "gmpSession" -> encryptedValues._2,
          "lastModified" -> encryptedValues._3
        )
      }
    }

    def formats(implicit encryption: Encryption): OFormat[SingleCalculationSessionCache] = OFormat(reads, writes)
  }
}