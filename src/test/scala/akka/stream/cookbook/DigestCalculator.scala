package akka.stream.cookbook

import java.security.MessageDigest

import akka.NotUsed
import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import akka.stream.scaladsl.{Sink, Source}
import akka.util.ByteString
import docs.RecipeSpec
import scala.concurrent.Await
import scala.concurrent.duration._

class RecipeDigest extends RecipeSpec {

    " digest of a ByteString stream" in {
      import akka.stream.stage._
      val data = Source(List(ByteString("abcdbcdecdef"), ByteString("defgefghfghighijhijkijkljklmklmnlmnomnopnopq")))
      class DigestCalculator(algorithm: String) extends GraphStage[FlowShape[ByteString, ByteString]] {
        val in = Inlet[ByteString]("DigestCalculator.in")
        val out = Outlet[ByteString]("DigestCalculator.out")
        override val shape = FlowShape.of(in, out)
        override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {
          val digest = MessageDigest.getInstance(algorithm)
          setHandler(out, new OutHandler {
            override def onPull(): Trigger = {
              pull(in)
            }
          })
          setHandler(in, new InHandler {
            override def onPush(): Trigger = {
              val chunk = grab(in)
              digest.update(chunk.toArray)
              pull(in)
            }
            override def onUpstreamFinish(): Unit = {
              emit(out, ByteString(digest.digest()))
              completeStage()
            }
          })
        }
      }
      val digest: Source[ByteString, NotUsed] = data.via(new DigestCalculator("SHA-256"))
      Await.result(digest.runWith(Sink.head), 3.seconds) should be(
        ByteString(
          0x24, 0x8d, 0x6a, 0x61,
          0xd2, 0x06, 0x38, 0xb8,
          0xe5, 0xc0, 0x26, 0x93,
          0x0c, 0x3e, 0x60, 0x39,
          0xa3, 0x3c, 0xe4, 0x59,
          0x64, 0xff, 0x21, 0x67,
          0xf6, 0xec, 0xed, 0xd4,
          0x19, 0xdb, 0x06, 0xc1))
    }


  "Parsing lines from a stream of ByteStrings" in {
    val rawData = Source(List(
      ByteString("Hello World"),
      ByteString("\r"),
      ByteString("!\r"),
      ByteString("\nHello Akka!\r\nHello Streams!"),
      ByteString("\r\n\r\n")))

    import akka.stream.scaladsl.Framing
    val linesStream = rawData.via(Framing.delimiter(
      ByteString("\r\n"), maximumFrameLength = 100, allowTruncation = true)).map(_.utf8String)

    Await.result(linesStream.limit(10).runWith(Sink.seq), 3.seconds) should be(List(
      "Hello World\r!",
      "Hello Akka!",
      "Hello Streams!",
      ""))
  }

  "uncompressed" in {
    import akka.stream.scaladsl.Compression
    val compressed = Source.single(ByteString.fromString("Hello World")).via(Compression.gzip)
    val uncompressed = compressed.via(Compression.gunzip()).map(_.utf8String)
    Await.result(uncompressed.runWith(Sink.head), 3.seconds) should be("Hello World")
  }


}
