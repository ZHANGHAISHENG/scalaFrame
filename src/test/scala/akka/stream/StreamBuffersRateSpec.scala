package akka.stream

import akka.NotUsed
import akka.stream.testkit.scaladsl.TestSource
import akka.stream.scaladsl._
import akka.stream.testkit.scaladsl._
import scala.util.Random
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.collection.immutable
import akka.testkit.{ AkkaSpec, TestLatch }

class StreamBuffersRateSpec extends AkkaSpec {
  implicit val materializer = ActorMaterializer()

  "Demonstrate pipelining" in {
    def println(s: Any) = ()
    //#pipelining
    Source(1 to 3)
      .map { i ⇒ println(s"A: $i"); i }.async
      .map { i ⇒ println(s"B: $i"); i }.async
      .map { i ⇒ println(s"C: $i"); i }.async
      .runWith(Sink.ignore)
    //#pipelining
  }

  "Demonstrate buffer sizes" in {
    //#materializer-buffer
    val materializer = ActorMaterializer(
      ActorMaterializerSettings(system)
        .withInputBuffer(
          initialSize = 64,
          maxSize = 64))
    //#materializer-buffer

    //#section-buffer
    val section = Flow[Int].map(_ * 2).async
      .addAttributes(Attributes.inputBuffer(initial = 1, max = 1)) // the buffer size of this map is 1
    val flow = section.via(Flow[Int].map(_ / 2)).async // the buffer size of this map is the default
    //#section-buffer
  }

  "buffering abstraction leak" in {
    //#buffering-abstraction-leak
    import scala.concurrent.duration._
    case class Tick()

    val graph = RunnableGraph.fromGraph(GraphDSL.create() { implicit b ⇒
      import GraphDSL.Implicits._

      // this is the asynchronous stage in this graph
      val zipper = b.add(ZipWith[Tick, Int, Int]((tick, count) ⇒ count).async)

      Source.tick(initialDelay = 3.second, interval = 3.second, Tick()) ~> zipper.in0

      Source.tick(initialDelay = 1.second, interval = 1.second, "message!")
        .conflateWithSeed(seed = (_) ⇒ 1)((count, _) ⇒ count + 1) ~> zipper.in1

      zipper.out ~> Sink.foreach(println)
      ClosedShape
    })
    graph.run()
    Thread.sleep(20000)
    //#buffering-abstraction-leak
  }
  /**
    *
    * 输出：
    *
    *  如果 zipper async,只会输出1，
    *  否则输出： （速率不同就累加）
          1
          1
          3
          4
          3
          3
    */

  "explcit buffers" in {
    trait Job
    def inboundJobsConnector(): Source[Job, NotUsed] = Source.empty
    //#explicit-buffers-backpressure
    // Getting a stream of jobs from an imaginary external system as a Source
    val jobs: Source[Job, NotUsed] = inboundJobsConnector()
    jobs.buffer(1000, OverflowStrategy.backpressure)
    //#explicit-buffers-backpressure

    //#explicit-buffers-droptail
    jobs.buffer(1000, OverflowStrategy.dropTail)
    //#explicit-buffers-droptail

    //#explicit-buffers-dropnew
    jobs.buffer(1000, OverflowStrategy.dropNew)
    //#explicit-buffers-dropnew

    //#explicit-buffers-drophead
    jobs.buffer(1000, OverflowStrategy.dropHead)
    //#explicit-buffers-drophead

    //#explicit-buffers-dropbuffer
    jobs.buffer(1000, OverflowStrategy.dropBuffer)
    //#explicit-buffers-dropbuffer

    //#explicit-buffers-fail
    jobs.buffer(1000, OverflowStrategy.fail)
    //#explicit-buffers-fail

  }

  "conflate should sample" in {
    //#conflate-sample
    val p = 0.01
    val sampleFlow = Flow[Double]
      .conflateWithSeed(immutable.Seq(_)) {  //生产者生产速度过快，0.01的概率取值
        case (acc, elem) if Random.nextDouble < p ⇒ acc :+ elem
        case (acc, _)                             ⇒ acc
      }
      .mapConcat(identity)
    //#conflate-sample

    val fut = Source(1 to 1000)
      .map(_.toDouble)
      .via(sampleFlow)
      .runWith(Sink.fold(Seq.empty[Double])(_ :+ _))

    val a: Seq[Double] = fut.futureValue
    println(a.length) //输出1000 （计算机消费跟得上速度就不会走conflateWithSeed）
  }

  "expand should repeat last" in {
    //#expand-last
    val lastFlow = Flow[Double]
      .expand(Iterator.continually(_))
    //#expand-last

    val (probe, fut) = TestSource.probe[Double]
      .via(lastFlow)
      .grouped(10)  //消费者需要10个元素
      .toMat(Sink.head)(Keep.both)
      .run()

    probe.sendNext(1.0) //生产者只给了一个
    val expanded = fut.futureValue
    expanded.size shouldBe 10
    expanded.sum shouldBe 10

    //Iterator.continually(2).foreach(println(_)) //不停输出2
  }
  "expand should track drift" in {
    //#expand-drift
    val driftFlow = Flow[Double].expand(i ⇒ Iterator.from(0).map(i -> _))
    //#expand-drift
    val latch = TestLatch(2)
    val realDriftFlow: Flow[Double, (Double, Int), NotUsed] = Flow[Double]
      .expand(d ⇒ { latch.countDown(); Iterator.from(0).map(d -> _) })

    val (pub, sub) = TestSource.probe[Double]
      .via(realDriftFlow)
      .toMat(TestSink.probe[(Double, Int)])(Keep.both)
      .run()

    sub.request(1)
    pub.sendNext(1.0)
    sub.expectNext((1.0, 0))

    sub.requestNext((1.0, 1))
    sub.requestNext((1.0, 2))
    sub.requestNext((1.0, 3))
    sub.requestNext((1.0, 4))
    sub.requestNext((1.0, 5))

    pub.sendNext(2.0)
    Await.ready(latch, 1.second)
    sub.requestNext((2.0, 0))

   // Iterator.from(10).map(12 -> _).foreach(println(_))
    /**
      * (12,125974)
        (12,125975)
        (12,125976)
      */
  }

}