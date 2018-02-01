package akka.actor

import akka.stream._
import akka.stream.scaladsl.{ Sink, Source}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal
import scala.concurrent.duration._

object StreamTest2 {
  implicit def actorSystem: ActorSystem = ActorSystem("adx-edge-" + java.util.UUID.randomUUID().toString.toLowerCase)
  implicit def mt: Materializer = ActorMaterializer()
  implicit def ec: ExecutionContext = actorSystem.dispatcher

  protected val decider: Supervision.Decider = {
    case NonFatal(e) =>
      e.printStackTrace()
      Supervision.Resume
  }

  /**
    * 定时执行任务
    * @param args
    */
  def main(args: Array[String]): Unit = {
    val tickInterval: FiniteDuration = 1000.milliseconds
    val source: Cancellable =  Source.tick(0.seconds, tickInterval, ())
      .mapAsync(1) { _ => Future.successful(User(1,"zhs")) }
      .withAttributes(ActorAttributes.supervisionStrategy(decider))
      .mapAsync(1){x => println(x.name+"!"); Future.successful(x.copy(name = x.name+"!"))}
      .to(Sink.ignore)
      .run()
  }

}
