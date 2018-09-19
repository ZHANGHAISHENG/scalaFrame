package AkAkHttp

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.util.ByteString
import io.circe.generic.auto._
import io.circe.syntax._

import scala.concurrent.Future
import scala.util.{Failure, Success}

object EdgeHttpsTest {
  implicit val system = ActorSystem()
  implicit val mat = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  def main(args: Array[String]): Unit = {
    getAd(json)
  }

  private[this] def getAd(json: String) = {
    //val url = "https://bid-stage.iadmob.com/api/v1/adx/1"
    val url = "http://127.0.0.1:4400/api/v1/adx/1"
    val entity = HttpEntity(string = json, contentType = ContentTypes.`application/json`)
    val req = HttpRequest(uri = url, method = HttpMethods.POST, entity = entity)
    val responseFuture: Future[HttpResponse] = Http().singleRequest(req)
    responseFuture
      .onComplete {
        case Success(res) =>
          res.entity.dataBytes.runFold(ByteString(""))(_ ++ _).map(_.toArray).map { bytes =>
            println(new String(bytes))
          }
        case Failure(_)   => sys.error("something wrong")
      }
  }


  val json =
    """
      {
      	"id": "492e41c6d8bc42e3a0db8805b848b395",
      	"apiVersion": "9.0",
      	"app": {
      		"id": "usokdusz",
      	    "name": "com.screenlockshow.android",
              "version": "2.0"
      	},
        "device": {
      		"did": "864230036377785",
      		"type": 1,
      		"os": 1,
      		"osVersion": "6.0",
      		"vendor": "Meizu",
      		"model": "M5",
      		"ua": "Mozilla/5.0 (Linux; Android 6.0;M5 Build/MRA58K; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/44.0.2403.140 Mobile Safari/537.36",
      		"language": null,
      		"connType": 1,
      		"androidId": "3684daa8487cf202",
      		"imsi": "460023192787105",
      		"adId": null,
      		"height": 1280,
      		"width": 720,
      		"carrier": 1,
      		"cellularId": null,
      		"dpi": 320,
      		"mac": "28:fa:a0:39:79:cc",
      		"wifi": null,
      	"ip": "116.24.65.113",
          "clientIp": "127.0.0.1"
      	},
      	"geo": {
      		"latitude": 22.544170379638672,
      		"longitude": 113.94439697265625,
      		"timestamp": 1497521504428
      	},
      	"user": null,
      	"adSlot": {
      		"id": "gbelm3mv",
      		"size": {
      			"width": 360,
      			"height": 300
      		},
      		"minCpm": 0,
      		"orientation": 1
      	},
        "adSources": [
          {
            "dspCode": "",
            "dspAppId": -1,
            "adSlotId":""
          }
        ],
        "pubApiVersion": "2.0.1"
      }
    """.stripMargin

}
