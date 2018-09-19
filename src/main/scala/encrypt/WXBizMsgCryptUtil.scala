package encrypt

import java.nio.charset.Charset
import java.util
import java.util.Random

import javax.crypto.Cipher
import javax.crypto.spec.{IvParameterSpec, SecretKeySpec}
import org.apache.commons.codec.binary.Base64

object WXBizMsgCryptUtil extends App {
  private[this] val CHARSET = Charset.forName("utf-8")
  private[this] val base64 = new Base64
  private[this] var aesKey = Base64.decodeBase64("pYk1f82rgYyM0qVpYGMIhXyrOJSsDKiQSOfaB7NguD8=")
  private[this] var token = "aidati2018f14"
  private[this] var appId = "wx735c5d5f6dd497a9"


  // 生成4个字节的网络字节序
  private[this] def getNetworkBytesOrder(sourceNumber: Int) = {
    val orderBytes = new Array[Byte](4)
    orderBytes(3) = (sourceNumber & 0xFF).toByte
    orderBytes(2) = (sourceNumber >> 8 & 0xFF).toByte
    orderBytes(1) = (sourceNumber >> 16 & 0xFF).toByte
    orderBytes(0) = (sourceNumber >> 24 & 0xFF).toByte
    orderBytes
  }

  // 还原4个字节的网络字节序
  private[this] def recoverNetworkBytesOrder(orderBytes: Array[Byte]) = {
    var sourceNumber = 0
    var i = 0
    while (i < 4) {
      sourceNumber <<= 8
      sourceNumber |= orderBytes(i) & 0xff
      i += 1
    }
    sourceNumber
  }

  private[this] def getRandomStr = {
    val base = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
    val random = new Random
    val sb = new StringBuffer
    var i = 0
    while ( {
      i < 16
    }) {
      val number = random.nextInt(base.length)
      sb.append(base.charAt(number))

      {
        i += 1; i - 1
      }
    }
    sb.toString
  }

  /**
    * 对明文进行加密.
    *
    * @param text 需要加密的明文
    * @return 加密后base64编码的字符串
    */
  private[this] def encrypt(randomStr: String, text: String) = {
    val byteCollector = new ByteGroup
    val randomStrBytes = randomStr.getBytes(CHARSET)
    val textBytes = text.getBytes(CHARSET)
    val networkBytesOrder = getNetworkBytesOrder(textBytes.length)
    val appidBytes = appId.getBytes(CHARSET)
    // randomStr + networkBytesOrder + text + appid
    byteCollector.addBytes(randomStrBytes)
    byteCollector.addBytes(networkBytesOrder)
    byteCollector.addBytes(textBytes)
    byteCollector.addBytes(appidBytes)
    // ... + pad: 使用自定义的填充方式对明文进行补位填充
    val padBytes = PKCS7Encoder.encode(byteCollector.size)
    byteCollector.addBytes(padBytes)
    // 获得最终的字节流, 未加密
    val unencrypted = byteCollector.toBytes
    try { // 设置加密模式为AES的CBC模式
      val cipher = Cipher.getInstance("AES/CBC/NoPadding")
      val keySpec = new SecretKeySpec(aesKey, "AES")
      val iv = new IvParameterSpec(aesKey, 0, 16)
      cipher.init(Cipher.ENCRYPT_MODE, keySpec, iv)
      // 加密
      val encrypted = cipher.doFinal(unencrypted)
      // 使用BASE64对加密后的字符串进行编码
      val base64Encrypted = base64.encodeToString(encrypted)
      base64Encrypted
    } catch {
      case e: Exception =>
        e.printStackTrace()
        ""
    }
  }

  /**
    * 对密文进行解密.
    *
    * @param text 需要解密的密文
    * @return 解密得到的明文
    */
  private[this] def decrypt(text: String) = {
    var original: Array[Byte] = Array[Byte]()
    try { // 设置解密模式为AES的CBC模式
      val cipher = Cipher.getInstance("AES/CBC/NoPadding")
      val key_spec = new SecretKeySpec(aesKey, "AES")
      val iv = new IvParameterSpec(util.Arrays.copyOfRange(aesKey, 0, 16))
      cipher.init(Cipher.DECRYPT_MODE, key_spec, iv)
      // 使用BASE64对密文进行解码
      val encrypted = Base64.decodeBase64(text)
      // 解密
      original = cipher.doFinal(encrypted)
    } catch {
      case e: Exception =>
        e.printStackTrace()
    }
    var xmlContent = ""
    var from_appid = ""
    try { // 去除补位字符
      val bytes: Array[Byte] = PKCS7Encoder.decode(original)
      // 分离16位随机字符串,网络字节序和AppId
      val networkOrder = util.Arrays.copyOfRange(bytes, 16, 20)
      val xmlLength = recoverNetworkBytesOrder(networkOrder)
      xmlContent = new String(util.Arrays.copyOfRange(bytes, 20, 20 + xmlLength), CHARSET)
      from_appid = new String(util.Arrays.copyOfRange(bytes, 20 + xmlLength, bytes.length), CHARSET)
    } catch {
      case e: Exception =>
        e.printStackTrace()
    }
    // appid不相同的情况
    if (!(from_appid == appId)) "" // @TODO
    xmlContent
  }

  val str = encrypt(getRandomStr, "<xml><ToUserName><![CDATA[gh_87232ecc5278]]></ToUserName><xml>")
  println(decrypt(str))
}

class ByteGroup {
  private[this] val byteContainer = new util.ArrayList[Byte]
  def toBytes: Array[Byte] = {
    val bytes = new Array[Byte](byteContainer.size)
    var i = 0
    while ( {
      i < byteContainer.size
    }) {
      bytes(i) = byteContainer.get(i)

      {
        i += 1; i - 1
      }
    }
    bytes
  }
  def addBytes(bytes: Array[Byte]): ByteGroup = {
    for (b <- bytes) {
      byteContainer.add(b)
    }
    this
  }
  def size: Int = byteContainer.size
}

/**
  * 提供基于PKCS7算法的加解密接口.
  */
object PKCS7Encoder {
  private[this] val CHARSET = Charset.forName("utf-8")
  private[this] val BLOCK_SIZE = 32
  /**
    * 获得对明文进行补位填充的字节.
    *
    * @param count 需要进行填充补位操作的明文字节个数
    * @return 补齐用的字节数组
    */
  def encode(count: Int) = { // 计算需要填充的位数
    var amountToPad = BLOCK_SIZE - (count % BLOCK_SIZE)
    if (amountToPad == 0) amountToPad = BLOCK_SIZE
    // 获得补位所用的字符
    val padChr = chr(amountToPad)
    var tmp = new String
    var index = 0
    while ( {
      index < amountToPad
    }) {
      tmp += padChr

      {
        index += 1; index - 1
      }
    }
    tmp.getBytes(CHARSET)
  }

  /**
    * 删除解密后明文的补位字符
    *
    * @param decrypted 解密后的明文
    * @return 删除补位字符后的明文
    */
  def decode(decrypted: Array[Byte]) = {
    var pad = decrypted(decrypted.length - 1).toInt
    if (pad < 1 || pad > 32) pad = 0
    util.Arrays.copyOfRange(decrypted, 0, decrypted.length - pad)
  }

  /**
    * 将数字转化成ASCII码对应的字符，用于对明文进行补码
    * @param a 需要转化的数字
    * @return 转化得到的字符
    */
  private[this] def chr(a: Int) = {
    val target = (a & 0xFF).toByte
    target.toChar
  }


}
