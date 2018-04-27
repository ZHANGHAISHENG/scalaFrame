package AkAkHttp


import java.io.{File, FileOutputStream}
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.RawHeader
import akka.stream.ActorMaterializer
import akka.util.ByteString
import scala.concurrent.Future
import io.circe.parser._
import io.circe.generic.auto._

object WxQrCodeHttpsTest {
  val appid = "wxa392f9af221825c3"
  val secret = "411a8fc21cae8b8834254adc19be896a"
  implicit val system = ActorSystem()
  implicit val mat = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  final case class Token(access_token: String, expires_in: Long)

  def main(args: Array[String]): Unit = {
    val f1 = getAccessToken()
    f1.map {
      case Some(token) =>
        val f2 = getImgBytes(token.access_token)
        f2.map {
          case Some(x) =>
            writeToFile(x, path = "/home/hamlt/qrcode.png")
          case _ =>
        }
      case _ =>
    }
  }

  private[this] def writeToFile(bytes: Array[Byte], path: String) = {
    val file = new File(path)
    if(!file.exists()){
      file.createNewFile()
    }
    val a: FileOutputStream  = new FileOutputStream(file)
    a.write(bytes)
    a.flush()
    a.close()
    println("write to file success")
  }

  private[this] def getImgBytes(token: String): Future[Option[Array[Byte]]] = {
    val url = s"https://api.weixin.qq.com/cgi-bin/wxaapp/createwxaqrcode?access_token=$token"
    val entity = HttpEntity(string = "{\"path\": \"pages/index?query=1\", \"width\": 430}", contentType = ContentTypes.`application/json`)
    val req = HttpRequest(uri = url, method = HttpMethods.POST, entity = entity)
    val responseFuture: Future[HttpResponse] = Http().singleRequest(req)
    responseFuture.flatMap {
      case res: HttpResponse if res.status == StatusCodes.OK =>
        res.entity.dataBytes.runFold(ByteString(""))(_ ++ _).map(_.toArray).map { bytes =>
          Some(bytes)
        }
      case _  => Future.successful(None)
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

