package comomns

import java.nio.charset.Charset

import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.Uri.{ParsingMode, Query}

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

    val baseUrl = "https://engine.lvehaisen.com/index/activity?a=a&b=aa=="

    val query = Query(ext: _*)

    val q: List[(String, String)] = Query(Uri(baseUrl).rawQueryString).toList ++ query

    val url = Uri(baseUrl).withQuery(Query(q: _*)).toString()



    println(Uri(baseUrl).rawQueryString)
    println("-------------------------------------------------------------------------------------------")
    println(Uri(baseUrl).authority)
    println(Uri(baseUrl).path)
    println("q:"+q)
    println("query:" + query)
    println(Query(q: _*))

  }


}
