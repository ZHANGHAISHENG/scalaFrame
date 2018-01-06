package finagle.finagle_thrift.complete

import com.test.logger.LoggerService.{GetLogSize, Log}
import com.test.logger.{LoggerService, ReadException}
import com.twitter.finagle._
import com.twitter.finagle.service.{RetryExceptionsFilter, RetryPolicy}
import com.twitter.finagle.util.DefaultTimer
import com.twitter.util.{Await, Future, Throw, Try}

object RetryTest {

  def main(args: Array[String]): Unit = {
    //客户端
    val clientServiceIface: LoggerService.ServiceIface = Thrift.client.newServiceIface[LoggerService.ServiceIface]("localhost:1234", "thrift_client")

    //重连策略
    val retryPolicy = RetryPolicy.tries[Try[GetLogSize.Result]](3, {
      case Throw(ex: ReadException) => true
    })

    //执行
    val retriedGetLogSize = new RetryExceptionsFilter(retryPolicy, DefaultTimer).andThen(clientServiceIface.getLogSize)
    val r: Future[Int] = retriedGetLogSize(GetLogSize.Args())
    Await.result(r)
    r.onSuccess(x => println(x))


  }

}
