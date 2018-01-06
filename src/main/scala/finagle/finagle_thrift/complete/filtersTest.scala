package finagle.finagle_thrift.complete

import com.test.logger.LoggerService
import com.test.logger.LoggerService.Log
import com.twitter.conversions.time._
import com.twitter.finagle._
import com.twitter.finagle.service.TimeoutFilter
import com.twitter.finagle.util.DefaultTimer
import com.twitter.util.{Await, Duration, Future, Try}

object filtersTest {

  def main(args: Array[String]): Unit = {
    //客户端
    val clientServiceIface: LoggerService.ServiceIface = Thrift.client.newServiceIface[LoggerService.ServiceIface]("localhost:1234", "thrift_client")

    //大写过滤器
    val uppercaseFilter = new SimpleFilter[Log.Args, Log.SuccessType] {
      def apply(req: Log.Args,
                service: Service[Log.Args, Log.SuccessType]
               ): Future[Log.SuccessType] = {
        val uppercaseRequest = req.copy(message = req.message.toUpperCase)
        service(uppercaseRequest)
      }
    }

    //超时过滤器
    def timeoutFilter[Req, Rep](duration: Duration) = {
      val exc = new IndividualRequestTimeoutException(duration)
      val timer = DefaultTimer
      new TimeoutFilter[Req, Rep](duration, exc, timer)
    }
    val filteredLog = timeoutFilter(2.seconds)
      .andThen(uppercaseFilter)
      .andThen(clientServiceIface.log)

    //执行查看结果
    val r: Future[String] = filteredLog(Log.Args("hello", 2))
    Await.result(r)
    r.onSuccess( x => println(x))

    //method convert
    val filteredMethodIface: LoggerService[Future] = Thrift.client.newMethodIface(clientServiceIface.copy(log = filteredLog))
    Await.result(filteredMethodIface.log("ping", 3).map(println))

  }

}
