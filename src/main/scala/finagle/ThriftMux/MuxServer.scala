package finagle.ThriftMux

import com.twitter.finagle.{Failure, ThriftMux}
import com.twitter.finagle.stats.NullStatsReceiver
import com.twitter.finagle.tracing.NullTracer
import finagle.finagle_thrift.simple.DictServiceImpl
import com.twitter.conversions.time._
import com.twitter.util.{Await, Future}

object MuxServer {

  def main(args: Array[String]): Unit = {

    val host = "127.0.0.1"
    val port = "8080"
    val server = ThriftMux.server
        .withAdmissionControl.concurrencyLimit(maxConcurrentRequests = 10000, maxWaiters = 0)
        .withRequestTimeout(100.milliseconds)
        .withStatsReceiver(NullStatsReceiver)
        .withTracer(NullTracer)
        .serveIface(s"$host:$port", new DictServiceImpl)

    Await.ready(server)


  }

}
