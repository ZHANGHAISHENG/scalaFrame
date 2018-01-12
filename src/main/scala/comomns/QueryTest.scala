package comomns

import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.Uri.Query

import scala.util.{Failure, Success, Try}

object QueryTest {

  def formatMacro(url: String) = url.replaceAll("\\$\\{DOWN_X}", "\\{{AG_DX_A}}")
    .replaceAll("\\$\\{DOWN_Y}", "\\{{AG_DY_A}}")
    .replaceAll("\\$\\{UP_X}","\\{{AG_UX_A}}")
    .replaceAll("\\$\\{UP_Y}", "\\{{AG_UY_A}}")

  def main(args: Array[String]): Unit = {
    val ext: List[(String, String)] = List(
      "adslotId" -> "111111"
      , "md" -> "2222222"
      , "timestamp" -> "333333"
      , "nonce" -> """4444444"""
      , "signature" -> "中文"
    )

    val baseUrl = "https://engine.lvehaisen.com/index/activity?appKey=hZmYczUX7EBYtjjhc4723G9nrWB"

    val query = Query(ext: _*)

    val q: List[(String, String)] = Query(Uri(baseUrl).rawQueryString).toList ++ query

    val url = Uri(baseUrl).withQuery(Query(q: _*)).toString()

    println("-------------------------------------------------------------------------------------------")
    println(Uri(baseUrl).authority)
    println(Uri(baseUrl).path)
    println(q)
    println(query)

    println(java.net.URLEncoder.encode("中", "utf-8"))

    println(System.currentTimeMillis())

   val url2 = "http://www.baidu.com?a=${DOWN_X}".replaceAll("\\$\\{DOWN_X}", "\\{{AG_DX_A}}")
   println(url2)

  }

  private[this] def vStrToInt(v: String):Int = {
    Try(v.split("\\.")(0).toInt) match {
      case Success(i) => i
      case Failure(e) => 0
    }
  }

}
