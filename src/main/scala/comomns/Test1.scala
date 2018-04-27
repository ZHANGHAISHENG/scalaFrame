package comomns

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

  }

}
