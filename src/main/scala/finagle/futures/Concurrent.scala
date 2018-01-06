package finagle.futures

import com.twitter.util.Future

/**
  * 并行组合
  */
object Concurrent {

  def main(args: Array[String]): Unit = {
    //抓取网页
    def fetchUrl(url: String): Future[Array[Byte]] = Future {
      Thread.sleep(1000)//模拟网络延时
      println("-e-")
      //val a = 1/0     //模拟出现异常情况
      url.getBytes      //简单处理，当做正常返回数据
    }
    //抓取网页图片链接
    def findImageUrls(bytes: Array[Byte]): Seq[String] = {List("https://twitter.github.io/finagle/guide/_static/logo_small.png","https://twitter.github.io/finagle/guide/_static/logo_small.png")}

    val url = "http://www.google.com"
    val collected: Future[Seq[Array[Byte]]] = fetchUrl(url).flatMap { bytes =>
        val fetches: Seq[Future[Array[Byte]]] = findImageUrls(bytes).map { url => println("-s-");fetchUrl(url) }
        Future.collect(fetches) //合并结果
    }

    collected.onSuccess { c =>
      println("Found image of size " + c.foldLeft(0)(_ + _.size))
    }
    collected.onFailure{ ex =>
      println("failed " + ex)
    }

  }

}
