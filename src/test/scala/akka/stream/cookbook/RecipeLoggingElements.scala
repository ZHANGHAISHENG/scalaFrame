package akka.stream.cookbook
import java.util.UUID

import akka.NotUsed
import akka.event.Logging
import akka.stream.Attributes
import akka.stream.scaladsl.{Sink, Source}

import scala.concurrent.duration._
import docs.RecipeSpec

import scala.concurrent.{Await, Future}

class RecipeLoggingElements extends RecipeSpec {
  "use log()" in {
    val mySource = Source(List("1", "2", "3"))
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
      ).map(analyse)

    // or provide custom logging adapter
    implicit val adapter = Logging(system, "customLogger")
    mySource.log("custom")
    //#log-custom

    val loggedSource = mySource.log("custom")
    //EventFilter.debug(start = "[custom] Element: ").intercept {
      loggedSource.runWith(Sink.foreach(println))
   // }
  }

  "Source.repeat" in {
      def builderFunction(): String = UUID.randomUUID.toString
      val source = Source.repeat(NotUsed).map(_ ⇒ builderFunction())
      val f = source.take(2).runWith(Sink.seq)
      f.futureValue.distinct.size should ===(2)
    }

  "mapConcat" in {

    val someDataSource = Source(List(List("1"), List("2"), List("3", "4", "5"), List("6", "7")))
    val myData: Source[List[Message], NotUsed] = someDataSource
    val flattened: Source[Message, NotUsed] = myData.mapConcat(identity)
    Await.result(flattened.limit(8).runWith(Sink.seq), 3.seconds) should be(List("1", "2", "3", "4", "5", "6", "7"))
  }

  "limit , take" in {
    val mySource = Source(1 to 3).map(_.toString)
    val MAX_ALLOWED_SIZE = 100
    val limited: Future[Seq[String]] = mySource.limit(MAX_ALLOWED_SIZE).runWith(Sink.seq)
    //OK. Collect up until max-th elements only, then cancel upstream
    val ignoreOverflow: Future[Seq[String]] = mySource.take(MAX_ALLOWED_SIZE).runWith(Sink.seq)
    limited.futureValue should === (Seq("1", "2", "3"))
    ignoreOverflow.futureValue should === (Seq("1", "2", "3"))
  }



}
