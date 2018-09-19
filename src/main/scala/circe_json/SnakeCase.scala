package circe_json

import io.circe.generic.extras.Configuration
import io.circe._
import io.circe.generic.extras.semiauto._
import io.circe.parser._
import scala.reflect.runtime.universe._



object SnakeCase extends App {
  case class User(firstName: String = "zhs", lasterName: String = "ls", age: Int = 0)

  //测试circe 下划线编译
  implicit val customConfig: Configuration = Configuration.default.withSnakeCaseConstructorNames.withSnakeCaseMemberNames.withDefaults

  implicit val encoderB = deriveEncoder[User]
  implicit val decoderB = deriveDecoder[User]

  val jsonStr = """{
                      "first_name":"zhang",
                      "lasterName":"hs",
                      "age":18
                  }"""

  val r: Either[Error, User] = decode[User](jsonStr)
  r match {
    case Right(x) => println(x)  // User(zhang,ls,18)
    case Left(e) => e.printStackTrace()
  }

  //测试反射获取参数名称
  def classAccessors[T: TypeTag]: List[MethodSymbol] = typeOf[T].members.collect {
    case m: MethodSymbol if m.isCaseAccessor => m
  }.toList
  classAccessors[User].filter(_.isCaseAccessor).map(x => x.name).foreach(println)

  //测试反射获取参数类型
  typeOf[User].members.filter(!_.isMethod).map(_.typeSignature).foreach {
    case t if t == typeOf[Int] => println("int")
    case s if s == typeOf[String] => println("snt")
  }

}
