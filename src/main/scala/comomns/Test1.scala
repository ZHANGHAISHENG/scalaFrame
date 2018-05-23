package comomns

import java.text.SimpleDateFormat
import java.util.Base64

object Test1 {

  sealed trait ReqResult

  final case class PubTokenResult(cacheItem: String) extends ReqResult

  final case class LoginResult(user: String, isFirstLogin: Boolean) extends ReqResult

  sealed trait ReqOk extends ReqResult

  sealed trait ReqError extends ReqResult

  case object NoData extends ReqError // 没有获取到数据

  case object TokenExpireError extends ReqError // token過期

  case object CheatError extends ReqError // 作弊

  def main(args: Array[String]): Unit = {
   
    val r: Option[ReqResult] = None

    r foreach  {
      case a: LoginResult => println(a.user)
      case x => println(x)
    }



    val current = System.currentTimeMillis
    val tokenExpireTime = current + (7200 - 3) * 1000
    val refreshTokenExpireTime = current + ((28L * 24 * 60 * 60) - 3) * 1000

    val sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")


    println(((25L * 24 * 60 * 60) - 3) * 1000)
    println(tokenExpireTime + "--" + sf.format(1524913148181L))
    println(refreshTokenExpireTime + "--" + sf.format(1527325148181L))

    //val s1 = "http%3A%2F%2Ftest.kxdati.com%2F%3FfromType%3D5%26friendsToken%3DeyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjE1MjU2MTY1NTcsImlhdCI6MTUyNTUzMDE1NywiaWQiOiIxYWQyNTIyMi1hZTRjLTRlZWEtODM4OC01ODljMjBmZjkwNGEiLCJ1aWQiOjExMzd9.NgN9rs8t3nBUrTd2F3H-b_CGjMMdtu1xsZSov08hzSA%26from%3Dsinglemessage%3FfriendsToken%3DeyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjE1MjU2MTY1NTcsImlhdCI6MTUyNTUzMDE1NywiaWQiOiIxYWQyNTIyMi1hZTRjLTRlZWEtODM4OC01ODljMjBmZjkwNGEiLCJ1aWQiOjExMzd9.NgN9rs8t3nBUrTd2F3H-b_CGjMMdtu1xsZSov08hzSA%26fromType%3D5,"
    val s1 = "http%3A%2F%2Ftest.kxdati.com%2F%3FfriendsToken%3Dundefined%26fromType%3Dundefined"
    val s2 =  java.net.URLDecoder.decode(s1, "utf-8")

    print(s2)

   /* val a: Array[Byte] = Base64.getDecoder.decode("aHR0cDovL3Rlc3Qua3hkYXRpLmNvbS9hcGkvdjAvcmVkaXJlY3Q/ZnJpZW5kVG9rZW49c2RkZGRkZGRkZGRkZGRkZGRkZGRkZGQmZnJpZW5kVHlwZT0x")
    println(new String(a))*/
  }

}
