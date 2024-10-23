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

import play.api.libs.json.Json
import repositories.DatedCacheMap
import services.Encryption
import uk.gov.hmrc.crypto.EncryptedValue

import java.time.Instant

object ModelEncryption {

  def encryptSessionCache(sessionCache: GMPBulkSessionCache)(implicit encryption: Encryption): (String, EncryptedValue) =
    (sessionCache.id, encryption.crypto.encrypt(Json.toJson(sessionCache.gmpSession).toString, sessionCache.id))

  def decryptSessionCache(id: String, gmpSession: EncryptedValue)(implicit encryption: Encryption): GMPBulkSessionCache =
    GMPBulkSessionCache(
      id = id,
      gmpSession = Json.parse(encryption.crypto.decrypt(gmpSession, id)).as[GMPBulkSession]
    )
  def encryptDatedCacheMap(datedCacheMap: DatedCacheMap)(implicit encryption: Encryption): (String, Map[String, EncryptedValue], Instant) = {
    (
      datedCacheMap.id,
      datedCacheMap.data.map(item => item._1 -> encryption.crypto.encrypt(item._2.toString(), datedCacheMap.id)),
      datedCacheMap.lastUpdated
    )
  }

  def decryptDatedCacheMap(id: String,
                           data: Map[String, EncryptedValue],
                           lastUpdated: Instant)(implicit encryption: Encryption): DatedCacheMap = {
    DatedCacheMap(
      id = id,
      data = data.map(item => item._1 -> Json.parse(encryption.crypto.decrypt(item._2, id))),
      lastUpdated = lastUpdated
    )
  }


  def encryptSingleCalculationSessionCache(singleCalculationSessionCache: SingleCalculationSessionCache)
                                          (implicit encryption: Encryption): (String, EncryptedValue, String, String, Option[GmpDate], Option[String], Leaving, Option[Int], Instant) = {
    (
      singleCalculationSessionCache.id,
      encryption.crypto.encrypt(Json.toJson(singleCalculationSessionCache.memberDetails).toString, singleCalculationSessionCache.id),
      singleCalculationSessionCache.scon,
      singleCalculationSessionCache.scenario,
      singleCalculationSessionCache.revaluationDate,
      singleCalculationSessionCache.rate,
      singleCalculationSessionCache.leaving,
      singleCalculationSessionCache.equalise,
      singleCalculationSessionCache.lastModified
    )
  }

  def decryptSingleCalculationSessionCache(id: String,
                                           encryptedMemberDetails: EncryptedValue,
                                           scon: String,
                                           scenario: String,
                                           revaluationDate: Option[GmpDate],
                                           rate: Option[String],
                                           leaving: Leaving,
                                           equalise: Option[Int],
                                           lastModified: Instant)(implicit encryption: Encryption): SingleCalculationSessionCache = {
    SingleCalculationSessionCache(
      id = id,
      memberDetails = Json.parse(encryption.crypto.decrypt(encryptedMemberDetails, id)).as[MemberDetails],
      scon = scon,
      scenario = scenario,
      revaluationDate = revaluationDate,
      rate = rate,
      leaving = leaving,
      equalise = equalise,
      lastModified = lastModified
    )
  }

}
