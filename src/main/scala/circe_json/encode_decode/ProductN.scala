package circe_json.encode_decode

import io.circe.{Decoder, Encoder}
import io.circe.syntax._

object ProductN {

  def main(args: Array[String]): Unit = {
    //使用productN 方法创建implict
    case class User(id: Long, firstName: String, lastName: String)
    object UserCodec {
      implicit val decodeUser: Decoder[User] = Decoder.forProduct3("id", "first_name", "last_name")(User.apply)
      implicit val encodeUser: Encoder[User] = Encoder.forProduct3("id", "first_name", "last_name")(u =>
        (u.id, u.firstName, u.lastName)
      )
    }
    import UserCodec._
    val  u1 = User(100,"zhang","hs").asJson
    println(u1)
  }

}
