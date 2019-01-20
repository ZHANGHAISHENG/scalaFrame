package comomns

import java.nio.ByteOrder

import akka.pattern._

import scala.concurrent.duration._
import akka.NotUsed
import akka.actor.{Actor, ActorRef, ActorSystem, Cancellable, Props}
import akka.stream.FanInShape.{Init, Name}
import akka.stream._
import akka.stream.scaladsl._
import akka.util.ByteString
import org.reactivestreams.Subscriber

import scala.collection.immutable
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}
//import scala.collection.parallel.immutable
import scala.concurrent._

object Test11 extends  App {
 // implicit val materializer = ActorMaterializer()
 //#materializer-from-system
 implicit val system = ActorSystem("ExampleSystem")

 implicit val mat = ActorMaterializer() // created from `system`

 implicit def ec: ExecutionContext = system.dispatcher

 /*case class PriorityWorkerPoolShape[In, Out](jobsIn:         Inlet[In],
                                               priorityJobsIn: Inlet[In],
                                               resultsOut:     Outlet[Out]) extends Shape {
    override val inlets: immutable.Seq[Inlet[_]] = jobsIn :: priorityJobsIn :: Nil
    override val outlets: immutable.Seq[Outlet[_]] = resultsOut :: Nil
    override def deepCopy() = PriorityWorkerPoolShape(
      jobsIn.carbonCopy(),
      priorityJobsIn.carbonCopy(),
      resultsOut.carbonCopy())

  }
  object PriorityWorkerPool {
    def apply[In, Out](worker:Flow[In, Out, Any], workerCount: Int): Graph[PriorityWorkerPoolShape[In, Out], NotUsed] = {GraphDSL.create() { implicit b ⇒
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

  val worker1 = Flow[String].map("step 1 " + _)
  val worker2 = Flow[String].map("step 2 " + _)
  RunnableGraph.fromGraph(GraphDSL.create() { implicit b ⇒
    import GraphDSL.Implicits._
    val priorityPool1 = b.add(PriorityWorkerPool(worker1, 4))
    val priorityPool2 = b.add(PriorityWorkerPool(worker2, 2))
    Source(1 to 100).map("job: " + _) ~> priorityPool1.jobsIn
    Source(1 to 100).map("priority job: " + _) ~> priorityPool1.priorityJobsIn

    priorityPool1.resultsOut ~> priorityPool2.jobsIn
    Source(1 to 100).map("one-step, priority " + _) ~> priorityPool2.priorityJobsIn
    priorityPool2.resultsOut ~> Sink.foreach(println)
    ClosedShape
  }).run()


  val topHeadSink = Sink.head[Int]
  val bottomHeadSink = Sink.head[Int]
  val sharedDoubler = Flow[Int].map(_ * 2)

  RunnableGraph.fromGraph(GraphDSL.create(topHeadSink, bottomHeadSink)((_, _)) { implicit builder =>
    (topHS, bottomHS) =>
      import GraphDSL.Implicits._
      val broadcast = builder.add(Broadcast[Int](2))
      Source.single(1) ~> broadcast.in

      broadcast ~> sharedDoubler ~> topHS.in
      broadcast ~> sharedDoubler ~> bottomHS.in
      ClosedShape
  })
*/
 //#simple-partial-graph-dsl
 /*val pickMaxOfThree: Graph[UniformFanInShape[Int, Int], NotUsed] = GraphDSL.create() { implicit b ⇒
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

    Source.single(4) ~> pm3.in(0)
    Source.single(2) ~> pm3.in(1)
    Source.single(3) ~> pm3.in(2)
    pm3.out ~> sink.in
    ClosedShape
  })

  val max: Future[Int] = g.run()
  max.foreach(println)
  Await.result(max, Duration.Inf)*/

 //#sink-combine
 /*val localProcessing = Sink.foreach[Int](println)
  val localProcessing2 = Sink.foreach[Int](println)

  val sink = Sink.combine(localProcessing, localProcessing2)(Broadcast[Int](_))

  val a: NotUsed = Source(List(0, 1, 2)).runWith(sink)
  //#sink-combine

  class PriorityWorkerPoolShape2[In, Out](_init: Init[Out] = Name("PriorityWorkerPool"))
    extends FanInShape[Out](_init) {
    protected override def construct(i: Init[Out]) = new PriorityWorkerPoolShape2(i)

    val jobsIn = newInlet[In]("jobsIn")
    val priorityJobsIn = newInlet[In]("priorityJobsIn")
    // Outlet[Out] with name "out" is automatically created
  }*/

