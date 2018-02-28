package finagle.quick_start

import com.twitter.concurrent.AsyncStream
import com.twitter.finagle.http.Request
import com.twitter.finagle.{Http, Service, http}
import com.twitter.io.Buf
import com.twitter.util.{Await, Future}


object Client {

  def main(args: Array[String]): Unit = {
    val client: Service[http.Request, http.Response] = Http.newService("127.0.0.1:8080")//代理8888
    //请求方式一：
    val request = http.Request(http.Method.Get, "/")
    request.host = "127.0.0.1"
    val response: Future[http.Response] = client(request)

    Await.result(response.onSuccess { rep: http.Response =>

      //获取结果方式1：
      val r: Future[Array[Byte]] = AsyncStream.fromReader(rep.reader).foldLeft(Buf.Empty)(_ concat _).map(Buf.ByteArray.Owned.extract)
      r.onSuccess(x => println("success: " + new String(x)))
      //获取结果方式2：
      println("GET success: " + rep.getContentString())
    })
    Await.result(response.onFailure{ ex =>
      println("GET fail: " + ex)
    })

    Thread.sleep(2000)
    //请求方式二：
    client(Request("127.0.0.1")).onSuccess { response: http.Response =>
      println("received response " + response)
    }
    Thread.sleep(2000)
  }

}
