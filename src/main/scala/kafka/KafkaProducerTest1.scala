package kafka


import scala.util.control.NonFatal
import akka.actor.ActorSystem
import akka.kafka.{ProducerMessage, ProducerSettings}
import akka.kafka.scaladsl.Producer
import akka.stream.scaladsl._
import akka.stream._
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ByteArraySerializer, Serializer, StringSerializer}
import io.prometheus.client.Counter

import scala.concurrent.{ExecutionContext, Future}

case class User(id: String,name: String)

object KafkaProducerTest1 extends Converter[User] {
  implicit def actorSystem: ActorSystem = ActorSystem("adx-edge-" + java.util.UUID.randomUUID().toString.toLowerCase)
  implicit def mt: Materializer = ActorMaterializer()
  implicit def ec: ExecutionContext = actorSystem.dispatcher

  def keyTransfer(t: User): String = t.id
  def valueTransfer(t: User): Array[Byte] = toByteArray(t)

  val producerKeySerializer: Serializer[String] = new StringSerializer
  val producerValueSerializer: Serializer[Array[Byte]] = new ByteArraySerializer
  val bufferSize: Int = 10000
  val overflowStrategy: OverflowStrategy = akka.stream.OverflowStrategy.backpressure
  val kafkaTopic: String = actorSystem.settings.config.getString("kafka.logs.topic-out")
  lazy val kafkaQueue: SourceQueueWithComplete[User] = queue(kafkaTopic)

  def queue(topic: String)(implicit system: ActorSystem, mt: Materializer): SourceQueueWithComplete[User] =
    Source
      .queue[User](bufferSize, overflowStrategy)
      .map(e => ProducerMessage.Message(new ProducerRecord(topic, keyTransfer(e), valueTransfer(e)), e))
      .via(Producer.flow(ProducerSettings(system, producerKeySerializer, producerValueSerializer): ProducerSettings[String, Array[Byte]]))
      .withAttributes(ActorAttributes.supervisionStrategy(decider))
      .map(_ => kafkaSentCounter.inc())
      .to(Sink.ignore)
      .run()

  val kafkaSentCounter: Counter = Counter.build()
    .name("sent_to_kafka_total")
    .help("sent_to_kafka_total")
    .register()

   val decider: Supervision.Decider = {
    case NonFatal(e) =>
      e.printStackTrace()
      Supervision.Resume
  }

  def main(args: Array[String]): Unit = {
    val a: Future[QueueOfferResult] = kafkaQueue offer User("1","zhs")
    Thread.sleep(2000)
    println(a)
  }
}
