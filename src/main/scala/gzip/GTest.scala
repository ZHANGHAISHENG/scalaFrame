package gzip

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import java.util.zip.{GZIPInputStream, GZIPOutputStream}
import scala.util.{Try}

object GTest {

  def compress(input: Array[Byte]): Array[Byte] = {
    val bos = new ByteArrayOutputStream(input.length)
    val gzip = new GZIPOutputStream(bos)
    gzip.write(input)
    gzip.close()
    val compressed = bos.toByteArray
    bos.close()
    compressed
  }

  def decompress(compressed: Array[Byte]): Option[String] = Try {
    val inputStream = new GZIPInputStream(new ByteArrayInputStream(compressed))
    scala.io.Source.fromInputStream(inputStream).mkString
  }.toOption

  def main(args: Array[String]): Unit = {
    //原始字符串
    val str = """
    {"apps":"android,cn.coupon.kfc,cn.coupon.mac,cn.wps.moffice_eng,com.
      MobileTicket,com.UCMobile,com.alipay.security.mobile.authenticator,co
      m.android.BBKClock,com.android.BBKCrontab,com.android.BBKPhoneIn
      structions,com.android.BBKTools,com.android.VideoPlayer,com.android.
      attachcamera,com.android.backupconfirm,com.android.bbk.lockscreen3
      ","idfa":"AEBE52E7-03EE-455A-B3C4-E57283966239","imei":"355065053
      311001","latitude":"104.07642","longitude":"38.6518","model":"MIMAX",
      "nt":"wifi","os":"Android","os_version":"7","vendor":"Xiaomi"}
     """
    //压缩
    val c: Array[Byte] = compress(str.getBytes)
    println("压缩前后大小："+str.getBytes.length+"--"+c.length) //591--357
    //解压
    val d = decompress(c)
    val r = d.getOrElse(None)
    println(r)

  }

}
