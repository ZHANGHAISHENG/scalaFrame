package comomns

import java.security._
import javax.crypto.Cipher
import javax.crypto.spec.{IvParameterSpec, SecretKeySpec}
import org.bouncycastle.jce.provider.BouncyCastleProvider

object WxDecoder {
  var initialized = false


  def initialize(): Unit = {
    if (initialized) return
    Security.addProvider(new BouncyCastleProvider())
    initialized = true
  }

  //生成iv
  def generateIV(iv: Array[Byte]): AlgorithmParameters = {
    val params = AlgorithmParameters.getInstance("AES")
    params.init(new IvParameterSpec(iv))
    params
  }


  /**
    * AES解密
    * @param content
    * @param keyByte
    * @param ivByte
    * @return
    */
  def decrypt(content: Array[Byte], keyByte: Array[Byte], ivByte: Array[Byte]): Option[String] = {
    initialize()
    try {
      val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
      val sKeySpec = new SecretKeySpec(keyByte, "AES")
      cipher.init(Cipher.DECRYPT_MODE, sKeySpec, generateIV(ivByte)) // 初始化
      val resultByte = cipher.doFinal(content)
      Some(new String(resultByte, "UTF-8"))
    } catch {
      case e: Exception => e.printStackTrace(); None
    }
  }

  def main(args: Array[String]): Unit = {

  }

}
