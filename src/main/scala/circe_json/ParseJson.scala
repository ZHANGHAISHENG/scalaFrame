package com.json

import io.circe.Json
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._

/**
  * 解析json
  */
object ParseJson {
  def main(args: Array[String]): Unit = {
    val rawJson: String = """
      {
        "foo": "bar",
        "baz": 123,
        "list of stuff": [ 4, 5, 6 ]
      }
      """
    //解析json，并取其中一个元素
    val parseResult: Json = parse(rawJson).getOrElse(Json.Null)
    println(parseResult)
    println(parseResult.\\("foo").headOption)

    //使用模式匹配
    parse(rawJson) match {
      case Left(failure) => println("failure)"+failure)
      case Right(json) => println("Yay, got some JSON!")
    }

    //parse(rawJson).flatMap(_.camelizeKeys.as[Map]])

    //parse("").flatMap(x => Either.)


  }
}
