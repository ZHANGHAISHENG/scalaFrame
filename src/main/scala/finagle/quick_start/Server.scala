package finagle.quick_start

import com.twitter.finagle.{Http, Service, http}
import com.twitter.io.{Buf, Reader}
import com.twitter.util.{Await, Future}

object Server {

  def main(args: Array[String]): Unit = {
    //http:
    val service = new Service[http.Request, http.Response] {
      def apply(req: http.Request): Future[http.Response] =
       {
         //方式1：发送string
         //val r = http.Response(req.version, http.Status.Ok)
         //r.setContentString("hello")

         //方式2：发送Buf
         val r = http.Response.apply(req.version, http.Status.Ok, Reader.fromBuf(Buf.Utf8("hello")))
         Future.value(
           r
         )

       }
    }

    val httpServer = Http.serve(":8080", service)
    Await.ready(httpServer)

  }

}
