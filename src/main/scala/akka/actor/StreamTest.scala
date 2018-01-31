package akka.actor

import akka.stream._
import akka.stream.scaladsl.{Flow, Sink, Source, SourceQueueWithComplete}
import scala.util.Success
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal
import scala.concurrent.duration._


object StreamTest {
  implicit def actorSystem: ActorSystem = ActorSystem("adx-edge-" + java.util.UUID.randomUUID().toString.toLowerCase)
  implicit def mt: Materializer = ActorMaterializer()
  implicit def ec: ExecutionContext = actorSystem.dispatcher

  val bufferSize: Int = 1000
  val overflowStrategy: OverflowStrategy = akka.stream.OverflowStrategy.backpressure
  val parallel: Int = 20

  lazy val queue: SourceQueueWithComplete[User] = {
    val flow = Flow[User].mapAsync(parallel) {
      case t:User =>println("success:" + t.name); Future.successful(t.copy(name = "ls"))
      case _ =>println("fail"); Future.successful(User(0,"ww"))
    }

    val tickInterval: FiniteDuration = 1000.milliseconds
    Source.tick(0.seconds, tickInterval, ())
      .mapAsync(1) { _ => Future.successful(User(1,"z")) }
      .withAttributes(ActorAttributes.supervisionStrategy(decider))
      .mapAsync(1){x => println(x.name); Future.successful(x.copy(name = x.name+"!"))}
      .to(Sink.ignore)
      .run()

    val source = Source
      .queue[User](bufferSize, overflowStrategy)
      .map(x => x.copy(name = x.name+"!"))
      .via(flow)
      .map{x => println("map:" + x.name) ; x}
      .withAttributes(ActorAttributes.supervisionStrategy(decider))
      .to(Sink.ignore)
      .run()
    source
  }
  protected val decider: Supervision.Decider = {
    case NonFatal(e) =>
      e.printStackTrace()
      Supervision.Resume
  }
  def pushToQueue(t: User): Future[QueueOfferResult] = queue offer t

  /**
    * 最后输出：
    * success
    * Enqueued
    * map:ls
    */
  def main(args: Array[String]): Unit = {
    pushToQueue(User(1,"zhs")).onComplete{
      case Success(q: QueueOfferResult) =>  println(q) // Enqueued
      case _ => println("know")
    }
  }

}
