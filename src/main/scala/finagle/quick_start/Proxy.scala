package finagle.quick_start

import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Http, Service}
import com.twitter.util.Await

/**
  *代理:
  * 先启动server.scala,在启动当前类，再使用client测试
  */
object Proxy {

  def main(args: Array[String]): Unit = {
    val client: Service[Request, Response] =
      Http.newService("127.0.0.1:8080")

    val server = Http.serve(":8888", client)
    Await.ready(server)
  }

}
