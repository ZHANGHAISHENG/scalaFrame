package AkAkHttp

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.util.ByteString

import scala.concurrent.Future
import scala.util.{Failure, Success}

object HttpsTest {

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem()
    implicit val mat = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.dispatcher
    val responseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = "https://api.weixin.qq.com/sns/jscode2session"))
    responseFuture
      .onComplete {
        case Success(res) =>
          res.entity.dataBytes.runFold(ByteString(""))(_ ++ _).map(_.toArray).map { bytes =>
            println(new String(bytes))
          }
        case Failure(_)   => sys.error("something wrong")
      }
  }

}