 //#codec
 /*trait Message
  case class Ping(id: Int) extends Message
  case class Pong(id: Int) extends Message

  //#codec-impl
  def toBytes(msg: Message): ByteString = {
    //#implementation-details-elided
    implicit val order = ByteOrder.LITTLE_ENDIAN
    msg match {
      case Ping(id) ⇒ ByteString.newBuilder.putByte(1).putInt(id).result()
      case Pong(id) ⇒ ByteString.newBuilder.putByte(2).putInt(id).result()
    }
    //#implementation-details-elided
  }

  def fromBytes(bytes: ByteString): Message = {
    //#implementation-details-elided
    implicit val order = ByteOrder.LITTLE_ENDIAN
    val it = bytes.iterator
    it.getByte match {
      case 1     ⇒ Ping(it.getInt)
      case 2     ⇒ Pong(it.getInt)
      case other ⇒ throw new RuntimeException(s"parse error: expected 1|2 got $other")
    }
    //#implementation-details-elided
  }
  //#codec-impl

  val codecVerbose = BidiFlow.fromGraph(GraphDSL.create() { b ⇒
    // construct and add the top flow, going outbound
    val outbound = b.add(Flow[Message].map(toBytes))
    // construct and add the bottom flow, going inbound
    val inbound = b.add(Flow[ByteString].map(fromBytes))
    // fuse them together into a BidiShape
    BidiShape.fromFlows(outbound, inbound)
  })

  // this is the same as the above
  val codec: BidiFlow[Message, ByteString, ByteString, Message, NotUsed] = BidiFlow.fromFunctions(toBytes _, fromBytes _)
  // test it by plugging it into its own inverse and closing the right end
  val pingpong = Flow[Message].collect { case Ping(id) ⇒ Pong(id) }
  // val a: BidiFlow[ByteString, Message, Message, ByteString, NotUsed] = codec.reversed
  // val a: BidiFlow[Message, Message, Message, Message, NotUsed] = codec.atop(codec.reversed)
  val flow = codec.atop(codec.reversed).join(pingpong)
  val result: Future[immutable.Seq[Message]] = Source((0 to 9).map(Ping)).via(flow).limit(20).runWith(Sink.seq)
  result.foreach(println)
  Await.result(result, Duration.Inf)*/

 /*RunnableGraph.fromGraph(GraphDSL.create() { implicit b =>
    import GraphDSL.Implicits._
    val source = Source.fromIterator(() ⇒ Iterator.from(0))
    val zip = b.add(ZipWith((left: Int, right: Int) => left))
    val bcast = b.add(Broadcast[Int](2))
    val concat = b.add(Concat[Int]())
    val start = Source.single(0)

    source ~> zip.in0
    zip.out.map { s => println(s); s } ~> bcast ~> Sink.ignore
    zip.in1 <~ concat <~ start
    concat         <~          bcast
    ClosedShape
  })*/

 /*import GraphDSL.Implicits._
  val foldFlow: Flow[Int, Int, Future[Int]] = Flow.fromGraph(GraphDSL.create(Sink.fold[Int, Int](0)(_ + _)) { implicit builder ⇒ fold ⇒
    FlowShape(fold.in, builder.materializedValue.mapAsync(4)(identity).outlet)
  })
  //#graph-dsl-matvalue

  Await.result(Source(1 to 10).via(foldFlow).runWith(Sink.head), 3.seconds) should ===(55)*/

