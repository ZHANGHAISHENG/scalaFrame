package finagle.finagle_thrift.complete

import com.test.logger.LoggerService
import com.test.logger.LoggerService.Log
import com.twitter.finagle._
import com.twitter.util.{Await, Future}

object Client1 {
  def main(args: Array[String]): Unit = {
    //客户端：使用newServiceIface需要使用参数包装对象
    val clientServiceIface: LoggerService.ServiceIface = Thrift.client.newServiceIface[LoggerService.ServiceIface]("localhost:1234", "thrift_client")
    val result: Future[String] = clientServiceIface.log(Log.Args("hello", 1))
    //等待相应
    Await.result(result)
    //输出结果
    result.onSuccess( x => println(x))

    //客户端：使用newIface可以直接传递参数
    val stub = Thrift.client.newIface[LoggerService.FutureIface]("localhost:1234")
    val r: Future[String] = stub.log("hello",1)
    Await.result(r)
    r.onSuccess( x => println(x))
  }

}
