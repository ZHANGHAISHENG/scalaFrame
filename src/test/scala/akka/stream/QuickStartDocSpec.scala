package akka.stream

//#stream-imports
import akka.stream._
import akka.stream.scaladsl._
//#stream-imports

//#other-imports
import akka.{ NotUsed, Done }
import akka.actor.ActorSystem
import akka.util.ByteString
import scala.concurrent._
import scala.concurrent.duration._
import java.nio.file.Paths
//#other-imports

import org.scalatest._
import org.scalatest.concurrent._

//#main-app
object Main extends App {
  // Code here
}
//#main-app

class QuickStartDocSpec extends WordSpec with BeforeAndAfterAll with ScalaFutures {
  implicit val patience = PatienceConfig(5.seconds)

 // def println(any: Any) = () // silence printing stuff

  "a" in {
    implicit val system = ActorSystem("QuickStart")
    implicit val materializer = ActorMaterializer()

    Source(List(1, 2, 3))
      .map(_ + 1).async
      .map(_ * 2)
      .to(Sink.foreach(println)).run()
  }

  "demonstrate Source" in {
    //#create-materializer
    implicit val system = ActorSystem("QuickStart")
    implicit val materializer = ActorMaterializer()
    //#create-materializer

    //#create-source
    val source: Source[Int, NotUsed] = Source(1 to 100)
    //#create-source

    //#run-source
    source.runForeach(i ⇒ println(i))(materializer)
    //#run-source

    //#transform-source
    val factorials = source.scan(BigInt(1))((acc, next) ⇒ acc * next)

    val result: Future[IOResult] =
      factorials
        .map(num ⇒ ByteString(s"$num\n"))
        .runWith(FileIO.toPath(Paths.get("factorials.txt")))
    //#transform-source

    //#use-transformed-sink
    factorials.map(_.toString).runWith(lineSink("factorial2.txt"))
    //#use-transformed-sink

    //#add-streams
    factorials
      .zipWith(Source(0 to 100))((num, idx) ⇒ s"$idx! = $num")
      .throttle(1, 1.second, 5, ThrottleMode.shaping) // 第一次5个立即通过，接下来需每等待1秒后通过1个
      //#add-streams
      .take(10)
      //#add-streams
      .runForeach(println)
    //#add-streams
     Thread.sleep(10000)
    //#run-source-and-terminate
    val done: Future[Done] = source.runForeach(i ⇒ println(i))(materializer)

    implicit val ec = system.dispatcher
    done.onComplete(_ ⇒ system.terminate())
    //#run-source-and-terminate

    done.futureValue
  }

  //#transform-sink
  def lineSink(filename: String): Sink[String, Future[IOResult]] =
    Flow[String]
      .map(s ⇒ ByteString(s + "\n"))
      .toMat(FileIO.toPath(Paths.get(filename)))(Keep.right)
  //#transform-sink

}
