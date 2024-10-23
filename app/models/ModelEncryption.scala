package models

import models.GMPBulkSession
import models.upscan.UploadStatus
import play.api.libs.json._
import uk.gov.hmrc.crypto.EncryptedValue
import services.Encryption


object ModelEncryption {

  def encryptGMPBulkSession(GMPBulkSession: GMPBulkSession)
                           (implicit encryption: Encryption): (String, Option[EncryptedValue], Option[EncryptedValue], Option[EncryptedValue]) = {
    val encryptCallBackData:Option[EncryptedValue] = GMPBulkSession.callBackData match  {
      case Some(callBackData) => val serialized = Json.toJson(callBackData).toString()
        Some(encryption.crypto.encrypt(serialized, GMPBulkSession.id))
      case None => None
    }

    val encryptEmailAddress: Option[EncryptedValue] = GMPBulkSession.emailAddress match {
      case Some(email) => Some(encryption.crypto.encrypt(email, GMPBulkSession.id))
      case None => None
    }

    val encryptReference: Option[EncryptedValue] = GMPBulkSession.reference match {
      case Some(reference) => Some(encryption.crypto.encrypt(reference, GMPBulkSession.id))
    }

    (GMPBulkSession.id, encryptEmailAddress, encryptCallBackData, encryptReference)
  }

  def decryptGMPBulkSession(
                             id:String, encryptedCallbackData:Option[EncryptedValue], encryptedEmailAddress:Option[EncryptedValue], encryptedReference: Option[EncryptedValue])
                           (implicit encryption: Encryption): GMPBulkSession = {

    val decryptCallBackData: Option[UploadStatus] = encryptedCallbackData match {
      case Some(encryptedCallBackData) =>
        val decryptedString = encryption.crypto.decrypt(encryptedCallBackData, id)
        Json.parse(decryptedString).asOpt[UploadStatus]
      case None => None
    }

    val decryptEmailAddress: Option[String] = encryptedEmailAddress match {
      case Some(encryptedEmailAddress) => Some(encryption.crypto.decrypt(encryptedEmailAddress, id))
      case None => None
    }

    val decryptReference: Option[String] = encryptedReference match {
      case Some(encryptedReference) => Some(encryption.crypto.decrypt(encryptedReference, id))
      case None => None
    }

    GMPBulkSession(
      id = id,
      callBackData = decryptCallBackData,
      emailAddress = decryptEmailAddress,
      reference = decryptReference
    )
  }

  def encryptSessionCache(sessionCache: GMPBulkSessionCache)(implicit encryption: Encryption): (String, EncryptedValue) =
    (sessionCache.id, encryption.crypto.encrypt(Json.toJson(sessionCache.gmpSession).toString, sessionCache.id))

  def decryptSessionCache(id: String, gmpSession: EncryptedValue)(implicit encryption: Encryption): GMPBulkSessionCache =
    GMPBulkSessionCache(
      id = id,
      gmpSession = Json.parse(encryption.crypto.decrypt(gmpSession, id)).as[GMPBulkSession]
    )
}