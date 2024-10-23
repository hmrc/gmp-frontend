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
                                          (implicit encryption: Encryption): (String, EncryptedValue, EncryptedValue, EncryptedValue, Option[EncryptedValue], Option[EncryptedValue], EncryptedValue, Option[EncryptedValue], Instant) = {
    (
      singleCalculationSessionCache.id,
      encryption.crypto.encrypt(Json.toJson(singleCalculationSessionCache.memberDetails).toString, singleCalculationSessionCache.id),
      encryption.crypto.encrypt(singleCalculationSessionCache.scon, singleCalculationSessionCache.id),
      encryption.crypto.encrypt(singleCalculationSessionCache.scenario, singleCalculationSessionCache.id),
      singleCalculationSessionCache.revaluationDate.map(date => encryption.crypto.encrypt(Json.toJson(date).toString, singleCalculationSessionCache.id)),
      singleCalculationSessionCache.rate.map(rate => encryption.crypto.encrypt(rate, singleCalculationSessionCache.id)),
      encryption.crypto.encrypt(Json.toJson(singleCalculationSessionCache.leaving).toString, singleCalculationSessionCache.id),
      singleCalculationSessionCache.equalise.map(equalise => encryption.crypto.encrypt(equalise.toString, singleCalculationSessionCache.id)),
      singleCalculationSessionCache.lastModified
    )
  }

  def decryptSingleCalculationSessionCache(id: String,
                                           encryptedMemberDetails: EncryptedValue,
                                           encryptedScon: EncryptedValue,
                                           encryptedScenario: EncryptedValue,
                                           encryptedRevaluationDate: Option[EncryptedValue],
                                           encryptedRate: Option[EncryptedValue],
                                           encryptedLeaving: EncryptedValue,
                                           encryptedEqualise: Option[EncryptedValue],
                                           lastModified: Instant)(implicit encryption: Encryption): SingleCalculationSessionCache = {
    SingleCalculationSessionCache(
      id = id,
      memberDetails = Json.parse(encryption.crypto.decrypt(encryptedMemberDetails, id)).as[MemberDetails],
      scon = encryption.crypto.decrypt(encryptedScon, id),
      scenario = encryption.crypto.decrypt(encryptedScenario, id),
      revaluationDate = encryptedRevaluationDate.map(date => Json.parse(encryption.crypto.decrypt(date, id)).as[GmpDate]),
      rate = encryptedRate.map(rate => encryption.crypto.decrypt(rate, id)),
      leaving = Json.parse(encryption.crypto.decrypt(encryptedLeaving, id)).as[Leaving],
      equalise = encryptedEqualise.map(equalise => encryption.crypto.decrypt(equalise, id).toInt),
      lastModified = lastModified
    )
  }

}
