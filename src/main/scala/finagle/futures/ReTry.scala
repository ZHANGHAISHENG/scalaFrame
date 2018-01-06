package finagle.futures
import com.twitter.util.Future
import com.twitter.finagle.{Http, Service, TimeoutException, http}

/**
  * 检验方式待定
  */
object ReTry {
  val client: Service[http.Request, http.Response] = Http.newService("127.0.0.1:8080")

  def fetchUrl(url: String): Future[http.Response] = {
    val request = http.Request(http.Method.Get, url)
    request.host = "127.0.0.1"
    client(request)
  }

  def fetchUrlWithRetry(url: String): Future[http.Response] = fetchUrl(url).rescue {
      case exc: TimeoutException => println(exc); fetchUrlWithRetry(url)
    }

  def main(args: Array[String]): Unit = {
      val r = fetchUrlWithRetry("https://twitter.github.io/finagle/guide/_static/logo_small.png")
      Thread.sleep(2000)
      r.onSuccess(x => println(x))
      r.onFailure(x => println(x))
  }

}
