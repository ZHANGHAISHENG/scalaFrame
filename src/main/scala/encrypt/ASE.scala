package encrypt

import java.util.Base64

import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

object ASE extends App {
  // 加密
  @throws[Exception]
  def encrypt(sSrc: String, sKey: String): String = {
    if (sKey == null) {
      System.out.print("Key为空null")
      return null
    }
    // 判断Key是否为16位
    if (sKey.length != 16) {
      System.out.print("Key长度不是16位")
      return null
    }
    val raw = sKey.getBytes("utf-8")
    val skeySpec = new SecretKeySpec(raw, "AES")
    val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding") //"算法/模式/补码方式"
    cipher.init(Cipher.ENCRYPT_MODE, skeySpec)
    val encrypted = cipher.doFinal(sSrc.getBytes("utf-8"))
    Base64.getEncoder.encodeToString(encrypted) //此处使用BASE64做转码功能，同时能起到2次加密的作用。

  }

  // 解密
  @throws[Exception]
  def decrypt(sSrc: String, sKey: String): String = try { // 判断Key是否正确
    if (sKey == null) {
      System.out.print("Key为空null")
      return null
    }
    if (sKey.length != 16) {
      System.out.print("Key长度不是16位")
      return null
    }
    val raw = sKey.getBytes("utf-8")
    val skeySpec = new SecretKeySpec(raw, "AES")
    val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
    cipher.init(Cipher.DECRYPT_MODE, skeySpec)
    val encrypted1 = Base64.getDecoder.decode(sSrc) //先用base64解密
    try {
      val original = cipher.doFinal(encrypted1)
      val originalString = new String(original, "utf-8")
      originalString
    } catch {
      case e: Exception =>
        System.out.println(e.toString)
        null
    }
  } catch {
    case ex: Exception =>
      System.out.println(ex.toString)
      null
  }

  val cKey = "yichi01234567890"
  // 需要加密的字串
  val cSrc = "adid=a10001&adtype=banner&width=100&height=300&os=0&osv=5.1.1&pkgname=co" + "m.yichi.mobads.test&appver=7.0&&devicetype=1&vendor=xiaomi&model=mix2&imei=8" + "67068020992938&mac=9C:99:A0:FF:E9:15&anid=eedbdb39f66910a4&sw=1440&sh=2" + "560&ip=192.168.1.200&ot=1&ct=1"
  System.out.println(cSrc)
  val enString = encrypt(cSrc, cKey)
  System.out.println("加密后的字串是：" + enString)
  val DeString = decrypt(enString, cKey)
  System.out.println(DeString)
}
