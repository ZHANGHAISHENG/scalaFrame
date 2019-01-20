package comomns

import java.nio.ByteOrder

import akka.NotUsed
import akka.actor.ActorSystem
import akka.dispatch.forkjoin.ThreadLocalRandom
import akka.stream.{ThrottleMode, _}
import akka.stream.scaladsl._
import akka.stream.stage._
import akka.stream.testkit.{TestPublisher, TestSubscriber}
import akka.testkit.EventFilter
import akka.util.ByteString

import scala.collection.immutable.Iterable
import scala.collection.mutable
import scala.collection.parallel.immutable
//import scala.collection.parallel.immutable
import scala.concurrent._
import scala.concurrent.duration._

object Test10 extends  App {
  implicit val materializer = ActorMaterializer()
  implicit def system: ActorSystem = ActorSystem("test")
  implicit def ec: ExecutionContext = system.dispatcher

  /*class Duplicator[A](la: A) extends GraphStage[FlowShape[A, A]] {

    val in = Inlet[A]("Duplicator.in")
    val out = Outlet[A]("Duplicator.out")

    val shape = FlowShape.of(in, out)

    override def createLogic(inheritedAttributes: Attributes): GraphStageLogic =
      new GraphStageLogic(shape) {
        // Again: note that all mutable state
        // MUST be inside the GraphStageLogic
        var lastElem: Option[A] = None

        setHandler(in, new InHandler {
          override def onPush(): Unit = {
            val elem = grab(in)
            lastElem = Some(elem)
            println("----------------lastElem:" + lastElem.isDefined)
            push(out, elem)

          }

          override def onUpstreamFinish(): Unit = {

            println("----------------onUpstreamFinish:")
            //emit(out, la)
            push(out, la)
            complete(out)
          }

        })
        setHandler(out, new OutHandler {
          override def onPull(): Unit = {
            println("----------------onPull")
            if (lastElem.isDefined) {
              push(out, lastElem.get)
              lastElem = None
            } else {
              pull(in)
            }
          }
        })
      }
  }

  // tests:
  val duplicator = Flow.fromGraph(new Duplicator[Int](4))

  val result: Future[Seq[Int]] =
    Source(Vector(1, 2, 3))
      .via(duplicator)
      .runFold(Seq.empty[Int])((elem, acc) ⇒ elem :+ acc)

  result.map(println(_))
  Await.result(result, Duration.Inf)*/

  /*final class RandomLettersSource extends GraphStage[SourceShape[String]] {
    val out = Outlet[String]("RandomLettersSource.out")
    override val shape: SourceShape[String] = SourceShape(out)

    override def createLogic(inheritedAttributes: Attributes) =
      new GraphStageLogic(shape) with StageLogging {
        setHandler(out, new OutHandler {
          override def onPull(): Unit = {
            val c = nextChar() // ASCII lower case letters

            // `log` is obtained from materializer automatically (via StageLogging)
            log.debug("Randomly generated: [{}]", c)

            push(out, c.toString)
          }
        })
      }

    def nextChar(): Char =
      ThreadLocalRandom.current().nextInt('a', 'z'.toInt + 1).toChar
  }
  //#operator-with-logging
  val n = 10*/


  //#async-side-channel
  // will close upstream in all materializations of the graph stage instance
  // when the future completes
 /* class KillSwitch[A](switch: Future[Unit]) extends GraphStage[FlowShape[A, A]] {

    val in = Inlet[A]("KillSwitch.in")
    val out = Outlet[A]("KillSwitch.out")

    val shape = FlowShape.of(in, out)

    override def createLogic(inheritedAttributes: Attributes): GraphStageLogic =
      new GraphStageLogic(shape) {

        override def preStart(): Unit = {
          val callback: AsyncCallback[Unit] = getAsyncCallback[Unit] { (_) ⇒
            completeStage()
          }
          switch.foreach(callback.invoke)
        }

        setHandler(in, new InHandler {
          override def onPush(): Unit = { push(out, grab(in)) }
        })
        setHandler(out, new OutHandler {
          override def onPull(): Unit = { pull(in) }
        })
      }
  }
  //#async-side-channel

  // tests:

  val switch = Promise[Unit]()
  val duplicator = Flow.fromGraph(new KillSwitch[Int](switch.future))

  val in = TestPublisher.probe[Int]()
  val out = TestSubscriber.probe[Int]()

  Source.fromPublisher(in)
    .via(duplicator)
    .to(Sink.fromSubscriber(out))
    .withAttributes(Attributes.inputBuffer(1, 1))
    .run()

  val sub = in.expectSubscription()

  out.request(1)

  sub.expectRequest()
  sub.sendNext(1)

  out.expectNext(1)

  switch.success(Unit)

  out.expectComplete()*/

