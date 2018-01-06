package finagle.service_filter
import com.twitter.util._
import com.twitter.conversions.time._
import com.twitter.finagle.service.RetryFilter
import com.twitter.finagle.util.DefaultTimer
import com.twitter.finagle.{Http, Service, SimpleFilter, http}


/**
  * 超时过滤测试：不起效果
  */
object FilterTest {

  class TimeoutFilter[Req, Rep](timeout: Duration, timer: Timer) extends SimpleFilter[Req, Rep] {
    def apply(request: Req, service: Service[Req, Rep]): Future[Rep] = {
      val res = service(request)
      res.within(timer, timeout)
    }
  }

  def main(args: Array[String]): Unit = {

    //service
    val service = new Service[http.Request, http.Response] {
      def apply(req: http.Request): Future[http.Response] =
       {
         Thread.sleep(3000)
         Future.value(
             http.Response(req.version, http.Status.Ok)
         )
       }
    }

    //请求timeOutFilter:超时过滤器
    val timeoutFilter = new TimeoutFilter[http.Request, http.Response](1.seconds,DefaultTimer)
    val timeoutService: Service[http.Request, http.Response] = timeoutFilter.andThen(service)

    //httpService
    val httpServer = Http.serve(":8080", timeoutService)
    Await.ready(httpServer)


  }

}
