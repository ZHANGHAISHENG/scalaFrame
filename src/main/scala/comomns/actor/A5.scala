package comomns.actor

import akka.actor._
import akka.persistence._
import akka.{Done, NotUsed}
import akka.actor._
import akka.persistence.{PersistentActor, Recovery}
import akka.persistence.query._
import akka.persistence.query.journal.leveldb.scaladsl.LeveldbReadJournal
import akka.stream.{ActorMaterializer, FlowShape}
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.javadsl
import akka.testkit.AkkaSpec
import akka.util.Timeout
import org.reactivestreams.Subscriber

import scala.collection.immutable
import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._
import com.typesafe.config.Config

object A5 extends  App {
  val system = ActorSystem("example")
  implicit val mat = ActorMaterializer()(system)
  val queries = PersistenceQuery(system).readJournalFor[LeveldbReadJournal](
    LeveldbReadJournal.Identifier)

/*  val src: Source[EventEnvelope, NotUsed] =
    queries.eventsByPersistenceId("sample-id-3", 0L, Long.MaxValue)
  val events: Future[Done] = src.runWith(Sink.foreach(println))*/

  val src: Source[EventEnvelope, NotUsed] =
    queries.eventsByTag(tag = "green", offset = Sequence(0L))
  val events: Future[Done] = src.runWith(Sink.foreach(println))

  Thread.sleep(2000)
  system.terminate()
}
