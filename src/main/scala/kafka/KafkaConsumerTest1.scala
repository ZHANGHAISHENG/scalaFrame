package kafka

import scala.collection.immutable.Seq
import scala.concurrent.duration.{FiniteDuration, _}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal
import akka.actor.ActorSystem
import akka.kafka.ConsumerMessage.{CommittableMessage, CommittableOffsetBatch}
import akka.kafka.scaladsl.Consumer
import akka.kafka.scaladsl.Consumer.Control
import akka.kafka.{ConsumerMessage, ConsumerSettings, Subscriptions}
import akka.stream.scaladsl.{RunnableGraph, Sink}
import akka.stream.{ActorAttributes, ActorMaterializer, Materializer, Supervision}
import com.typesafe.config.Config
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer}

object KafkaConsumerTest1 extends Converter[User] {


  implicit val system: ActorSystem = ActorSystem("adx-edge-" + java.util.UUID.randomUUID().toString.toLowerCase)
  implicit val mat: Materializer = ActorMaterializer()
  implicit def ec: ExecutionContext = system.dispatcher

  val consumerKeyDeserializer    = new StringDeserializer
  val consumerValueDeserializer = new ByteArrayDeserializer
  val config: Config = system.settings.config

  lazy val topicIn = config.getString("kafka.topic-in")
  lazy val consumerSettings = ConsumerSettings(system, consumerKeyDeserializer, consumerValueDeserializer).withClientId(java.util.UUID.randomUUID().toString.toUpperCase)
  lazy val commitBatchSize: Int = config.getInt("kafka.commit.batch-size")
  lazy val commitBatchTimeWindow: FiniteDuration = config.getDuration("kafka.commit.batch-time-window").toMillis.milliseconds

  val maxPartitions = 1024
  lazy val decider: Supervision.Decider = {
    case NonFatal(e)                 =>
      system.log.warning("consuming flow error! will resume. reason: {}", e)
      Supervision.Resume
  }
  val parallelism = 1000
  def flow()(implicit ec: ExecutionContext): RunnableGraph[Control] = Consumer
    .committablePartitionedSource(consumerSettings, Subscriptions.topics(topicIn))
    .flatMapMerge(maxPartitions, _._2)
    .groupedWithin(commitBatchSize, commitBatchTimeWindow)
    .map { (xs: Seq[CommittableMessage[String, Array[Byte]]]) =>
      val offsets: CommittableOffsetBatch = foldCommittableOffsets(xs.map(_.committableOffset))
      val aggrs = extract(xs)
      (aggrs, offsets)
    }
    .mapAsync(1) { case (xs, offsets) =>
      store(xs).map { _ => offsets }
    }
    .mapAsync(parallelism)(_.commitScaladsl())
    .withAttributes(ActorAttributes.supervisionStrategy(decider))
    .to(Sink.ignore)

  def foldCommittableOffsets(os: Seq[ConsumerMessage.CommittableOffset]): CommittableOffsetBatch =
    os.foldLeft(CommittableOffsetBatch.empty) { case (batch, p) =>
      batch.updated(p)
    }

  def extract(ms: Seq[CommittableMessage[String, Array[Byte]]]): Seq[User] = {
    ms.flatMap { m =>
      val xs: User = toObject(m.record.value())
      List(xs)
    }
  }
  def store(rows: Seq[User])(implicit ec: ExecutionContext): Future[Any] = {
    println(rows)
    Future.successful("")
  }

  def main(args: Array[String]): Unit = {
    flow().run()
  }
}
