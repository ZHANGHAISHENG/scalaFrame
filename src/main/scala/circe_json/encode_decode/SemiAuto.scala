package circe_json.encode_decode

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.syntax._


object SemiAuto {

  def main(args: Array[String]): Unit = {
    //样例类编码解码
    case class Foo(a: Int, b: String, c: Boolean)
    implicit val fooDecoder: Decoder[Foo] = deriveDecoder[Foo] //  或 deriveDecoder
    implicit val fooEncoder: Encoder[Foo] = deriveEncoder[Foo] //  或 deriveEncoder
    val j1 = Foo(100,"i m b",true).asJson
    println(j1)
   // println(j1.as[Foo])

  }

}
