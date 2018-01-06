package circe_json.encode_decode

import io.circe.Json
import io.circe.parser.decode
import io.circe.syntax._

/**
  * bean 与 json 转换
  */
object EncodeAndDecode {

  def main(args: Array[String]): Unit = {
    //常见类型编码解码
    val intsJson: Json = List(1, 2, 3).asJson
    println(intsJson)

    println(intsJson.as[List[Int]])
    val obj = decode[List[Int]]("[1, 2, 3]")
    println(obj)

   /* @JsonCodec case class Bar(i: Int, s: String)
    val b1 = Bar(13, "Qux").asJson
    println(b1)*/

  }

}
