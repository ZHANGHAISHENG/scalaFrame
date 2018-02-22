package akka.streamTest.pipelines

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}

/**
  * Akka Streams: pipelines
  * 参考：
  * http://www.moye.me/2016/08/28/akka_streams-linear_pipelines/
  */
object WireUp extends App {
  implicit val system = ActorSystem("actor-system")
  implicit val materializer = ActorMaterializer()

  Source(0 to 25)
    .via(Flow[Int].map(i => (i + 'A').asInstanceOf[Char]))
    .to(Sink.foreach(println(_))).run()

  system.terminate()
}
