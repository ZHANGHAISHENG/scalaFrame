package comomns

import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl.{Flow, Keep, RunnableGraph, Sink, Source}
import akka.stream.testkit._
import org.reactivestreams.Processor
import akka.testkit.AkkaSpec

import scala.concurrent.{Future, Promise}

object Test4 extends  App {
  import TwitterStreamQuickstartDocSpec._

  implicit val materializer = ActorMaterializer()
  implicit def system: ActorSystem = ActorSystem("test")

  //#imports
  import org.reactivestreams.Publisher
  import org.reactivestreams.Subscriber
  //#imports


  def assertResult(storage: TestSubscriber.ManualProbe[Author]): Unit = {
    val sub = storage.expectSubscription()
    sub.request(10)
    storage.expectNext(Author("rolandkuhn"))
    storage.expectNext(Author("patriknw"))
    storage.expectNext(Author("bantonsson"))
    storage.expectNext(Author("drewhk"))
    storage.expectNext(Author("ktosopl"))
    storage.expectNext(Author("mmartynas"))
    storage.expectNext(Author("akkateam"))
    storage.expectComplete()
  }

  val tweets: Publisher[Tweet] = TwitterStreamQuickstartDocSpec.tweets.runWith(Sink.asPublisher(fanout = false))
  val storage = TestSubscriber.manualProbe[Author]
  val authors = Flow[Tweet]
    .filter(_.hashtags.contains(akkaTag))
    .map(_.author)

  val a: NotUsed = Source.fromPublisher(tweets).via(authors).to(Sink.fromSubscriber(storage)).run()
  assertResult(storage)

  //val s: RunnableGraph[Promise[Option[Int]]] = Source.maybe[Int].toMat(Sink.head)(Keep.left)

  //#flow-publisher-subscriber
  val processor: Processor[Tweet, Author] = authors.toProcessor.run()
  tweets.subscribe(processor)
  processor.subscribe(storage)
  //Source.fromPublisher()
  assertResult(storage)
  val tt1: ActorRef = Source.actorRef[String](100,OverflowStrategy.dropHead).map(x => x).to(Sink.head).run()
  val tt3: NotUsed = Source(1 to 10).to(Sink.head).run()
  val tt4: Future[Int] = Source(1 to 10).runWith(Sink.head)
  val tt2: (ActorRef, Future[String]) = Source.actorRef[String](100,OverflowStrategy.dropHead).map(x => x).toMat(Sink.head)(Keep.both).run()
  val authorPublisher: Publisher[Author] =
    Source.fromPublisher(tweets).via(authors).runWith(Sink.asPublisher(fanout = false))
  authorPublisher.subscribe(storage)

  val tweetSubscriber: Subscriber[Tweet] =
    authors.to(Sink.fromSubscriber(storage)).runWith(Source.asSubscriber[Tweet])
  tweets.subscribe(tweetSubscriber)

}
