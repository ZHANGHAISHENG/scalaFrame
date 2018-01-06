package finagle.finagle_thrift.simple

import com.twitter.finagle.Thrift
import com.twitter.util.Future
import in.xnnyygn.dictservice.DictService

class DictServiceTestClient {
  val stub = Thrift.client.newIface[DictService.FutureIface]("localhost:9999") // 或 ("localhost:9999","thrift_client")
}

object DictServiceTestClient extends DictServiceTestClient {
  def main(args: Array[String]): Unit = {
    //put
    val f1: Future[Unit] = stub.put("a","100$")
    f1.onSuccess { c =>
      println("put success.")
    }
    Thread.sleep(200)

    //get
    val f2: Future[String] = stub.get("a")
    f2.onSuccess { v =>
      println("get success："+v)
    }
    Thread.sleep(200)
  }
}