  //#detached
 /* class TwoBuffer[A] extends GraphStage[FlowShape[A, A]] {

    val in = Inlet[A]("TwoBuffer.in")
    val out = Outlet[A]("TwoBuffer.out")

    val shape = FlowShape.of(in, out)

    override def createLogic(inheritedAttributes: Attributes): GraphStageLogic =
      new GraphStageLogic(shape) {

        val buffer = mutable.Queue[A]()
        def bufferFull = buffer.size == 2
        var downstreamWaiting = false

        override def preStart(): Unit = {
          // a detached stage needs to start upstream demand
          // itself as it is not triggered by downstream demand
          pull(in)
        }

        setHandler(in, new InHandler {
          override def onPush(): Unit = {
            val elem = grab(in)
            buffer.enqueue(elem)
            if (downstreamWaiting) {
              downstreamWaiting = false
              val bufferedElem = buffer.dequeue()
              push(out, bufferedElem)
            }
            if (!bufferFull) {
              pull(in)
            }
          }

          override def onUpstreamFinish(): Unit = {
            if (buffer.nonEmpty) {
              // emit the rest if possible
              emitMultiple(out, buffer.toIterator)
            }
            completeStage()
          }
        })

        setHandler(out, new OutHandler {
          override def onPull(): Unit = {
            if (buffer.isEmpty) {
              downstreamWaiting = true
            } else {
              val elem = buffer.dequeue
              push(out, elem)
            }
            if (!bufferFull && !hasBeenPulled(in)) {
              pull(in)
            }
          }
        })
      }

  }
  //#detached

  // tests:
  val result1 = Source(Vector(1, 2, 3))
    .via(new TwoBuffer)
    .runFold(Vector.empty[Int])((acc, n) ⇒ acc :+ n)

  Await.result(result1, 3.seconds)
  result1.foreach(println)

  val subscriber = TestSubscriber.manualProbe[Int]()
  val publisher = TestPublisher.probe[Int]()
  val flow2 =
    Source.fromPublisher(publisher)
      .via(new TwoBuffer)
      .to(Sink.fromSubscriber(subscriber))

  val result2 = flow2.run()

  val sub = subscriber.expectSubscription()
  // this happens even though the subscriber has not signalled any demand
  publisher.sendNext(1)
  publisher.sendNext(2)

  sub.cancel()*/

  import GraphDSL.Implicits._
  /*RunnableGraph.fromGraph(GraphDSL.create() { implicit builder =>
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
  trait Message
  case class Ping(id: Int) extends Message
  case class Pong(id: Int) extends Message

  def toBytes(msg: Message): ByteString = {
    implicit val order = ByteOrder.LITTLE_ENDIAN
    msg match {
      case Ping(id) ⇒ ByteString.newBuilder.putByte(1).putInt(id).result()
      case Pong(id) ⇒ ByteString.newBuilder.putByte(2).putInt(id).result()
    }
  }

  def fromBytes(bytes: ByteString): Message = {
    implicit val order = ByteOrder.LITTLE_ENDIAN
    val it = bytes.iterator
    it.getByte match {
      case 1     ⇒ Ping(it.getInt)
      case 2     ⇒ Pong(it.getInt)
      case other ⇒ throw new RuntimeException(s"parse error: expected 1|2 got $other")
    }
  }

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
*/