 /* val throttler: Flow[Int, Int, Cancellable] = Flow.fromGraph(
       GraphDSL.create(Source.tick(1.second, 1.second, "test")) { implicit builder ⇒tickSource ⇒
    import GraphDSL.Implicits._
    val zip = builder.add(ZipWith[String, Int, Int](Keep.right))
    tickSource ~> zip.in0
    FlowShape(zip.in1, zip.out)

  })

  //#flow-mat-combine
  // An source that can be signalled explicitly from the outside
  val source: Source[Int, Promise[Option[Int]]] = Source.maybe[Int]

  // A flow that internally throttles elements to 1/second, and returns a Cancellable
  // which can be used to shut down the stream
  val flow: Flow[Int, Int, Cancellable] = throttler

  // A sink that returns the first element of a stream in the returned Future
  val sink: Sink[Int, Future[Int]] = Sink.head[Int]

  // By default, the materialized value of the leftmost stage is preserved
  val r1: RunnableGraph[Promise[Option[Int]]] = source.via(flow).to(sink)

  // Simple selection of materialized values by using Keep.right
  val r2: RunnableGraph[Cancellable] = source.viaMat(flow)(Keep.right).to(sink)
  val r3: RunnableGraph[Future[Int]] = source.via(flow).toMat(sink)(Keep.right)

  // Using runWith will always give the materialized values of the stages added
  // by runWith() itself
  val r4: Future[Int] = source.via(flow).runWith(sink)
  val r5: Promise[Option[Int]] = flow.to(sink).runWith(source)
  val r6: (Promise[Option[Int]], Future[Int]) = flow.runWith(source, sink)

  // Using more complex combinations
  val r7: RunnableGraph[(Promise[Option[Int]], Cancellable)] =
    source.viaMat(flow)(Keep.both).to(sink)

  val r8: RunnableGraph[(Promise[Option[Int]], Future[Int])] =
    source.via(flow).toMat(sink)(Keep.both)

  val r9: RunnableGraph[((Promise[Option[Int]], Cancellable), Future[Int])] =
    source.viaMat(flow)(Keep.both).toMat(sink)(Keep.both)

  val r10: RunnableGraph[(Cancellable, Future[Int])] =
    source.viaMat(flow)(Keep.right).toMat(sink)(Keep.both)

  // It is also possible to map over the materialized values. In r9 we had a
  // doubly nested pair, but we want to flatten it out
  val r11: RunnableGraph[(Promise[Option[Int]], Cancellable, Future[Int])] =
  val a: RunnableGraph[(Promise[Option[Int]], Cancellable, Future[Int])] = r9.mapMaterializedValue {
    case ((promise, cancellable), future) ⇒
      (promise, cancellable, future)
  }

  // Now we can use pattern matching to get the resulting materialized values
  val (promise, cancellable, future) = r11.run()

  // Type inference works as expected
  promise.success(None)
  cancellable.cancel()
  future.map(_ + 3)

  // The result of r11 can be also achieved by using the Graph API
  val r12: RunnableGraph[(Promise[Option[Int]], Cancellable, Future[Int])] =
    RunnableGraph.fromGraph(GraphDSL.create(source, flow, sink)((_, _, _)) { implicit builder ⇒ (src, f, dst) ⇒
      import GraphDSL.Implicits._
      src ~> f ~> dst
      ClosedShape
    })

  //#flow-mat-combine*/

 import GraphDSL.Implicits._

 /*val matValuePoweredSource: Source[String, ActorRef] =
   Source.actorRef[String](bufferSize = 100, overflowStrategy = OverflowStrategy.fail)

  val (actorRef, source) = matValuePoweredSource.preMaterialize()


  actorRef ! "Hello!"

  // pass source around for materialization
  source.runWith(Sink.foreach(println))*/

 //#source-queue
 /*val bufferSize = 10
 val elementsToProcess = 3

 val queue = Source.queue[Int](bufferSize, OverflowStrategy.backpressure)
   .throttle(elementsToProcess, 100.second,0,ThrottleMode.enforcing)
   .map(x ⇒ x)
   .toMat(Sink.foreach(x ⇒ println(s"completed $x")))(Keep.left)
   .run()

 val source = Source(1 to 100)

 source.mapAsync(1)(x ⇒ {
  queue.offer(x).map {
   case QueueOfferResult.Enqueued    ⇒ println(s"enqueued $x")
   case QueueOfferResult.Dropped     ⇒ println(s"dropped $x")
   case QueueOfferResult.Failure(ex) ⇒ println(s"Offer failed ${ex.getMessage}")
   case QueueOfferResult.QueueClosed ⇒ println("Source Queue closed")
  }
 }).runWith(Sink.ignore)*/
 //#source-queue
 /*
 val (firstElem, source) = Source.maybe[Int].concat(Source(1 until 20)).toMat(
  PartitionHub.sink((size, elem) ⇒ elem % size, startAfterNrOfConsumers = 2, bufferSize = 1))(Keep.both).run()

 // Second cannot be overwhelmed since the first one throttles the overall rate, and second allows a higher rate
 val f2 = source.throttle(10, 1000.millis, 8, ThrottleMode.enforcing).map(x => "f2:"+x).runWith(Sink.foreach(println))
 val f1 = source.throttle(1, 1000.millis, 1, ThrottleMode.shaping).map(x => "f1:"+x).runWith(Sink.foreach(println))

 // Ensure subscription of Sinks. This is racy but there is no event we can hook into here.
 Thread.sleep(100)
 firstElem.success(Some(0))
 //f1.foreach(x => println("f1:"+x))
 //f2.foreach(x => println("f2:"+x))
 Thread.sleep(100000)
*/
 /* Source(List(1,2,3,3,6,5,6,5,5,5,6,5,5,5,5,5,5,5,8))
   .throttle(10, 100.second,2,ThrottleMode.enforcing)
   .map(x ⇒ {Thread.sleep(1000);println(x);x})
   .toMat(Sink.foreach(x ⇒ println(s"completed $x")))(Keep.left)
   .run()
}*/
}
