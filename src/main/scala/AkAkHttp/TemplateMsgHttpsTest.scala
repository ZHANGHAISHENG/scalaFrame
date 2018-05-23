package AkAkHttp

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.util.ByteString
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}

object TemplateMsgHttpsTest {
  val appid = "wxa392f9af221825c3"
  val secret = "411a8fc21cae8b8834254adc19be896a"

  implicit val system = ActorSystem()
  implicit val mat = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  final case class Token(access_token: String, expires_in: Long)
  final case class Pair(value: String, color: String = "#173177")
  final case class MsgTemplate(touser: String, template_id: String, page: String, form_id: String, data: Map[String, Pair])


  def main(args: Array[String]): Unit = {
    val msgtmp = MsgTemplate(
      touser = "o9JK35Yrx1kN-dWZHoMNeX73Dmro"
      , template_id = "EBmgTu0hLAOjHGxBEqiioabAUSLV3ztYEqEkHAHf9lY"
      , page = "pages/index"
      , form_id = "c53de3255f52d4ab9396a6ceabae6150" // 支付可以使用3次，普通form只能用1次，有效期7天
      , data = Map("keyword1" -> Pair("赛季1"), "keyword2" -> Pair("2018-9-9"), "keyword3" -> Pair("深圳"))
    )
    val f1 = getAccessToken()
    val f2 = f1.flatMap {
      case Some(token) =>
        val str = msgtmp.asJson.toString()
        println(str)
        val result = send(token.access_token, str)
        result
      case _ =>
        Future.successful(Some("no token"))
    }
    f2.onComplete {
      case Success(r) =>
        println(r)
      case Failure(e) =>
        println(e)
    }
    Await.result(f2, Duration.Inf)
  }

  private[this] def send(tokenStr: String, templateStr: String) = {
    val url = s"https://api.weixin.qq.com/cgi-bin/message/wxopen/template/send?access_token=$tokenStr"
    val entity = HttpEntity(string = templateStr, contentType = ContentTypes.`application/json`)
    val req = HttpRequest(uri = url, method = HttpMethods.POST, entity = entity)
    val responseFuture: Future[HttpResponse] = Http().singleRequest(req)
    responseFuture.flatMap {
      case res: HttpResponse if res.status == StatusCodes.OK =>
        val r: Future[Option[String]] = res.entity.dataBytes.runFold(ByteString(""))(_ ++ _).map(_.toArray).map { bytes =>
          Some(new String(bytes))
        }
        r
      case _ => Future.successful(None)
    }
  }

  private[this] def getAccessToken() = {
    val url = s"https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=$appid&secret=$secret"
    val req = HttpRequest(uri = url, method = HttpMethods.GET)
    val responseFuture: Future[HttpResponse] = Http().singleRequest(req)
    responseFuture.flatMap {
      case res: HttpResponse if res.status == StatusCodes.OK =>
        val r: Future[Option[Token]] = res.entity.dataBytes.runFold(ByteString(""))(_ ++ _).map(_.toArray).map { bytes =>
          val a = new String(bytes)
          val b = parse(a).flatMap(_.as[Token])
          b match {
            case Right(x) =>
              Some(x)
            case Left(_) =>
              None
          }
        }
        r
      case _ => Future.successful(None)
    }
  }

}
