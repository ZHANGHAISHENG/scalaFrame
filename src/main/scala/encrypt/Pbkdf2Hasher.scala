package encrypt

import java.util.Base64
import javax.crypto.spec.PBEKeySpec
import javax.crypto.SecretKeyFactory
import scala.util._


object Pbkdf2Hasher extends App {
  private[this] val random = new scala.util.Random(System.currentTimeMillis)
  private[this] val b64Encoder = Base64.getEncoder
  private[this] val b64Decoder = Base64.getDecoder
  private[this] val secretKeyFactory: SecretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512")
  private[this] val defaultIters = 10000
  private[this] val defaultKeyLength = 512
  private[this] val randomUtil = RandomUtil()
  private[this] def genSalt(n: Int) = randomUtil.rString(n)

  def generatePasswordHash(password: String, saltSize: Int = 16, iters: Int = defaultIters, keyLength: Int = defaultKeyLength): String = {
    val salt = genSalt(saltSize)
    val chars = password.toCharArray()
    val spec = new PBEKeySpec(chars, salt.getBytes, iters, keyLength)
    val hash: Array[Byte] = secretKeyFactory.generateSecret(spec).getEncoded()
    val r = "pbkdf2_sha512_base64" + "$" + iters + "$" + salt + "$" + b64Encoder.encodeToString(hash)
    r
  }

  def validate(password: String, hash: String): Boolean = {
    hash.split("""\$""").toList match {
      case info :: _iters :: salt :: _hash :: Nil => {
        Try {
          val iters     = _iters.toInt
          val keyLength = b64Decoder.decode(_hash).length * 8
          val spec = new PBEKeySpec(password.toCharArray(), salt.getBytes, iters, keyLength)
          val bs: Array[Byte] = secretKeyFactory.generateSecret(spec).getEncoded()
          val hash0: String = b64Encoder.encodeToString(bs)
          hash0 == _hash
        }.toOption getOrElse false
      }
      case _                                      => false
    }
  }


  val s = generatePasswordHash("123456")
  println(s)
  println(validate("123456", s))

}
