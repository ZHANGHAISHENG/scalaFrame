package comomns

import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem}
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import akka.stream.testkit._
import akka.stream.{ActorMaterializer, OverflowStrategy}
import org.reactivestreams.Processor

import scala.concurrent.Future

object Test5 extends  App {
  implicit val materializer = ActorMaterializer()
  implicit def system: ActorSystem = ActorSystem("test")


}
