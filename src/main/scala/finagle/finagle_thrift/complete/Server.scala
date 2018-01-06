package finagle.finagle_thrift.complete

import com.test.logger.{LoggerService, ReadException}
import com.twitter.finagle._
import com.twitter.util.{Await, Future}

object Server {

  def main(args: Array[String]): Unit = {

    //服务器定义
    val server = Thrift.server.serveIface("localhost:1234", new LoggerService[Future] {
        //打印日志
        def log(message: String, logLevel: Int): Future[String] = {
          println(s"[$logLevel] Server received: '$message'")
          Future.value(s"You've sent: ('$message', $logLevel)")
        }

        //交替报异常
        var counter = 0
        def getLogSize(): Future[Int] = {
          counter += 1
          if (counter % 2 == 1) {
            println(s"Server: getLogSize ReadException")
            Future.exception(new ReadException("read exception"))
          } else {
            println(s"Server: getLogSize Success")
            Future.value(4)
          }
        }
      }
    )

    //启动
    Await.ready(server)
  }

}
