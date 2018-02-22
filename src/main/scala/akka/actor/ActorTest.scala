package akka.actor

import akka.pattern._
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Random, Success}

case class User(id: Int,name: String)
object AsyncQueue {
  implicit def actorSystem: ActorSystem = ActorSystem("adx-edge-" + java.util.UUID.randomUUID().toString.toLowerCase)
  implicit def ec: ExecutionContext = actorSystem.dispatcher
  private [this] implicit val timeout: Timeout = Timeout(20.seconds) //  ? t 用到
  val parallel: Int = 20

  private[this] class Worker extends Actor {
    override def receive: Receive = {
      case t:User =>println("success");sender() ! t
      case _ =>println("fail");  sender() ! User(0,"ww")
    }
    
  }
  private[this] def makeWork(): ActorRef = actorSystem.actorOf(Props(classOf[Worker]))
  private[this] lazy val workers: Vector[ActorRef] = Vector.fill(parallel)(makeWork())

  def pushToActor1(t: User): Unit = workers(new Random().nextInt(parallel)) ! t
  def pushToActor2(t: User): Future[User] = (workers(new Random().nextInt(parallel)) ? t).mapTo[User]

  def main(args: Array[String]): Unit = {
    pushToActor1(User(1,"zhs"))
    /*val r: Future[User] = pushToActor2(User(1,"zhs"))
    r.onComplete{
      case Success(u) => println(u)
      case e => println(e)
    }*/
  }

}
