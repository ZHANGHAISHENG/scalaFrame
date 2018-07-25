package AkAkHttp

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.util.ByteString

import scala.concurrent.Future
import scala.util.{Failure, Success}
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._

object LoginHttpsTest {
  implicit val system = ActorSystem()
  implicit val mat = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  final case class LoginRequset(code: String, userEncryptedData: String, userIv: String, fromType: Int, scene: String, friendUuid: String)
  val obj = LoginRequset(
    code = "001F5zUJ08RIQ62nPOSJ0tQdUJ0F5zU2"
    , userEncryptedData = "Vu1o9El+Z9M5S3q1yetM99JjxkXLejor7DqZsXea8qk8Bqn/W3bgw1Pahgaj2LtpphIwUwCZV3aBvX30cD+dsDGeqpPBcJElq9OgtIX3jJSfZNwwYH6ab8I/IAzUmorRqX0JYvFyktcR/xSlZkFcBQ2DcnOfQghkFDVZ2Xpuq0Wk4UqK60ykj/3BXyZphGbyTnv47SHFgwHFf7g2ChwMhCz5N/Px+EqPBAyNLslZj2f51old22GGUvydxGSLXun6M+WGk7msrp94l6kuhROls+HXcBZzhk0hq1ZTFVxmpJgfZQFHAx39iLwEa0UiT9MPCf7ul4DSrF/qnpeeQ8Xd1B0JjuSlror62kpgSeiqHDikaVRQvnwdh3UfGalBLkwFlsl56ERRTerZrVVoYgRv2Mm7JrfWF3CJFbzNmpdYfViMp13DS7HfyIJVrH6u3tkHcOFh+EEtq4KKdH+EGZRdZbOmanKcOKM3gBjGSv8HouQKCBH8BDtqhZbBwujZIddIwDbI9pnqKn/WXUi/f1G9gA=="
    , userIv = "Je+rulOsVigT9EaUNDe7Cw=="
    , fromType = 2
    , scene = ""
    , friendUuid = "8eeff975-10a5-4dbd-9b7c-0c7f4eb78a54"
  )
  def main(args: Array[String]): Unit = {
    (1 to 100).foreach(i => login(obj.copy(scene = "0_" + i)))
  }

  private[this] def login(obj: LoginRequset) = {
    val url = "http://127.0.0.1:3300/api/v1/login"
    val entity = HttpEntity(string = obj.asJson.toString(), contentType = ContentTypes.`application/json`)
    val req = HttpRequest(uri = url, method = HttpMethods.POST, entity = entity)
    val responseFuture: Future[HttpResponse] = Http().singleRequest(req)
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
