package akka.stream

import akka.NotUsed
import akka.stream.scaladsl._
import akka.testkit.AkkaSpec
import docs.docs.CompileOnlySpec
import scala.concurrent.duration._
import scala.concurrent._

class RestartDocSpec extends AkkaSpec with CompileOnlySpec {
  implicit val mat = ActorMaterializer()
  import system.dispatcher
  object Http {
    def apply() = this
    def singleRequest(req: HttpRequest) = Future.successful(())
  }
  case class HttpRequest(uri: String)
  case class Unmarshal(b: Any) {
    def to[T]: Future[T] = Promise[T]().future
  }
  case class ServerSentEvent()
  def doSomethingElse(): Unit = ()

  /**
    * 请求将以3,6,12,24和最后30秒的间隔增加（此时由于该参数它将保持上限）
    */
  "Restart stages" should {
    "demonstrate a restart with backoff source" in  {
      val restartSource = RestartSource.withBackoff(
        minBackoff = 3.seconds,
        maxBackoff = 30.seconds,
        randomFactor = 0.2
      ) { () ⇒
        // Create a source from a future of a source
        Source.fromFutureSource {
          // Make a single request with akka-http
          Http().singleRequest(HttpRequest(
            uri = "http://example.com/eventstream"
          ))
            // Unmarshall it as a source of server sent events
            .flatMap(Unmarshal(_).to[Source[ServerSentEvent, NotUsed]])
        }
      }
      val killSwitch = restartSource
        .viaMat(KillSwitches.single)(Keep.right)
        .toMat(Sink.foreach(event ⇒ println(s"Got event: $event")))(Keep.left)
        .run()
      doSomethingElse()
      killSwitch.shutdown()
    }
  }

  /**
    * restart任何累积状态都被清除
    */
  "demonstrate restart section" in {
    val decider: Supervision.Decider = {
      case _: IllegalArgumentException ⇒ Supervision.Resume
      case _                           ⇒ Supervision.Stop
    }
    val flow = Flow[Int]
      .scan(0) { (acc, elem) ⇒
        if (elem < 0) throw new IllegalArgumentException("negative not allowed")
        else acc + elem
      }
      .withAttributes(ActorAttributes.supervisionStrategy(decider))
    val source = Source(List(1, 3, -1, 5, 7)).via(flow)
    val result = source.limit(1000).runWith(Sink.seq)
    val r = Await.result(result, 3.seconds)
    println(r)
    r should be(Vector(0, 1, 4, 0, 5, 12)) // 如果是 Resume 则是 Vector(0, 1, 4, 9, 16)
  }
}
