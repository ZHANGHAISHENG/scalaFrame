package aerospike

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import com.aerospike.client.Bin
import scala.concurrent.duration.{DurationLong}
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.FiniteDuration

object AsClientTest {

  implicit def system: ActorSystem = ActorSystem("test-" + java.util.UUID.randomUUID().toString.toLowerCase)
  implicit def mt: Materializer = ActorMaterializer()
  implicit def ec: ExecutionContext = system.dispatcher

  private[this] val c = system.settings.config.getConfig("aerospike.test-main")
  val bin: String = c.getString("bin")
  val set: String = c.getString("set1")
  val asClient: SimpleAsyncAsClient = SimpleAsyncAsClient(AsConfig("aerospike.cluster.main"))
  val expire: FiniteDuration = c.getDuration("expire").toMillis.milliseconds
  val namespace: String = c.getString("namespace")

  def save(key: String,value: String) = {
    asClient.put(namespace, set, key, Seq(new Bin(bin, value)), expire)
  }

  def get(key: String)(implicit ec: ExecutionContext): Future[Option[String]] = {
    asClient.get(namespace, set, key)
      .map {
        _.flatMap { record =>
          Option(record.getString(bin)) flatMap { x =>
            Some(x)
          }
        }
      }
  }

  def main(args: Array[String]): Unit = {
    //保存
    save("k1", "v1")
    //获取
    val r: Future[Option[String]] = get("k1")
    Thread.sleep(1000)
    println(r)
  }

}
