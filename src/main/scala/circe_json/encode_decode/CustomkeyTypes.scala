package circe_json.encode_decode

import io.circe.syntax._
import io.circe.{KeyDecoder, KeyEncoder}

/**
  * map key是对象类型的encode,decode
  */
object CustomkeyTypes {

  def main(args: Array[String]): Unit = {
    //key encoder decoder
    case class Foo(value: String)

    val map = Map[Foo, Int](
      Foo("hello") -> 123,
      Foo("world") -> 456
    )
    println(map)

    //编码
    implicit val fooKeyEncoder = new KeyEncoder[Foo] {
      override def apply(foo: Foo): String = foo.value
    }
    val json = map.asJson
    println(json)

    //解码
    implicit val fooKeyDecoder = new KeyDecoder[Foo] {
      override def apply(key: String): Option[Foo] = Some(Foo(key))
    }
    println(json.as[Map[Foo, Int]])
  }

}
