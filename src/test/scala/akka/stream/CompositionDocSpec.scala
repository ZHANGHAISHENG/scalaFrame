package akka.stream

import akka.NotUsed
import akka.stream._
import akka.stream.scaladsl.Tcp.OutgoingConnection
import akka.stream.scaladsl._
import akka.testkit.AkkaSpec
import akka.util.ByteString
import scala.concurrent.duration._

import scala.concurrent.{Await, Future, Promise}

class CompositionDocSpec extends AkkaSpec {

  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()

  "nonnested flow" in {
    //#non-nested-flow
    val r = Source.single(0)
      .map(_ + 1)
      .filter(_ != 0)
      .map(_ - 2)
      .to(Sink.fold(0)(_ + _))

  }

  "nested flow" in {
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


  "complex graph" in {
    // format: OFF
    //#complex-graph
    import GraphDSL.Implicits._
    RunnableGraph.fromGraph(GraphDSL.create() { implicit builder =>
      val A: Outlet[Int]                  = builder.add(Source.single(0)).out
      val B: UniformFanOutShape[Int, Int] = builder.add(Broadcast[Int](2))
      val C: UniformFanInShape[Int, Int]  = builder.add(Merge[Int](2))
      val D: FlowShape[Int, Int]          = builder.add(Flow[Int].map(_ + 1))
      val E: UniformFanOutShape[Int, Int] = builder.add(Balance[Int](2))
      val F: UniformFanInShape[Int, Int]  = builder.add(Merge[Int](2))
      val G: Inlet[Any]                   = builder.add(Sink.foreach(println)).in

      C     <~      F
      A  ~>  B  ~>  C     ~>      F
      B  ~>  D  ~>  E  ~>  F
      E  ~>  G

      ClosedShape
    })
    //#complex-graph

    //#complex-graph-alt
    import GraphDSL.Implicits._
    RunnableGraph.fromGraph(GraphDSL.create() { implicit builder =>
      val B = builder.add(Broadcast[Int](2))
      val C = builder.add(Merge[Int](2))
      val E = builder.add(Balance[Int](2))
      val F = builder.add(Merge[Int](2))

      Source.single(0) ~> B.in; B.out(0) ~> C.in(1); C.out ~> F.in(0)
      C.in(0) <~ F.out

      B.out(1).map(_ + 1) ~> E.in; E.out(0) ~> F.in(1)
      E.out(1) ~> Sink.foreach(println)
      ClosedShape
    })
    //#complex-graph-alt
    // format: ON
  }

  "partial graph" in {
    // format: OFF
    //#partial-graph
    import GraphDSL.Implicits._
    val partial = GraphDSL.create() { implicit builder =>
      val B = builder.add(Broadcast[Int](2))
      val C = builder.add(Merge[Int](2))
      val E = builder.add(Balance[Int](2))
      val F = builder.add(Merge[Int](2))

      C  <~  F
      B  ~>                            C  ~>  F
      B  ~>  Flow[Int].map(_ + 1)  ~>  E  ~>  F
      FlowShape(B.in, E.out(1))
    }.named("partial")
    //#partial-graph
    // format: ON

    //#partial-use
    Source.single(0).via(partial).to(Sink.ignore)
    //#partial-use

    // format: OFF
    //#partial-flow-dsl
    // Convert the partial graph of FlowShape to a Flow to get
    // access to the fluid DSL (for example to be able to call .filter())
    val flow = Flow.fromGraph(partial)

    // Simple way to create a graph backed Source
    val source = Source.fromGraph( GraphDSL.create() { implicit builder =>
      val merge = builder.add(Merge[Int](2))
      Source.single(0)      ~> merge
      Source(List(2, 3, 4)) ~> merge

      // Exposing exactly one output port
      SourceShape(merge.out)
    })

    // Building a Sink with a nested Flow, using the fluid DSL
    val sink = {
      val nestedFlow = Flow[Int].map(_ * 2).take(10).named("nestedFlow")
      nestedFlow.to(Sink.foreach(println))
    }

    // Putting all together
    val closed = source.via(flow.filter(_ >= 0)).to(sink).run()
    //#partial-flow-dsl
    // format: ON
  }

  "closed graph" in {
    //#embed-closed
    val closed1 = Source.single(0).to(Sink.foreach(println))
    val closed2 = RunnableGraph.fromGraph(GraphDSL.create() { implicit builder ⇒
      val embeddedClosed: ClosedShape = builder.add(closed1)
      // …
      embeddedClosed
    })
    //#embed-closed
  }

  "materialized values" in {
    //#mat-combine-1
    // Materializes to Promise[Option[Int]]                                   (red)
    val source: Source[Int, Promise[Option[Int]]] = Source.maybe[Int]

    // Materializes to NotUsed                                               (black)
    val flow1: Flow[Int, Int, NotUsed] = Flow[Int].take(100)

    // Materializes to Promise[Int]                                          (red)
    val nestedSource: Source[Int, Promise[Option[Int]]] =
      source.viaMat(flow1)(Keep.left).named("nestedSource")
    //#mat-combine-1

    //#mat-combine-2
    // Materializes to NotUsed                                                (orange)
    val flow2: Flow[Int, ByteString, NotUsed] = Flow[Int].map { i ⇒ ByteString(i.toString) }

    // Materializes to Future[OutgoingConnection]                             (yellow)
    val flow3: Flow[ByteString, ByteString, Future[OutgoingConnection]] =
      Tcp().outgoingConnection("localhost", 8080)

    // Materializes to Future[OutgoingConnection]                             (yellow)
    val nestedFlow: Flow[Int, ByteString, Future[OutgoingConnection]] =
      flow2.viaMat(flow3)(Keep.right).named("nestedFlow")
    //#mat-combine-2

    //#mat-combine-3
    // Materializes to Future[String]                                         (green)
    val sink: Sink[ByteString, Future[String]] = Sink.fold("")(_ + _.utf8String)

    // Materializes to (Future[OutgoingConnection], Future[String])           (blue)
    val nestedSink: Sink[Int, (Future[OutgoingConnection], Future[String])] =
      nestedFlow.toMat(sink)(Keep.both)
    //#mat-combine-3

    //#mat-combine-4
    case class MyClass(private val p: Promise[Option[Int]], conn: OutgoingConnection) {
      def close() = p.trySuccess(None)
    }

    def f(
           p:    Promise[Option[Int]],
           rest: (Future[OutgoingConnection], Future[String])): Future[MyClass] = {

      val connFuture = rest._1
      connFuture.map(MyClass(p, _))
    }
    // Materializes to Future[MyClass]                                        (purple)
    val runnableGraph: RunnableGraph[Future[MyClass]] =
      nestedSource.toMat(nestedSink)(f)
    //#mat-combine-4
  }

  "attributes" in {
    //#attributes-inheritance
    import Attributes._
    val nestedSource =
      Source.single(0)
        .map(_ + 1)
        .named("nestedSource") // Wrap, no inputBuffer set

    val nestedFlow =
      Flow[Int].filter(_ != 0)
        .via(Flow[Int].map(_ - 2).withAttributes(inputBuffer(4, 4))) // override
        .named("nestedFlow") // Wrap, no inputBuffer set

    val nestedSink =
      nestedFlow.to(Sink.fold(0)(_ + _)) // wire an atomic sink to the nestedFlow
        .withAttributes(name("nestedSink") and inputBuffer(3, 3)) // override
    //#attributes-inheritance
  }
}
