package finagle.finagle_thrift.simple

import java.util.concurrent.ConcurrentHashMap

import com.twitter.util.Future
import in.xnnyygn.dictservice.{DictService, DictServiceException}

class DictServiceImpl extends DictService.FutureIface {

  private val map = new ConcurrentHashMap[String, String]

  def get(key: String): Future[String] = Option(map.get(key)) match {
    case Some(value) => Future.value(value)
    case _ => Future.exception(new DictServiceException("no such key"))
  }

  def put(key: String, value: String) = {
    map.put(key, value)
    Future.Done
  }

}
