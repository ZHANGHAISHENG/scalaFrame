package com.json

import io.circe._, io.circe.parser._

/**
  * 遍历，修改
  */
object TraverAndModifyJson {

  def main(args: Array[String]): Unit = {
    val json: String = """
        {
          "id": "c730433b-082c-4984-9d66-855c243266f0",
          "name": "Foo",
          "counts": [1, 2, 3],
          "values": {
            "bar": true,
            "baz": 100.001,
            "qux": ["a", "b"]
          }
        }
      """
    val doc: Json = parse(json).getOrElse(Json.Null)

    println("***********遍历*************")
    val cursor: HCursor = doc.hcursor
    val baz: Decoder.Result[Double] = cursor.downField("values").downField("baz").as[Double]
    val baz2: Decoder.Result[Double] = cursor.downField("values").get[Double]("baz")
    val secondQux: Decoder.Result[String] = cursor.downField("values").downField("qux").downArray.right.as[String]//downArray.right指针右移
    println(baz)
    println(baz2)
    println(secondQux)

    println("***********修改*************")
    val reversedNameCursor: ACursor = cursor.downField("name").withFocus(_.mapString(_.reverse))
    val reversedName: Option[Json] = reversedNameCursor.top
    println(reversedName)

  }

}
