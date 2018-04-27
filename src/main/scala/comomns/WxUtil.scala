package comomns

import java.security.{AlgorithmParameters, Security}
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.{IvParameterSpec, SecretKeySpec}

import org.bouncycastle.jce.provider.BouncyCastleProvider

import scala.util.Try


object WxUtil {

  /**
    * 检验数据的真实性，并且获取解密后的明文.
    */
  def decryData(sessionKey: String, encryptedData: String, iv: String): Option[String] = {
    if (sessionKey.length == 24 && iv.length == 24) {
      //先进行base64　encode
      val aesKey = Try(Base64.getDecoder.decode(sessionKey)).getOrElse(Array.emptyByteArray)
      val aesIV = Try(Base64.getDecoder.decode(iv)).getOrElse(Array.emptyByteArray)
      val aesCipher = Try(Base64.getDecoder.decode(encryptedData)).getOrElse(Array.emptyByteArray)
      //校验base64　encode结果
      if (aesKey.isEmpty || aesIV.isEmpty || aesCipher.isEmpty) {
        println("base64 encode error sessionKey:{} encryptedData:{} iv:{} ", sessionKey, encryptedData, iv)
        None
        //解密
      } else {
        val r1 = decrypt(aesCipher, aesKey, aesIV)
        val r2 = r1 match {
          case Some(jsonStr) =>
            Some(jsonStr)
          case _ =>
            println("decrypt AES error sessionKey:{} encryptedData:{} iv:{} ", sessionKey, encryptedData, iv)
            None
        }
        r2
      }
    } else {
      println("decrypt verrify lenghts err ,encryptedData.length:{} iv.length{}, sessionKey:{} encryptedData:{} iv:{} ", sessionKey.length, iv.length, sessionKey, encryptedData, iv)
      None
    }
  }

  /**
    * AES解密
    */
  private[this] def decrypt(content: Array[Byte], keyByte: Array[Byte], ivByte: Array[Byte]): Option[String] = {
    try {
      Security.addProvider(new BouncyCastleProvider())
      val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
      val sKeySpec = new SecretKeySpec(keyByte, "AES")
      cipher.init(Cipher.DECRYPT_MODE, sKeySpec, generateIV(ivByte))
      val resultByte = cipher.doFinal(content)
      Some(new String(resultByte, "UTF-8"))
    } catch {
      case e: Exception =>
        println(e)
        None
    }
  }

  //生成iv
  private[this] def generateIV(iv: Array[Byte]): AlgorithmParameters = {
    val params = AlgorithmParameters.getInstance("AES")
    params.init(new IvParameterSpec(iv))
    params
  }

  
  def main(args: Array[String]): Unit = {

    val sessionKey = "/FQSUoVP4WUlT4tu+u+E6Q=="
    val encryptedData = "FFjT7UQz7XsSmSOgBoexP2PRWi4xkGZdJPogN+6givNmLETvmjhlah9cbt+gtaAPL56c+Q9n8ypen/5UgqQoRnJ7XChsTFfDMsBy0dqyPr2tHrN21GDZiJhhQQmdumAqXh7HHIp5kthD7SUqjK7eTA=="
    val iv = "61Fa9RJaJzZpZk77ELY9sA=="
    val r = decryData(sessionKey , encryptedData, iv)
    println(r)

  }

}
