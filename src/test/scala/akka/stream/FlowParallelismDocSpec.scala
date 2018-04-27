package akka.stream

import akka.NotUsed
import akka.stream.scaladsl.{ GraphDSL, Merge, Balance, Flow }
import akka.testkit.AkkaSpec

class FlowParallelismDocSpec extends AkkaSpec {

  import GraphDSL.Implicits._
  case class ScoopOfBatter()
  case class HalfCookedPancake()
  case class Pancake()


  val fryingPan1: Flow[ScoopOfBatter, HalfCookedPancake, NotUsed] =
    Flow[ScoopOfBatter].map { batter => HalfCookedPancake() }

  val fryingPan2: Flow[HalfCookedPancake, Pancake, NotUsed] =
    Flow[HalfCookedPancake].map { halfCooked => Pancake() }

  "Demonstrate pipelining" in {
    val pancakeChef: Flow[ScoopOfBatter, Pancake, NotUsed] =
      Flow[ScoopOfBatter].via(fryingPan1.async).via(fryingPan2.async)
  }

  "Demonstrate parallel processing" in {
    val fryingPan: Flow[ScoopOfBatter, Pancake, NotUsed] =
      Flow[ScoopOfBatter].map { batter ⇒ Pancake() }

    val pancakeChef: Flow[ScoopOfBatter, Pancake, NotUsed] = Flow.fromGraph(GraphDSL.create() { implicit builder ⇒
      val dispatchBatter = builder.add(Balance[ScoopOfBatter](2))
      val mergePancakes = builder.add(Merge[Pancake](2))

      dispatchBatter.out(0) ~> fryingPan.async ~> mergePancakes.in(0)
      dispatchBatter.out(1) ~> fryingPan.async ~> mergePancakes.in(1)

      FlowShape(dispatchBatter.in, mergePancakes.out)
    })
  }

  "Demonstrate parallelized pipelines" in {
    val pancakeChef: Flow[ScoopOfBatter, Pancake, NotUsed] =
      Flow.fromGraph(GraphDSL.create() { implicit builder ⇒
        val dispatchBatter = builder.add(Balance[ScoopOfBatter](2))
        val mergePancakes = builder.add(Merge[Pancake](2))

        dispatchBatter.out(0) ~> fryingPan1.async ~> fryingPan2.async ~> mergePancakes.in(0)
        dispatchBatter.out(1) ~> fryingPan1.async ~> fryingPan2.async ~> mergePancakes.in(1)

        FlowShape(dispatchBatter.in, mergePancakes.out)
      })
  }

  "Demonstrate pipelined parallel processing" in {
    val pancakeChefs1: Flow[ScoopOfBatter, HalfCookedPancake, NotUsed] =
      Flow.fromGraph(GraphDSL.create() { implicit builder ⇒
        val dispatchBatter = builder.add(Balance[ScoopOfBatter](2))
        val mergeHalfPancakes = builder.add(Merge[HalfCookedPancake](2))

        dispatchBatter.out(0) ~> fryingPan1.async ~> mergeHalfPancakes.in(0)
        dispatchBatter.out(1) ~> fryingPan1.async ~> mergeHalfPancakes.in(1)

        FlowShape(dispatchBatter.in, mergeHalfPancakes.out)
      })

    val pancakeChefs2: Flow[HalfCookedPancake, Pancake, NotUsed] =
      Flow.fromGraph(GraphDSL.create() { implicit builder ⇒
        val dispatchHalfPancakes = builder.add(Balance[HalfCookedPancake](2))
        val mergePancakes = builder.add(Merge[Pancake](2))

        dispatchHalfPancakes.out(0) ~> fryingPan2.async ~> mergePancakes.in(0)
        dispatchHalfPancakes.out(1) ~> fryingPan2.async ~> mergePancakes.in(1)

        FlowShape(dispatchHalfPancakes.in, mergePancakes.out)
      })
    val kitchen: Flow[ScoopOfBatter, Pancake, NotUsed] = pancakeChefs1.via(pancakeChefs2)
  }

}
