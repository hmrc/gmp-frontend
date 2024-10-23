package models

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json._
import uk.gov.hmrc.crypto.EncryptedValue
import uk.gov.hmrc.crypto.json.CryptoFormats
import services.Encryption

case class GMPBulkSessionCache(
   id: String,
   gmpSession: GMPBulkSession
)

object GMPBulkSessionCache {
  object MongoFormats {
    import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats.Implicits._
    implicit val cryptEncryptedValueFormats: Format[EncryptedValue] = CryptoFormats.encryptedValueFormat

    def reads()(implicit encryption: Encryption): Reads[GMPBulkSessionCache] =
      (
        (__ \ "id").read[String] and
          (__ \ "gmpSession").read[EncryptedValue]
        )(ModelEncryption.decryptSessionCache _)

    def writes(implicit encryption: Encryption): OWrites[GMPBulkSessionCache] =
      new OWrites[GMPBulkSessionCache] {

        override def writes(sessionCache: GMPBulkSessionCache): JsObject = {
          val encryptedValue: (String, EncryptedValue) =
            ModelEncryption.encryptSessionCache(sessionCache)
          Json.obj(
            "id"        -> encryptedValue._1,
            "gmpSession" -> encryptedValue._2
          )
        }
      }

    def formats(implicit encryption: Encryption): OFormat[GMPBulkSessionCache] = OFormat(reads(), writes)
  }
}
