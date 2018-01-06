package finagle.quick_start

import com.twitter.finagle.{Http, Service, http}
import com.twitter.util.{Await, Future}

object Server {

  def main(args: Array[String]): Unit = {
    //http:
    val service = new Service[http.Request, http.Response] {
      def apply(req: http.Request): Future[http.Response] =
       {
         //Thread.sleep(10000)
         Future.value(
           http.Response(req.version, http.Status.Ok)
         )
       }
    }

    val httpServer = Http.serve(":8080", service)
    Await.ready(httpServer)

  }

}
