package comomns

object Test2 extends App {
  final case class LoginResult(user: String, isFirstLogin: Boolean)

  final case class RespWrapper[T](data:T, status: Int, msg: String = "")



  def getLoginResponse(): RespWrapper[Some[LoginResult]] = {
    val a = LoginResult("zhs", true)
    RespWrapper(data = Some(a), 1, "hello")
  }

  println(getLoginResponse())

}
