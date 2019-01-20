package comomns

import akka.NotUsed
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.stream._
import akka.stream.scaladsl._
import akka.stream.testkit.TestSubscriber
import akka.testkit._
import comomns.TwitterStreamQuickstartDocSpec.Author

import scala.collection.immutable
import scala.concurrent._
import scala.concurrent.duration._
import scala.util.Failure
import akka.stream.ThrottleMode

object Test9 extends  App {
  implicit val materializer = ActorMaterializer()
  implicit def system: ActorSystem = ActorSystem("test")
  implicit def ec: ExecutionContext = system.dispatcher

  // Mock akka-http interfaces
  /*object Http {
    def apply() = this
    def singleRequest(req: HttpRequest) = Future.successful((req.uri))
  }
  case class HttpRequest(uri: String)
  case class Unmarshal(b: Any) {
    def to[T]: Future[T] = Promise[T]().future
  }
  case class ServerSentEvent()

  def doSomethingElse(): Unit = ()

  //#restart-with-backoff-source
  val restartSource: Source[ServerSentEvent, NotUsed] = RestartSource.withBackoff(
    minBackoff = 3.seconds,
    maxBackoff = 30.seconds,
    randomFactor = 0.2 // adds 20% "noise" to vary the intervals slightly
   // maxRestarts = 20 // limits the amount of restarts to 20
  ) { () ⇒
    // Create a source from a future of a source
    Source.fromFutureSource {
      // Make a single request with akka-http
      Http().singleRequest(HttpRequest(
        uri = "http://example.com/eventstream"
      ))
        // Unmarshall it as a source of server sent events
        .flatMap(Unmarshal(_).to[Source[ServerSentEvent, NotUsed]])
    }
  }
  //#restart-with-backoff-source

  //#with-kill-switch
  val killSwitch = restartSource
    .viaMat(KillSwitches.single)(Keep.right)
    .toMat(Sink.foreach(event ⇒ println(s"Got event: $event")))(Keep.left)
    .run()

  doSomethingElse()

  killSwitch.shutdown()
  //#with-kill-switch

 val a: Sink[String, NotUsed] =  RestartSink.withBackoff[String](
    minBackoff = 3.seconds,
    maxBackoff = 30.seconds,
    randomFactor = 0.2
  ){ () ⇒ Sink.foreach(println) }

  import TwitterStreamQuickstartDocSpec._

  class AddressSystem2 {
    //#email-address-lookup2
    def lookupEmail(handle: String): Future[String] =
    //#email-address-lookup2
      Future.successful(handle + "@somewhere.com")
  }

  val addressSystem = new AddressSystem2
  val authors: Source[Author, NotUsed] =
    tweets.filter(_.hashtags.contains(akkaTag)).map(_.author)

  //#email-addresses-mapAsync-supervision
  import akka.stream.ActorAttributes.supervisionStrategy
  import akka.stream.Supervision.resumingDecider

  val emailAddresses: Source[String, NotUsed] =
    authors.via(
      Flow[Author].mapAsync(4)(author ⇒ addressSystem.lookupEmail(author.handle))
        .withAttributes(supervisionStrategy(resumingDecider)))*/

  // format: OFF
  //#unique-shutdown
  /*val countingSrc = Source(Stream.from(1)).delay(1.second, DelayOverflowStrategy.backpressure)
  val lastSnk = Sink.last[Int]

  val (killSwitch, last) = countingSrc
    .viaMat(KillSwitches.single)(Keep.right)
      .map(x => {println(x); x})
    .toMat(lastSnk)(Keep.both)
    .run()

  Thread.sleep(4000)
  //killSwitch.shutdown()
  val error = new RuntimeException("boom!")
  killSwitch.abort(error)
  last.onComplete {
    case Failure(x) => println(x)
  }*/

