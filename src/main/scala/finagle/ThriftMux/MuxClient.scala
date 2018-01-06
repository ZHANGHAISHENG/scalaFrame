package finagle.ThriftMux

import com.twitter.finagle.ThriftMux
import com.twitter.util.Future
import in.xnnyygn.dictservice.DictService
import com.twitter.conversions.time._

object MuxClient {

  def main(args: Array[String]): Unit = {
    val host = "127.0.0.1"
    val port = "8080"
    val client: DictService.FutureIface = ThriftMux.client
      .withRequestTimeout(5.seconds)
      //    .withRetryBudget(RetryBudget.Infinite)
      //    .withRetryBackoff(Backoff.exponentialJittered(100.millis, 10.seconds))
      .withSession.acquisitionTimeout(5.seconds)
      .withSession.maxLifeTime(10.minutes)  //会话被视为活动的最大持续时间
      .withSession.maxIdleTime(1.minute)    //会话允许空闲的最大持续时间（不发送任何请求）
      //    .withSessionPool.minSize(5)
      //    .withSessionPool.maxSize(10)
      //    .withSessionPool.maxWaiters(20)
      .newIface[DictService.FutureIface](s"$host:$port")


      //put
      val f1: Future[Unit] = client.put("a","100$")
      f1.onSuccess { c =>
        println("put success.")
      }
      Thread.sleep(200)

      //get
      val f2: Future[String] = client.get("a")
      f2.onSuccess { v =>
        println("get success："+v)
      }
      Thread.sleep(200)
  }

}
