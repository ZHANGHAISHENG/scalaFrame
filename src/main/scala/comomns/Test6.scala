package comomns

import java.net.InetSocketAddress

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import java.util.concurrent.atomic.AtomicReference

import akka.NotUsed
import akka.io.IO
import akka.io.Tcp.{Bind, Connect}
import akka.stream._
import akka.stream.scaladsl.Tcp._
import akka.stream.scaladsl._
import akka.stream.stage._
import akka.testkit.AkkaSpec
import akka.testkit.TestProbe
import akka.util.ByteString

import scala.concurrent.{ExecutionContext, Future, Promise}
import akka.testkit.SocketUtil

import scala.util.Success

object Test6 extends  App {
  implicit val materializer = ActorMaterializer()
  implicit def system: ActorSystem = ActorSystem("test")
  implicit def ec: ExecutionContext = system.dispatcher

  import akka.stream.scaladsl.Framing

  val connections: Source[IncomingConnection, Future[ServerBinding]] = Tcp().bind("127.0.0.1", 8888)

  val binding = connections.to(Sink.foreach { connection ⇒
      val commandParser = Flow[String].takeWhile(_ != "BYE").map(_ + "!")
      import connection._
      val welcomeMsg = s"Welcome to: $localAddress, you are: $remoteAddress!"
      val welcome: Source[String, NotUsed] = Source.single(welcomeMsg)

      val serverLogic = Flow[ByteString]
        .via(Framing.delimiter(
          ByteString("\n"),
          maximumFrameLength = 256,
          allowTruncation = true))
        .map(_.utf8String)
        .via(commandParser)
        // merge in the initial banner after parser
       // .merge(welcome)
        .map(_ + "\n")
        .map(ByteString(_))

      connection.handleWith(serverLogic)
    }).run()

  val input = new AtomicReference("Hello world" ::"What a lovely day" :: Nil)
  def readLine(prompt: String): String = {
    input.get() match {
      case all @ cmd :: tail if input.compareAndSet(all, tail) ⇒ cmd
      case _ ⇒ "q"
    }
  }

    val connection = Tcp().outgoingConnection("127.0.0.1", 8888)
    val replParser =
      Flow[String].takeWhile(_ != "q")
       .concat(Source.single("BYE"))
        .map(elem ⇒ ByteString(s"$elem\n"))
  val welcomeMsg = s"SS"
  val welcome: Source[String, NotUsed] = Source.single(welcomeMsg)
    val repl = Flow[ByteString]
      .via(Framing.delimiter(
        ByteString("\n"),
        maximumFrameLength = 256,
        allowTruncation = true))
      .map(_.utf8String)
      .map(text ⇒ println("Server: " + text))
      .map(_ ⇒ readLine("> "))
      .merge(welcome)
      .via(replParser)
  Tcp().outgoingConnection("localhost", 8080)

  val connected: Future[OutgoingConnection] = connection.join(repl).run()

  /*val f: Flow[Int, Int, Future[Int]] = Flow.fromGraph(new FirstValue[Int])
  class FirstValue[A] extends GraphStageWithMaterializedValue[FlowShape[A, A], Future[A]] {
    val in = Inlet[A]("FirstValue.in")
    val out = Outlet[A]("FirstValue.out")
    val shape = FlowShape.of(in, out)
    override def createLogicAndMaterializedValue(inheritedAttributes: Attributes): (GraphStageLogic, Future[A]) = {
      val promise = Promise[A]()
      val logic: GraphStageLogic = new GraphStageLogic(shape) {
        setHandler(in, new InHandler {
          override def onPush(): Unit = {
            val elem = grab(in)
            promise.success(elem)
            push(out, elem)
            // replace handler with one that only forwards elements
            setHandler(in, new InHandler {
              override def onPush(): Unit = {
                push(out, grab(in))
              }
            })
          }
        })
        setHandler(out, new OutHandler {
          override def onPull(): Unit = {
            pull(in)
          }
        })
      }
      (logic, promise.future)
    }
  }

  val ss: Source[Int, Promise[Int]] = Source.fromGraph(new NumbersSource)

  class NumbersSource extends GraphStageWithMaterializedValue[SourceShape[Int], Promise[Int]] {
    val out: Outlet[Int] = Outlet("NumbersSource")
    override val shape: SourceShape[Int] = SourceShape(out)

    override def createLogicAndMaterializedValue(inheritedAttributes: Attributes): (GraphStageLogic, Promise[Int]) = {
      val promise = Promise[Int]()
      val logic = new GraphStageLogic(shape) {
        setHandler(out, new OutHandler {
          override def onPull(): Unit = {
            promise.future.onComplete {
              case Success(x) => push(out, x)
            }
          }
        })
      }
      (logic, promise)
    }
  }*/
}
