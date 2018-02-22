package akka.streamTest.pipelines

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}

import scala.util.Random

/**
  * Akka Streams: pipelines
  * 参考：
  * http://www.moye.me/2016/08/28/akka_streams-linear_pipelines/
  */
object InputCustomer {
  def random(): InputCustomer = {
    InputCustomer(s"FirstName${Random.nextInt(1000)} " +
      s"LastName${Random.nextInt(1000)}")
  }
}

case class InputCustomer(name: String)

case class OutputCustomer(firstName: String, lastName: String)

object CustomersExample extends App {
  implicit val actorSystem = ActorSystem()

  import actorSystem.dispatcher

  implicit val materializer = ActorMaterializer()

  val inputCustomers = Source((1 to 100).map(_ => InputCustomer.random()))

  val normalize = Flow[InputCustomer]
    .map(c => c.name.split(" ").toList)
    .collect {
      case firstName :: lastName :: Nil => OutputCustomer(firstName, lastName)
    }

  val writeCustomers = Sink.foreach { println} // η-conversion

  inputCustomers.via(normalize).runWith(writeCustomers).andThen {
    case _ => actorSystem.terminate()
  }
}