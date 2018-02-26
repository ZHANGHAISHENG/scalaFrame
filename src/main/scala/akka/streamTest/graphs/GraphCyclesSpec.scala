package akka.streamTest.graphs

import akka.stream.scaladsl._
import akka.stream.{ActorMaterializer, ClosedShape, OverflowStrategy}
import akka.testkit.AkkaSpec

/**
  * back-pressure 特性
  * 参考：http://www.moye.me/2016/08/28/akka_streams-linear_pipelines/
  */
class GraphCyclesSpec extends AkkaSpec {

  implicit val materializer = ActorMaterializer()

  "Cycle demonstration" must {
    val source = Source.fromIterator(() ⇒ Iterator.from(0))

    "include a deadlocked cycle" in {

      // format: OFF
      //#deadlocked
      // WARNING! The graph below deadlocks!
      RunnableGraph.fromGraph(GraphDSL.create() { implicit b =>
        import GraphDSL.Implicits._

        val merge = b.add(Merge[Int](2))
        val bcast = b.add(Broadcast[Int](2))

        source ~> merge ~> Flow[Int].map { s => println(s); s } ~> bcast ~> Sink.ignore
                  merge                    <~                      bcast
        ClosedShape
      }).run()
      //#deadlocked
      // format: ON
    }
    /**
      * 输出：
      * 0
      */

    "include an unfair cycle" in {
      // format: OFF
      //#unfair
      // WARNING! The graph below stops consuming from "source" after a few steps
      RunnableGraph.fromGraph(GraphDSL.create() { implicit b =>
        import GraphDSL.Implicits._

        val merge = b.add(MergePreferred[Int](1))
        val bcast = b.add(Broadcast[Int](2))

        source ~> merge ~> Flow[Int].map { s => println(s); s } ~> bcast ~> Sink.ignore
                  merge.preferred              <~                  bcast
        ClosedShape
      }).run()
      //#unfair
      // format: ON
    }

    /**
      *
      * 输出：一直循环输出前面几个数字
      * 0
        1
        1
        0
        ...
      */

    "include a dropping cycle" in {
      // format: OFF
      //#dropping
      RunnableGraph.fromGraph(GraphDSL.create() { implicit b =>
        import GraphDSL.Implicits._

        val merge = b.add(Merge[Int](2))
        val bcast = b.add(Broadcast[Int](2))

        source ~> merge ~> Flow[Int].map { s => println(s); s } ~> bcast ~> Sink.ignore
        merge <~ Flow[Int].buffer(10, OverflowStrategy.dropHead) <~ bcast
        ClosedShape
      }).run()
      //#dropping
      // format: ON
    }

    /**
      * 不平衡输出：
      * 18
        3
        19
        0
        20
        4
        21
        2
        22
        5
        23
        1
        24
      */


    "include a dead zipping cycle" in {
      // format: OFF
      //#zipping-dead
      // WARNING! The graph below never processes any elements
      RunnableGraph.fromGraph(GraphDSL.create() { implicit b =>
        import GraphDSL.Implicits._

        val zip = b.add(ZipWith[Int, Int, Int]((left, right) => right))
        val bcast = b.add(Broadcast[Int](2))

        source ~> zip.in0
        zip.out.map { s => println(s); s } ~> bcast ~> Sink.ignore
        zip.in1             <~                bcast
        ClosedShape
      }).run() // 死锁
      //#zipping-dead
      // format: ON
    }

    "include a live zipping cycle" in {
      // format: OFF
      //#zipping-live
      RunnableGraph.fromGraph(GraphDSL.create() { implicit b =>
        import GraphDSL.Implicits._

        val zip = b.add(ZipWith((left: Int, right: Int) => left))
        val bcast = b.add(Broadcast[Int](2))
        val concat = b.add(Concat[Int]())
        val start = Source.single(0)

        source ~> zip.in0
        zip.out.map { s => println(s); s } ~> bcast ~> Sink.ignore
        zip.in1 <~ concat         <~          start
                   concat         <~          bcast
        ClosedShape
      }).run()
      //#zipping-live
      // format: ON
    }

    /**
      * 平衡输出:
      * 0
        1
        2
        3
        4
      */

  }

}
