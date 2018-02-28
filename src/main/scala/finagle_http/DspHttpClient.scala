package finagle_http


import scala.collection.mutable
import scala.concurrent._
import akka.actor._
import akka.http.scaladsl.model.Uri
import akka.pattern._
import akka.util.Timeout
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.service.{RetryBudget}
import com.twitter.finagle.{Http, Service}


object DspHttpClient {
  import com.twitter.conversions.time._

  final case class ClientKey(host: String, port: Int)

  def newClient(host: String, port: Int): Service[Request, Response] = {
    val c = Http.client
      .withRetryBudget(RetryBudget.Empty)
      .withSessionPool.maxSize(512)
      .withSessionPool.minSize(0)
      .withSessionPool.maxWaiters(0)
      .withTransport.connectTimeout(10.seconds)
      .withRequestTimeout(10.seconds) // TODO requestTimeout
      .newService(s"$host:$port", s"$host:$port")
      println("http client created. {"+ host +"}:{" +port+ "}")
    c
  }
  class ClientsActor extends Actor {
    private [this] val clients: mutable.Map[ClientKey, Service[Request, Response]] = mutable.Map.empty
    override def receive: Receive = {
      case x: ClientKey => sender() ! clients.getOrElseUpdate(x, newClient(x.host, x.port))
    }
  }

  def apply()(implicit system: ActorSystem): DspHttpClient = {
    val c = new DspHttpClient {
      val clientActor: ActorRef = system.actorOf(Props[ClientsActor])
    }
    c
  }
}

trait DspHttpClient {

  import scala.concurrent.duration._
  import DspHttpClient._

  private [this] implicit val timeout: Timeout = Timeout(20.seconds)

  protected def clientActor: ActorRef

  def clientOf(host: String, port: Int) (implicit ec: ExecutionContext): Future[Service[Request, Response]] = {
    (clientActor ? ClientKey(host, port)).mapTo[Service[Request, Response]]
  }

  def fromUrl(url: String)(implicit ec: ExecutionContext): Future[Service[Request, Response]] = {
    val uri = Uri(url)
    val host = uri.authority.host.address
    val port = uri.effectivePort
    clientOf(host, port)
  }

}


