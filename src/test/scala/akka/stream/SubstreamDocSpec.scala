package akka.stream

import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.testkit.AkkaSpec

class SubstreamDocSpec extends AkkaSpec {
  implicit val mat = ActorMaterializer()

  "generate substreams by groupBy" in {
    //#groupBy1
    val source = Source(1 to 10).groupBy(3, _ % 3)
    //#groupBy1

    //#groupBy2
    Source(1 to 10).groupBy(3, _ % 3).to(Sink.ignore).run()
    //#groupBy2

    //#groupBy3
    Source(1 to 10)
      .groupBy(3, _ % 3)
      .mergeSubstreams
      .runWith(Sink.ignore)
    //#groupBy3

    //#groupBy4
    Source(1 to 30)
      .groupBy(10, _ % 10)
      .mergeSubstreamsWithParallelism(5)
      .runWith(Sink.foreach(println))

    //concatSubstreams is equivalent to mergeSubstreamsWithParallelism(1)
    /*Source(1 to 10)
      .groupBy(3, _ % 3)
      .concatSubstreams
      .runWith(Sink.foreach(println))*/
    //#groupBy4
  }
  /**
    * group by 3 输出：
    * 2
      1
      4
      5
    group by 3 输出：
      2
     1
    */

  "generate substreams by splitWhen and splitAfter" in {
    //#splitWhenAfter
    Source(1 to 10).splitWhen(SubstreamCancelStrategy.drain)(_ == 3)

    Source(1 to 10).splitAfter(SubstreamCancelStrategy.drain)(_ == 3)
    //#splitWhenAfter

    //#wordCount
    val text =
      "This is the first line.\n" +
        "The second line.\n" +
        "There is also the 3rd line\n"

    val charCount = Source(text.toList)
      .splitAfter { _ == '\n' }
      .filter(_ != '\n')
      .map(_ ⇒ 1)
      .reduce(_ + _)
      .to(Sink.foreach(println))
      .run()
    Thread.sleep(1000)
    //#wordCount
  }

  "generate substreams by flatMapConcat and flatMapMerge" in {
    //#flatMapConcat
   /* Source(1 to 2)
      .flatMapConcat(i ⇒ Source(List.fill(3)(i)))
      .runWith(Sink.foreach(println))*/
    //#flatMapConcat

    //#flatMapMerge
    Source(1 to 2)
      .flatMapMerge(2, i ⇒ Source(List.fill(3)(i)))
      .runWith(Sink.foreach(println))
    //#flatMapMerge
  }
}