  //#pub-sub-1
  // Obtain a Sink and Source which will publish and receive from the "bus" respectively.
  /*val (sink, source) =
  MergeHub.source[String](perProducerBufferSize = 16)
    .toMat(BroadcastHub.sink(bufferSize = 256))(Keep.both)
    .run()
  //#pub-sub-1

  //#pub-sub-2
  // Ensure that the Broadcast output is dropped if there are no listening parties.
  // If this dropping Sink is not attached, then the broadcast hub will not drop any
  // elements itself when there are no subscribers, backpressuring the producer instead.
  source.runWith(Sink.ignore)
  //#pub-sub-2

  //#pub-sub-3
  // We create now a Flow that represents a publish-subscribe channel using the above
  // started stream as its "topic". We add two more features, external cancellation of
  // the registration and automatic cleanup for very slow subscribers.
  val busFlow: Flow[String, String, UniqueKillSwitch] =
  Flow.fromSinkAndSource(sink, source)
    .joinMat(KillSwitches.singleBidi[String, String])(Keep.right)
    .backpressureTimeout(3.seconds)
  //#pub-sub-3

  //#pub-sub-4
  val switch: UniqueKillSwitch =
    Source.repeat("Hello world!")
      .viaMat(busFlow)(Keep.right)
      .to(Sink.foreach(println))
      .run()

  Thread.sleep(1000)
  // Shut down externally
  switch.shutdown()
  //#pub-sub-4
*/

  //#partition-hub-stateful
  // A simple producer that publishes a new "message-" every second
  /*val producer = Source.tick(1.second, 1.second, "message")
    .zipWith(Source(1 to 100))((a, b) ⇒ s"$a-$b")

  // New instance of the partitioner function and its state is created
  // for each materialization of the PartitionHub.
  def roundRobin(): (PartitionHub.ConsumerInfo, String) ⇒ Long = {
    var i = -1L

    (info, elem) ⇒ {
      i += 1
      info.consumerIdByIdx((i % info.size).toInt)
    }
  }

  // Attach a PartitionHub Sink to the producer. This will materialize to a
  // corresponding Source.
  // (We need to use toMat and Keep.right since by default the materialized
  // value to the left is used)
  val runnableGraph: RunnableGraph[Source[String, NotUsed]] =
  producer.toMat(PartitionHub.statefulSink(
    () ⇒ roundRobin(),
    startAfterNrOfConsumers = 2, bufferSize = 256))(Keep.right)

  // By running/materializing the producer, we get back a Source, which
  // gives us access to the elements published by the producer.
  val fromProducer: Source[String, NotUsed] = runnableGraph.run()

  // Print out messages from the producer in two independent consumers
  fromProducer.runForeach(msg ⇒ println("consumer1: " + msg))
  fromProducer.runForeach(msg ⇒ println("consumer2: " + msg))
  //#partition-hub-stateful*/

  //#partition-hub
  // A simple producer that publishes a new "message-" every second
  /*val producer = Source.tick(1.second, 1.second, "message")
    .zipWith(Source(1 to 100))((a, b) ⇒ s"$a-$b")

  // Attach a PartitionHub Sink to the producer. This will materialize to a
  // corresponding Source.
  // (We need to use toMat and Keep.right since by default the materialized
  // value to the left is used)
  val runnableGraph: RunnableGraph[Source[String, NotUsed]] =
  producer.toMat(PartitionHub.sink(
    (size, elem) ⇒ math.abs(elem.hashCode % size),
    startAfterNrOfConsumers = 2, bufferSize = 256))(Keep.right)

  // By running/materializing the producer, we get back a Source, which
  // gives us access to the elements published by the producer.
  val fromProducer: Source[String, NotUsed] = runnableGraph.run()

  // Print out messages from the producer in two independent consumers
  fromProducer.runForeach(msg ⇒ println("consumer1: " + msg))
  fromProducer.runForeach(msg ⇒ println("consumer2: " + msg))
  //#partition-hub*/

  //#partition-hub-fastest
  val producer = Source(0 until 100)

  // ConsumerInfo.queueSize is the approximate number of buffered elements for a consumer.
  // Note that this is a moving target since the elements are consumed concurrently.
  val runnableGraph: RunnableGraph[Source[Int, NotUsed]] =
  producer.toMat(PartitionHub.statefulSink(
    () ⇒ (info, elem) ⇒ info.consumerIds.minBy(id ⇒ info.queueSize(id)),
    startAfterNrOfConsumers = 1, bufferSize = 16))(Keep.right)

  val fromProducer: Source[Int, NotUsed] = runnableGraph.run()

  //fromProducer.runForeach(msg ⇒ println("consumer1: " + msg))
  fromProducer.throttle(20, 100.millis,5,ThrottleMode.enforcing)
    .runForeach(msg ⇒ println("consumer2: " + msg))
  //#partition-hub-fastest

}
