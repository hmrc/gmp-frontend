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
                                          memberDetails: MemberDetails,
                                          scon: String,
                                          scenario: String,
                                          revaluationDate: Option[GmpDate],
                                          rate: Option[String],
                                          leaving: Leaving,
                                          equalise: Option[Int],
                                          lastModified: Instant = Instant.now()
                                        )

object SingleCalculationSessionCache {
  object MongoFormats {
    import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats.Implicits._
    implicit val cryptEncryptedValueFormats: Format[EncryptedValue] = CryptoFormats.encryptedValueFormat

    def reads()(implicit encryption: Encryption): Reads[SingleCalculationSessionCache] = (
      (__ \ "id").read[String] and
        (__ \ "memberDetails").read[EncryptedValue] and
        (__ \ "scon").read[String] and
        (__ \ "scenario").read[String] and
        (__ \ "revaluationDate").readNullable[GmpDate] and
        (__ \ "rate").readNullable[String] and
        (__ \ "leaving").read[Leaving] and
        (__ \ "equalise").readNullable[Int] and
        (__ \ "lastModified").read[Instant]
      )(ModelEncryption.decryptSingleCalculationSessionCache _)

    def writes(implicit encryption: Encryption): OWrites[SingleCalculationSessionCache] = new OWrites[SingleCalculationSessionCache] {
      override def writes(singleCalculationSessionCache: SingleCalculationSessionCache): JsObject = {
        val encryptedValue: (String, EncryptedValue, String, String, Option[GmpDate], Option[String], Leaving, Option[Int], Instant) = {
          ModelEncryption.encryptSingleCalculationSessionCache(singleCalculationSessionCache)
        }
        Json.obj(
          "id" -> encryptedValue._1,
          "memberDetails" -> encryptedValue._2,
          "scon" -> encryptedValue._3,
          "scenario" -> encryptedValue._4,
          "revaluationDate" -> encryptedValue._5,
          "rate" -> encryptedValue._6,
          "leaving" -> encryptedValue._7,
          "equalise" -> encryptedValue._8,
          "lastModified" -> encryptedValue._9
        )
      }
    }

    def formats(implicit encryption: Encryption): OFormat[SingleCalculationSessionCache] = OFormat(reads(), writes)
  }
}