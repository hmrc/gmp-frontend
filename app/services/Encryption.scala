package services

import play.api.Configuration
import uk.gov.hmrc.crypto.{AdDecrypter, AdEncrypter, SymmetricCryptoFactory}

import javax.inject.Inject

class Encryption @Inject() (configuration: Configuration){

  val crypto: AdEncrypter with AdDecrypter = SymmetricCryptoFactory.aesGcmAdCryptoFromConfig("mongodb.encryption", configuration.underlying)

}