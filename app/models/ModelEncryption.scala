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


import play.api.libs.json._
import uk.gov.hmrc.crypto.EncryptedValue
import services.Encryption
import java.time.Instant


object ModelEncryption {

  def encryptSessionCache(gmpBulkSessionCache: GMPBulkSessionCache)(implicit encryption: Encryption): (String, EncryptedValue, Instant) =
    (gmpBulkSessionCache.id,
      encryption.crypto.encrypt(Json.toJson(gmpBulkSessionCache.gmpBulkSession).toString, gmpBulkSessionCache.id),
      gmpBulkSessionCache.lastModified
    )

  def decryptSessionCache(id: String, gmpBulkSession: EncryptedValue, lastModified: Instant)(implicit encryption: Encryption): GMPBulkSessionCache =
    GMPBulkSessionCache(
      id = id,
      gmpBulkSession = Json.parse(encryption.crypto.decrypt(gmpBulkSession, id)).as[GmpBulkSession],
      lastModified = lastModified
    )

  def encryptSingleCalculationSessionCache(singleCalculationSessionCache: SingleCalculationSessionCache)
                                          (implicit encryption: Encryption): (String, EncryptedValue, Instant) =
    (
      singleCalculationSessionCache.id,
      encryption.crypto.encrypt(Json.toJson(singleCalculationSessionCache.gmpSession).toString, singleCalculationSessionCache.id),
      singleCalculationSessionCache.lastModified
    )


  def decryptSingleCalculationSessionCache(id: String, gmpSession: EncryptedValue, lastModified: Instant)
                                          (implicit encryption: Encryption): SingleCalculationSessionCache =
    SingleCalculationSessionCache(
      id = id,
      gmpSession = Json.parse(encryption.crypto.decrypt(gmpSession, id)).as[GmpSession],
      lastModified = lastModified
    )
}