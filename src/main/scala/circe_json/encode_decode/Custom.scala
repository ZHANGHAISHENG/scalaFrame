package circe_json.encode_decode

import io.circe.{Json, _}
import io.circe.syntax._ // asJson

object Custom {

  def main(args: Array[String]): Unit = {
    //常用方式
    class Thing(val foo: String, val bar: Int)
    implicit val encodeFoo: Encoder[Thing] = new Encoder[Thing] {
      final def apply(a: Thing): Json = Json.obj(
        ("foo", Json.fromString(a.foo)),
        ("bar", Json.fromInt(a.bar))
      )
    }
    implicit val decodeFoo: Decoder[Thing] = new Decoder[Thing] {
      final def apply(c: HCursor): Decoder.Result[Thing] =
        for {
          foo <- c.downField("foo").as[String]
          bar <- c.downField("bar").as[Int]
        } yield {
          new Thing(foo, bar)
        }
    }
    println(new Thing("good thing",100).asJson)
  }
}