 /* val framing = BidiFlow.fromGraph(GraphDSL.create() { b ⇒
    implicit val order = ByteOrder.LITTLE_ENDIAN

    def addLengthHeader(bytes: ByteString): ByteString = {
      val len = bytes.length
      ByteString.newBuilder.putInt(len).append(bytes).result()
    }

    class FrameParser extends GraphStage[FlowShape[ByteString, ByteString]] {

      val in = Inlet[ByteString]("FrameParser.in")
      val out = Outlet[ByteString]("FrameParser.out")
      override val shape = FlowShape.of(in, out)

      override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {

        // this holds the received but not yet parsed bytes
        var stash = ByteString.empty
        // this holds the current message length or -1 if at a boundary
        var needed = -1

        setHandler(out, new OutHandler {
          override def onPull(): Unit = {
            if (isClosed(in)) run()
            else pull(in)
          }
        })
        setHandler(in, new InHandler {
          override def onPush(): Unit = {
            val bytes = grab(in)
            stash = stash ++ bytes
            run()
          }

          override def onUpstreamFinish(): Unit = {
            // either we are done
            if (stash.isEmpty) completeStage()
            // or we still have bytes to emit
            // wait with completion and let run() complete when the
            // rest of the stash has been sent downstream
            else if (isAvailable(out)) run()
          }
        })

        private def run(): Unit = {
          if (needed == -1) {
            // are we at a boundary? then figure out next length
            if (stash.length < 4) {
              if (isClosed(in)) completeStage()
              else pull(in)
            } else {
              needed = stash.iterator.getInt
              stash = stash.drop(4)
              run() // cycle back to possibly already emit the next chunk
            }
          } else if (stash.length < needed) {
            // we are in the middle of a message, need more bytes,
            // or have to stop if input closed
            if (isClosed(in)) completeStage()
            else pull(in)
          } else {
            // we have enough to emit at least one message, so do it
            val emit = stash.take(needed)
            stash = stash.drop(needed)
            needed = -1
            push(out, emit)
          }
        }
      }
    }

    val outbound: FlowShape[ByteString, ByteString] = b.add(Flow[ByteString].map(addLengthHeader))
    val inbound: FlowShape[ByteString, ByteString] = b.add(Flow[ByteString].via(new FrameParser))
    BidiShape.fromFlows(outbound, inbound)
  })*/

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
 // Source.single(0).via(partial).to(Sink.foreach(println)).run()

    val a = RunnableGraph.fromGraph(GraphDSL.create() { implicit builder =>
    val A: Outlet[Int]                  = builder.add(Source.fromIterator(() => List(1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1).toIterator)).out
    val B: UniformFanOutShape[Int, Int] = builder.add(Broadcast[Int](2))
    val C: UniformFanInShape[Int, Int]  = builder.add(Merge[Int](2))
    val D: FlowShape[Int, Int]          = builder.add(Flow[Int])
    val E: UniformFanOutShape[Int, Int] = builder.add(Balance[Int](2))
    val F: UniformFanInShape[Int, Int]  = builder.add(Merge[Int](2))
    val G: Inlet[Any]                   = builder.add(Sink.foreach(println)).in
    val G1: FlowShape[Int, Int]         = builder.add(Flow[Int].map(x => {println("e->g:"+x); x}))
    val G2: FlowShape[Int, Int]         = builder.add(Flow[Int].map(x => {println("e->f:"+x); x}))
    val G3: FlowShape[Int, Int]         = builder.add(Flow[Int].map(x => {println("b->c:"+x); x}))
    val G4: FlowShape[Int, Int]         = builder.add(Flow[Int].map(x => {println("b->d:"+x); x}))

                  C  <~  F
    A  ~>  B  ~> G3  ~>  C  ~>  F
           B   ~> G4 ~>  D  ~>  E ~>  G2  ~>   F
    E ~>  G1  ~> G

    ClosedShape
  })

  a.run()

}
