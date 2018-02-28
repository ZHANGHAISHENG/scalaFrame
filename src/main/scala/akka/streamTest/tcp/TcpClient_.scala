package akka.streamTest.tcp

import java.net.InetSocketAddress
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.io.Tcp._
import akka.io.{IO, Tcp}
import akka.stream.ActorMaterializer
import akka.util.ByteString

/**
  * Akka Streams: Using TCP
  * 参考：
  * http://www.moye.me/2016/09/15/akka-streams_using-tcp/
  */
object TcpClient_ extends App {
  // 调用部分
  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()

  class Client(remote: InetSocketAddress, handler: ActorRef) extends Actor {

    override def preStart(): Unit = {
      IO(Tcp) ! Connect(remote, options = Vector(SO.TcpNoDelay(false)))
    }

    def receive: Receive = {
      case CommandFailed(_: Connect) =>
        handler ! "connect failed"
        context stop self

      case c@Connected(remote, local) =>
        handler ! c
        val connection = sender()
        connection ! Register(self)
        context become {
          case data: ByteString =>
            connection ! Write(data)
          case CommandFailed(w: Write) =>
            handler ! "write failed"
          case Received(data) =>
            handler ! data
          case "close" =>
            connection ! Close
          case _: ConnectionClosed =>
            handler ! "connection closed"
            context stop self
        }
    }
  }

  class ClientHandler extends Actor {
    def receive: Receive = {
      case c@Connected(remote, local) =>
        println(s"Connnected -> remote: $remote, local: $local")
        sender() ! ByteString("hello")

      case b: ByteString => println(b.utf8String)

      case f@("connect failed" | "connection closed" | "write failed") =>
        println(f)
    }
  }

  object Client {
    def generate = system.actorOf(Props(classOf[Client],
      serverAddress,
      system.actorOf(Props[ClientHandler])))
  }

  val serverAddress = new InetSocketAddress("localhost", 8888)
  val client = Client.generate
  Thread.sleep(1000)
  val client2 = Client.generate
}