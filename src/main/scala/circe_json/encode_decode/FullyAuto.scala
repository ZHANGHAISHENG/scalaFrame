package circe_json.encode_decode

import io.circe.Decoder.Result
import io.circe.syntax._

object FullyAuto {

  def main(args: Array[String]): Unit = {
    //默认方式，无需自己写implicit
    import io.circe.generic.auto._
    case class Person(name: String)
    case class Greeting(salutation: String, person: Person, exclamationMarks: Int)
    val g1 = Greeting("Hey", Person("Chris"), 3).asJson
    println(g1)
    val g2: Result[Greeting] = g1.as[Greeting]
    println(g2)

  }

}
