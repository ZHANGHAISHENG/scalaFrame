package comomns.actor

import akka.actor.{Actor, ActorIdentity, ActorRef, ActorSelection, ActorSystem, Identify, Props, Terminated}
import akka.event.Logging
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.collection.mutable

object A1 extends App {


  /*case class Message(msg: String)

  class EchoActor extends Actor {
    def receive = {
      case msg ⇒ sender() ! msg
    }
  }

  class CleanUpActor extends Actor {
    def receive = {
      case set: mutable.Set[_] ⇒ set.clear()
    }
  }

  class MyActor(echoActor: ActorRef, cleanUpActor: ActorRef) extends Actor {
    var state = ""
    val mySet = mutable.Set[String]()

    def expensiveCalculation(actorRef: ActorRef): String = {
      // this is a very costly operation
      "Meaning of life is 42"
    }

    def expensiveCalculation(): String = {
      // this is a very costly operation
      "Meaning of life is 42"
    }

    def receive = {
      case _ ⇒
        implicit val ec = context.dispatcher
        implicit val timeout = Timeout(5 seconds) // needed for `?` below

        // Example of incorrect approach
        // Very bad: shared mutable state will cause your
        // application to break in weird ways
        /*Future { state = "This will race" }
        (echoActor ? Message("With this other one")).mapTo[Message]
          .foreach { received ⇒ state = received.msg }*/

        // Very bad: shared mutable object allows
        // the other actor to mutate your own state,
        // or worse, you might get weird race conditions
        mySet.add("e1")
        Thread.sleep(1000)
        cleanUpActor ! mySet
        Thread.sleep(1000)
        println(mySet)

        // Very bad: "sender" changes for every message,
        // shared mutable state bug
        Future { expensiveCalculation(sender()) }

        // Example of correct approach
        // Completely safe: "self" is OK to close over
        // and it's an ActorRef, which is thread-safe
        Future { expensiveCalculation() } foreach { self ! _ }

        // Completely safe: we close over a fixed value
        // and it's an ActorRef, which is thread-safe
        val currentSender = sender()
        Future { expensiveCalculation(currentSender) }
    }
  }
  val system = ActorSystem("test-system")
  val a1 = system.actorOf(Props(new EchoActor), "echo")
  val a2 = system.actorOf(Props(new CleanUpActor), "clean")
  val a3 = system.actorOf(Props(new MyActor(a1, a2)), "myActor")

  a3 ! ""
*/

  case object Swap
  class Swapper extends Actor {
    import context._
    context.actorSelection("/user/another")
    ActorIdentity
    def receive = {
      case Swap ⇒
        println("Hi")
        become({
          case Swap ⇒
            println("Ho")
            unbecome() // resets the latest 'become' (just for fun)
        }, discardOld = false) // push on top instead of replace
    }
  }

  val system = ActorSystem("SwapperSystem")
  val swap = system.actorOf(Props[Swapper], name = "swapper")
  swap ! Swap // logs Hi
  swap ! Swap // logs Ho
  swap ! Swap // logs Hi
  swap ! Swap // logs Ho
  swap ! Swap // logs Hi
  swap ! Swap // logs Ho


  class Follower extends Actor {
    val identifyId = 1
    context.actorSelection("/user/another") ! Identify(identifyId)

    def receive = {
      case ActorIdentity(`identifyId`, Some(ref)) ⇒
        context.watch(ref)
        context.become(active(ref))
      case ActorIdentity(`identifyId`, None) ⇒ context.stop(self)

    }

    def active(another: ActorRef): Actor.Receive = {
      case Terminated(`another`) ⇒ context.stop(self)
    }
  }

}
