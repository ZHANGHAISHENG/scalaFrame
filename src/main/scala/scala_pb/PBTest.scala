package scala_pb

import java.util.Base64

import com.chinamobiad.adx.idl.{Bool, DspApiType, IntervalHour, Placement}

/**
  * pb 编译依赖python
  */
object PBTest {

  def main(args: Array[String]): Unit = {

    val p = Placement(id = "id1",
      pubAppId = 1,
      dspApiType = DspApiType.C2S,
      ecpm = 1.2,
      isAgApp = Bool.True,
      dspCode = 1,
      activeIntervalsForDsp = List(IntervalHour(startAt = 1,endAt = 2)),
      extData = Map("k1" -> "v1"))

    val b = p.toByteArray
    val p2 = Placement.parseFrom(b)
    println(p2)

    val s = Base64.getEncoder.encodeToString(b)
    val p3 = Placement.parseFrom(Base64.getDecoder.decode(s))
    println(p3)
  }

}
