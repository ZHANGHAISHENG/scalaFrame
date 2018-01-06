package finagle.futures

import com.twitter.util.Future

/**
  * 顺序执行
  */
object Sequential {

  def main(args: Array[String]): Unit = {
    //抓取网页
    def fetchUrl(url: String): Future[Array[Byte]] = Future {
      Thread.sleep(1000)//模拟网络延时
      //val a = 1/0     //模拟出现异常情况
      url.getBytes      //简单处理，当做正常返回数据
    }
    //抓取网页图片链接
    def findImageUrls(bytes: Array[Byte]): Seq[String] = {List("https://twitter.github.io/finagle/guide/_static/logo_small.png")}

    val url = "http://www.google.com"
    val f: Future[Array[Byte]] = fetchUrl(url).flatMap { bytes =>
      val images = findImageUrls(bytes)
      if (images.isEmpty)
        Future.exception(new Exception("no image"))
      else
        fetchUrl(images.head)
    }

    f.onSuccess { image =>
      println("Found image of size " + image.size)
    }
    f.onFailure{ ex =>
      println("failed " + ex)
    }

  }

}
