package finagle_http

import java.nio.charset.StandardCharsets
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, RequestBuilder, Response}
import TwitterConverter._
import com.twitter.concurrent.AsyncStream
import com.twitter.io.Buf
import com.twitter.conversions.time._
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

object HttpTest {

  val APPID = "wxa392f9af221825c3"
  val SECRET = "e8b32c93789923b51df086701058fce8"
  //val url = s"https://api.weixin.qq.com/sns/jscode2session?appid=$APPID&secret=$SECRET&js_code=JSCODE&grant_type=authorization_code"

  val url = "http://blog.csdn.net/android_gogogo/article/details/77063843" //能访问
  //val url = "https://api.weixin.qq.com/sns/jscode2session" //无法访问https

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem("game-quiz" + java.util.UUID.randomUUID().toString.toLowerCase)
    implicit val mat: ActorMaterializer = ActorMaterializer()
    implicit val ec: ExecutionContextExecutor = system.dispatcher
    val ctx: GlobalContext = GlobalContext()
    implicit val defaultTimer = com.twitter.finagle.util.DefaultTimer  // within(10.second) 方法需要一个隐式转换

    val client: Future[Service[Request, Response]] = ctx.dspClient.fromUrl(url)
    val req = RequestBuilder().url(url).buildGet()

    val r1: Future[Response] = client.flatMap(x => x.apply(req).within(10.second).asScala)
    val r2: Future[String] = r1.flatMap {
      case response if response.statusCode == 200 =>
        val bytesF: Future[Array[Byte]] = AsyncStream.fromReader(response.reader).foldLeft(Buf.Empty)((x, y) => x concat y).map(Buf.ByteArray.Owned.extract).asScala
        bytesF.map(x => new String(x, StandardCharsets.UTF_8))
      case response => Future.successful("error :" + response.statusCode)
    }

    r2.onComplete{
      case Success(body)=> println(body)
      case Failure(ex)=> println(ex)

    }
    Await.result(r2,Duration.Inf)
  }

}
