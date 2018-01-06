package finagle.finagle_thrift.simple

import com.twitter.finagle.Thrift
import com.twitter.util.Await

class DictServiceTestServer {

  def start = {
    val service = Thrift.server.serveIface("localhost:9999", new DictServiceImpl)
    Await.ready(service)
  }

}

object DictServiceTestServer extends  DictServiceTestServer {

  def main(args: Array[String]): Unit = {
      start
  }

}