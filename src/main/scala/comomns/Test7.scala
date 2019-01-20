package comomns

import java.util.UUID
import java.util.concurrent.atomic.{AtomicBoolean, AtomicInteger, AtomicReference}

import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.event.Logging
import akka.stream.{ActorMaterializer, Attributes, FlowShape}
import akka.stream.scaladsl.Tcp._
import akka.stream.scaladsl._
import akka.stream.testkit.TestSubscriber
import akka.stream.testkit.scaladsl.TestSink
import akka.testkit.EventFilter
import akka.util.ByteString

import scala.concurrent.duration._
import scala.collection.immutable
import scala.concurrent.duration.Duration
import scala.concurrent._

object Test7 extends  App {
  implicit val materializer = ActorMaterializer()
  implicit def system: ActorSystem = ActorSystem("test")
  implicit def ec: ExecutionContext = system.dispatcher

  /*val mySource = Source(List("1", "2", "3"))
  def analyse(s: String) = s

  //#log-custom
  // customise log levels
  mySource.log("before-map")
    .withAttributes(
      Attributes.logLevels(
        onElement = Logging.WarningLevel,
        onFinish = Logging.InfoLevel,
        onFailure = Logging.DebugLevel
      )
    )
    .map(analyse)

  // or provide custom logging adapter
  implicit val adapter = Logging(system, "customLogger")
  mySource.log("custom")
  //#log-custom

  val loggedSource = mySource.log("custom")
  EventFilter.debug(start = "[custom] Element: ").intercept {
    loggedSource.runWith(Sink.ignore)
  }

  def builderFunction(): String = UUID.randomUUID.toString

  //#source-from-function
  val source = Source.repeat(1).map(_ ⇒ builderFunction())
  //#source-from-function

  val f: Future[immutable.Seq[String]] = source.take(2).runWith(Sink.seq)
  f.map(println(_))

  Await.result(f, Duration.Inf )

  val someDataSource = Source(List(List("1"), List("2"), List("3", "4", "5"), List("6", "7")))

  //#flattening-seqs
  val myData: Source[List[String], NotUsed] = someDataSource
  val flattened: Source[String, NotUsed] = myData.mapConcat(identity)
  //#flattening-seqs

  Await.result(flattened.limit(8).runWith(Sink.seq), 3.seconds) should be(List("1", "2", "3", "4", "5", "6", "7"))
*/
  type Message = String
  case class Topic(name: String)
  val elems = Source(List("1: a", "1: b", "all: c", "all: d", "1: e"))
  val extractTopics: Message => List[Topic] = { msg: Message ⇒
    if (msg.startsWith("1")) List(Topic("1"))
    else List(Topic("1"), Topic("2"))
  }
  val topicMapper: (Message) ⇒ immutable.Seq[Topic] = extractTopics
  val messageAndTopic: Source[(Message, Topic), NotUsed] = elems.mapConcat { msg: Message ⇒
    val topicsForMessage = topicMapper(msg)
    topicsForMessage.map(msg -> _)
  }
  val multiGroups = messageAndTopic
    .groupBy(2, _._2).map {
    case (msg, topic) ⇒
      (msg, topic)
  }
  val result = multiGroups
    .grouped(10)
    .mergeSubstreams
    .map(g ⇒ g.head._2.name + g.map(_._1).mkString("[", ", ", "]"))
    .limit(10)
    .runWith(Sink.foreach(println))


  def adhocSource[T](source: Source[T, _], timeout: FiniteDuration, maxRetries: Int): Source[T, _] =
    Source.lazily(
      () ⇒ source.backpressureTimeout(timeout).recoverWithRetries(maxRetries, {
        case t: TimeoutException ⇒
          Source.lazily(() ⇒ source.backpressureTimeout(timeout)).mapMaterializedValue(_ ⇒ NotUsed)
      })
    )

  val sink: TestSubscriber.Probe[String] = adhocSource(Source.repeat("a"), 200.milliseconds, 3)
    .runWith(TestSink.probe[String])
  sink.requestNext("a")

  val shutdown = Promise[Done]()
  val startedCount = new AtomicInteger(0)

  val source: Source[String, Int] = Source
    .empty.mapMaterializedValue(_ ⇒ startedCount.incrementAndGet())
    .concat(Source.repeat("a"))

  def words = Source(List("hello", "world", "and", "hello", "universe", "akka") ++ List.fill(1000)("rocks!"))

  //#word-count
  val counts: RunnableGraph[NotUsed] = words
    // split the words into separate streams first
    .groupBy(10000, identity)
    //transform each element to pair with number of words in it
    .map(_ -> 1)
    // add counting logic to the streams
    .reduce((l, r) ⇒ (l._1, l._2 + r._2))
      .to(Sink.seq)
    // get a stream of word counts
    //.mergeSubstreams

  val myJobs = Source(List("1", "2", "3", "4", "5"))
  type Result = String

  val worker = Flow[String].map(_ + " done")

  //#worker-pool
  def balancer[In, Out](worker: Flow[In, Out, Any], workerCount: Int): Flow[In, Out, NotUsed] = {
    import GraphDSL.Implicits._
    Flow.fromGraph(GraphDSL.create() { implicit b ⇒
      val balancer = b.add(Balance[In](workerCount, waitForAllDownstreams = true))
      val merge = b.add(Merge[Out](workerCount))
      for (_ ← 1 to workerCount) {
        balancer ~> worker.async ~> merge
      }
      FlowShape(balancer.in, merge.out)
    })
  }
  val processedJobs: Source[Result, NotUsed] = myJobs.via(balancer(worker, 3))
  val droppyStream: Flow[Message, Message, NotUsed] =
    Flow[Message].conflate((lastMessage, newMessage) ⇒ newMessage)
}
