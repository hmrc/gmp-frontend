package models


import play.api.libs.json._
import uk.gov.hmrc.crypto.EncryptedValue
import services.Encryption


object ModelEncryption {

  def encryptSessionCache(sessionCache: GMPBulkSessionCache)(implicit encryption: Encryption): (String, EncryptedValue) =
    (sessionCache.id, encryption.crypto.encrypt(Json.toJson(sessionCache.gmpSession).toString, sessionCache.id))

  def decryptSessionCache(id: String, gmpSession: EncryptedValue)(implicit encryption: Encryption): GMPBulkSessionCache =
    GMPBulkSessionCache(
      id = id,
      gmpSession = Json.parse(encryption.crypto.decrypt(gmpSession, id)).as[GMPBulkSession]
    )
}