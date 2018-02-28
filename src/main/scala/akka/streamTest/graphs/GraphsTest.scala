package akka.streamTest.graphs
import akka.stream.scaladsl.{Flow, _}
import akka.testkit.AkkaSpec

import scala.collection.immutable
import scala.concurrent.{Await, Future}
import akka.NotUsed
import akka.stream._
import akka.stream.testkit.TestSubscriber.ManualProbe
import akka.stream.testkit.{TestPublisher, TestSubscriber}

import scala.concurrent.duration._

class GraphsTest extends AkkaSpec {

  implicit val ec = system.dispatcher

  implicit val materializer = ActorMaterializer()

  "Fan-out Test" in {
    //format: OFF
    //#simple-graph-dsl
    val g = RunnableGraph.fromGraph(GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] =>
      import GraphDSL.Implicits._
      val in = Source(1 to 6)
      val out = Sink.foreach(println)

      val unzipWith: FanOutShape2[Int, Int, Int] = builder.add(UnzipWith[Int,Int,Int]((n: Int) => (n, n)))
      in ~> unzipWith.in
      unzipWith.out0  ~> out
      unzipWith.out1  ~> out

      val unzip = builder.add(Unzip[Int, String]())
      Source(List((1 , "a"), 2 → "b", 3 → "c")) ~> unzip.in
      unzip.out0 ~> out
      unzip.out1 ~> out

      val bcast = builder.add(Broadcast[Int](2)) //1个输入输出 2 次
      in ~> bcast.in
      bcast.out(0) ~> Sink.foreach(println)
      bcast.out(1) ~> Sink.foreach(println)

      //val merge2 = builder.add(Merge[Int](2))
      val balance = builder.add(Balance[Int](2)) //1个输入输出 1 次（选择其中一条路线）
      in ~> balance  ~> out
      balance  ~> out

      ClosedShape
    })
    g.run()
  }

  "Fan-In Test" in {
    //format: OFF
    //#simple-graph-dsl
    val g = RunnableGraph.fromGraph(GraphDSL.create() { implicit b: GraphDSL.Builder[NotUsed] =>
      import GraphDSL.Implicits._
      val source1 = Source(0 to 3)
      val source2 = Source(4 to 9)
      val out = Sink.foreach(println)

      /* val m1 = b.add(Merge[Int](2))
       source1 ~> m1.in(0)
       source2 ~> m1.in(1)
       m1.out ~> Flow[Int].map(_ + 1) ~> out //输出1 ~ 10*/

      /* val zip = b.add(Zip[Int, String]())
       Source(1 to 4) ~> zip.in0
       Source(List("A", "B", "C", "D", "E", "F")) ~> zip.in1
       zip.out ~> out // 输出 （1，A）...(5,D)*/

      /*val zipWith = b.add(ZipWith[Int, Int, Int]((_: Int) + (_: Int)))
      Source(1 to 4) ~> zipWith.in0
      Source(2 to 10) ~> zipWith.in1
      zipWith.out ~> out // 输出： 1 + 2， 3 + 4, ...., 4 + 5*/

      /* val numElements = 10
       val preferred = Source(Stream.fill(numElements)(1))
       val aux1 = Source(Stream.fill(numElements)(2))
       val aux2 = Source(Stream.fill(numElements)(3))
       val mergeF = b.add(MergePreferred[Int](2))
       mergeF.out.grouped(numElements) ~> out
       preferred ~> mergeF.preferred  //同时进入时，优先输出preferred中的数字
       aux1 ~> mergeF.in(0)
       aux2 ~> mergeF.in(1)*/
      /*
      输出：
      Vector(2, 3, 1, 1, 2, 3, 1, 1, 1, 1)
      Vector(1, 1, 1, 1, 2, 3, 2, 3, 2, 3)
      Vector(2, 3, 2, 3, 2, 3, 2, 3, 2, 3)
      */

      /* val s1 = Source(1 to 3)
       val s2 = Source(4 to 6)
       val s3 = Source(7 to 9)

       val priorities = Seq(100, 10, 1) // 当 3个元素都准备输出时，按照概率输出
       val mergeP = b.add(MergePrioritized[Int](priorities))
       val delayFirst = b.add(Flow[Int].initialDelay(50.millis))
       s1 ~> mergeP.in(0)
       s2 ~> mergeP.in(1)
       s3 ~> mergeP.in(2)
       mergeP.out  ~> delayFirst ~> out*/

      val concat1 = b add Concat[Int]()
      val concat2 = b add Concat[Int]()
      Source(List.empty[Int]) ~> concat1.in(0)
      Source(1 to 4) ~> concat1.in(1)

      concat1.out ~> concat2.in(0)
      Source(5 to 10) ~> concat2.in(1)

      concat2.out ~> out  //依次输出1 ~ 10

      ClosedShape
    })
    g.run()
    Thread.sleep(2000)
  }

  "Constructing and combining Partial Graphs" in {
    //#simple-partial-graph-dsl
    val pickMaxOfThree = GraphDSL.create() { implicit b ⇒
      import GraphDSL.Implicits._

      val zip1 = b.add(ZipWith[Int, Int, Int](math.max _))
      val zip2 = b.add(ZipWith[Int, Int, Int](math.max _))
      zip1.out ~> zip2.in0

      UniformFanInShape(zip2.out, zip1.in0, zip1.in1, zip2.in1)
    }

    val resultSink = Sink.head[Int]

    val g = RunnableGraph.fromGraph(GraphDSL.create(resultSink) { implicit b ⇒ sink ⇒
      import GraphDSL.Implicits._

      // importing the partial graph will return its shape (inlets & outlets)
      val pm3 = b.add(pickMaxOfThree)

      Source.single(1) ~> pm3.in(0)
      Source.single(2) ~> pm3.in(1)
      Source.single(3) ~> pm3.in(2)
      pm3.out ~> sink.in
      ClosedShape
    })

    val max: Future[Int] = g.run()
    Await.result(max, 300.millis) should equal(3)
    //#simple-partial-graph-dsl
  }



  "build source from partial graph" in {
    //#source-from-partial-graph-dsl
    val pairs = Source.fromGraph(GraphDSL.create() { implicit b ⇒
      import GraphDSL.Implicits._

      // prepare graph elements
      val zip = b.add(Zip[Int, Int]())
      def ints = Source.fromIterator(() ⇒ Iterator.from(1))

      // connect the graph
      ints.filter(_ % 2 != 0) ~> zip.in0
      ints.filter(_ % 2 == 0) ~> zip.in1

      // expose port
      SourceShape(zip.out)
    })
    val firstPair: Future[(Int, Int)] = pairs.runWith(Sink.head)
    //#source-from-partial-graph-dsl
    Await.result(firstPair, 300.millis) should equal(1 -> 2)
  }

  "build sink from partial graph" in {
    val pairs = Sink.fromGraph(GraphDSL.create() { implicit b ⇒
      import GraphDSL.Implicits._

      val bcast = b.add(Broadcast[Int](3))
      bcast.out(0) ~> Flow[Int].filter(_ == 0) ~> Sink.foreach(println)
      bcast.out(1) ~> Flow[Int].filter(_ == 1) ~> Sink.foreach(println)
      bcast.out(2) ~> Flow[Int].filter(_ == 2) ~> Sink.foreach(println)
      SinkShape(bcast.in)
    })

    Source(List(0, 1, 2)).runWith(pairs)

  }

  "build flow from partial graph" in {
    val pairUpWithToString = Flow.fromGraph(GraphDSL.create() { implicit b ⇒
        import GraphDSL.Implicits._
        val broadcast = b.add(Broadcast[Int](2))
        val zip = b.add(Zip[Int, String]())

        broadcast.out(0).map(identity) ~> zip.in0
        broadcast.out(1).map(_.toString) ~> zip.in1
        FlowShape(broadcast.in, zip.out)
      })
    val (_, matSink: Future[(Int, String)]) = pairUpWithToString.runWith(Source(List(1)), Sink.head)
    Await.result(matSink, 300.millis) should equal(1 -> "1")
  }


  "building a reusable component" in {

    //#graph-dsl-components-shape
    case class PriorityWorkerPoolShape[In, Out](jobsIn:         Inlet[In],
                                                 priorityJobsIn: Inlet[In],
                                                 resultsOut:     Outlet[Out]) extends Shape {
      override val inlets: immutable.Seq[Inlet[_]] = jobsIn :: priorityJobsIn :: Nil
      override val outlets: immutable.Seq[Outlet[_]] = resultsOut :: Nil
      override def deepCopy() = PriorityWorkerPoolShape(
        jobsIn.carbonCopy(),
        priorityJobsIn.carbonCopy(),
        resultsOut.carbonCopy())

    }
    //#graph-dsl-components-shape

    //#graph-dsl-components-create
    object PriorityWorkerPool {
      def apply[In, Out](worker:  Flow[In, Out, Any], workerCount: Int): Graph[PriorityWorkerPoolShape[In, Out], NotUsed] = {
        GraphDSL.create() { implicit b ⇒
          import GraphDSL.Implicits._
          val priorityMerge = b.add(MergePreferred[In](1))
          val balance = b.add(Balance[In](workerCount))
          val resultsMerge = b.add(Merge[Out](workerCount))

          priorityMerge ~> balance

          for (i ← 0 until workerCount) balance.out(i) ~> worker ~> resultsMerge.in(i)

          PriorityWorkerPoolShape(
            jobsIn = priorityMerge.in(0),
            priorityJobsIn = priorityMerge.preferred,
            resultsOut = resultsMerge.out)
        }
      }
    }
    //#graph-dsl-components-create
    //#graph-dsl-components-use
    val worker1 = Flow[String].map("step 1 " + _)
    RunnableGraph.fromGraph(GraphDSL.create() { implicit b ⇒
      import GraphDSL.Implicits._
      val priorityPool1 = b.add(PriorityWorkerPool(worker1, 4))
      Source(1 to 2).map("job: " + _) ~> priorityPool1.jobsIn
      Source(1 to 2).map("priority job: " + _) ~> priorityPool1.priorityJobsIn
      priorityPool1.resultsOut ~> Sink.foreach(println)
      ClosedShape
    }).run()
    //#graph-dsl-components-use
  }

  "nested flow and materialized value" in {
    //#nested-flow
    val nestedSource =
      Source.single(0) // An atomic source
        .map(_ + 1) // an atomic processing stage
        .named("nestedSource") // wraps up the current Source and gives it a name

    val nestedFlow =
      Flow[Int].filter(_ != 0) // an atomic processing stage
        .map(_ - 2) // another atomic processing stage
        .named("nestedFlow") // wraps up the Flow, and gives it a name

    val nestedSink =
      nestedFlow.toMat(Sink.fold(0)(_ + _))(Keep.right) // wire an atomic sink to the nestedFlow
        .named("nestedSink") // wrap it up

    // Create a RunnableGraph
    val runnableGraph = nestedSource.toMat(nestedSink)(Keep.right) //物化sink的值
    val result: Future[Int] = runnableGraph.run()

    val r =  Await.result(result, 300.millis)
    println(r) // -1

    val runnableGraph2  = nestedSource.toMat(nestedSink)(( L: NotUsed, R: Future[Int]) => R.map(_  + 1))
    val r2 = Await.result(runnableGraph2.run(), 300.millis)
    println(r2) // 0

  }

}
