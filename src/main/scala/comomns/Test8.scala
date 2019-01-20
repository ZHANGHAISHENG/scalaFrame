package comomns

import java.util.concurrent.atomic.AtomicInteger
import akka.actor.ActorSystem
import akka.stream.scaladsl._
import akka.stream.testkit.TestSubscriber
import akka.stream.testkit.scaladsl.TestSink
import akka.stream.{ActorMaterializer, FlowShape}
import akka.{Done, NotUsed}
import scala.concurrent._
import akka.NotUsed
import akka.actor.{ Props, ActorRef, Actor }
import akka.stream.ClosedShape
import akka.stream.scaladsl._
import akka.stream.testkit._
import akka.testkit._
import scala.collection.immutable
import scala.concurrent.duration._

object Test8 extends  App {
  implicit val materializer = ActorMaterializer()
  implicit def system: ActorSystem = ActorSystem("test")
  implicit def ec: ExecutionContext = system.dispatcher
  type Message = String
  //#global-limiter-actor
  object Limiter {
    case object WantToPass
    case object MayPass
    case object ReplenishTokens
    def props(maxAvailableTokens: Int, tokenRefreshPeriod: FiniteDuration,
              tokenRefreshAmount: Int): Props =
      Props(new Limiter(maxAvailableTokens, tokenRefreshPeriod, tokenRefreshAmount))
  }
  class Limiter(
                 val maxAvailableTokens: Int,
                 val tokenRefreshPeriod: FiniteDuration,
                 val tokenRefreshAmount: Int) extends Actor {
    import Limiter._
    import context.dispatcher
    import akka.actor.Status
    private var waitQueue = immutable.Queue.empty[ActorRef]
    private var permitTokens = maxAvailableTokens
    private val replenishTimer = system.scheduler.schedule(
      initialDelay = tokenRefreshPeriod,
      interval = tokenRefreshPeriod,
      receiver = self,
      ReplenishTokens)
    override def receive: Receive = open
    val open: Receive = {
      case ReplenishTokens ⇒
        println("open---------------ReplenishTokens")
        permitTokens = math.min(permitTokens + tokenRefreshAmount, maxAvailableTokens)
      case WantToPass ⇒
        println("open---------------WantToPass")
        permitTokens -= 1
        sender() ! MayPass
        if (permitTokens == 0) context.become(closed)
    }
    val closed: Receive = {
      case ReplenishTokens ⇒
        println("closed---------------ReplenishTokens")
        permitTokens = math.min(permitTokens + tokenRefreshAmount, maxAvailableTokens)
        releaseWaiting()
      case WantToPass ⇒
        println("closed---------------WantToPass, waitQueue size:" + waitQueue.size)
        waitQueue = waitQueue.enqueue(sender())
    }
    private def releaseWaiting(): Unit = {
      val (toBeReleased, remainingQueue) = waitQueue.splitAt(permitTokens)
      waitQueue = remainingQueue
      permitTokens -= toBeReleased.size
      toBeReleased foreach (_ ! MayPass)
      if (permitTokens > 0) context.become(open)
    }
    override def postStop(): Unit = {
      replenishTimer.cancel()
      waitQueue foreach (_ ! Status.Failure(new IllegalStateException("limiter stopped")))
    }
  }

  //#global-limiter-actor
  //#global-limiter-flow
  def limitGlobal[T](limiter: ActorRef, maxAllowedWait: FiniteDuration): Flow[T, T, NotUsed] = {
    import akka.pattern.ask
    import akka.util.Timeout
    Flow[T].mapAsync(10)((element: T) ⇒ {
      implicit val triggerTimeout = Timeout(maxAllowedWait)
      val limiterTriggerFuture = limiter ? Limiter.WantToPass
      limiterTriggerFuture.map((_) ⇒ element)
    })

  }

  val limiter = system.actorOf(Limiter.props(2, 100.days, 1), "limiter")
  val source1 = Source.fromIterator(() ⇒ Iterator.continually("E1")).via(limitGlobal(limiter, 2.seconds.dilated))
  val source2 = Source.fromIterator(() ⇒ Iterator.continually("E2")).via(limitGlobal(limiter, 2.seconds.dilated))
  val probe = TestSubscriber.manualProbe[String]()

  RunnableGraph.fromGraph(GraphDSL.create() { implicit b ⇒
    import GraphDSL.Implicits._
    val merge = b.add(Merge[String](2))
    source1 ~> merge ~> Sink.foreach(println)
    source2 ~> merge
    ClosedShape
  }).run()

  Thread.sleep(500)
  limiter ! Limiter.ReplenishTokens
  /*for (_ ← 1 to 10) {
    limiter ! Limiter.ReplenishTokens
  }*/

}
