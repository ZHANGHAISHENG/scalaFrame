package finagle.configuration
import com.twitter.finagle.Http
import com.twitter.finagle.tunable.StandardTunableMap
import com.twitter.util.Duration
import com.twitter.util.tunable.{Tunable, TunableMap}

/**
  * 用法待定
  */
object Usage {

  def main(args: Array[String]): Unit = {
    val clientId = "exampleClient"
    val timeoutTunableId = "com.example.service.Timeout"

    val tunables: TunableMap = StandardTunableMap(clientId)
    val timeoutTunable: Tunable[Duration] = tunables(TunableMap.Key[Duration](timeoutTunableId))

    val client = Http.client.withLabel(clientId)
                .withRequestTimeout(timeoutTunable)
                .newService("localhost:10000")
  }

}
