package aerospike


import java.net.URI

import scala.concurrent._
import scala.concurrent.duration._
import scala.collection.JavaConverters._
import io.netty.channel.nio.NioEventLoopGroup
import com.aerospike.client.async.EventPolicy
import com.typesafe.config.{Config, ConfigFactory}
import com.aerospike.client.async._
import com.aerospike.client.listener.{DeleteListener, RecordListener, WriteListener}
import com.aerospike.client.policy.{ClientPolicy, GenerationPolicy, WritePolicy}
import com.aerospike.client._
import com.aerospike.client.Host


object AsConfig {

  def apply(c: Config): AsConfig = AsConfig(
    hosts = c.getStringList("hosts").asScala.toList
    , timeout = c.getDuration("timeout").toMillis.milliseconds
  )

  def apply(c: String): AsConfig = this.apply(ConfigFactory.load().getConfig(c))

}

final case class AsConfig(
                           hosts: List[String]
                           , timeout: FiniteDuration
                         )


trait AsClient {

  def get(ns: String, set: String, key: String): Future[Option[Record]]

  def delete(ns: String, set: String, key: String): Future[Boolean]

  def delete(ns: String, set: String, key: String, binNames: String*): Future[Unit]

  def put(ns: String, set: String, key: String, bins: Seq[Bin]): Future[Unit]

  def put(ns: String, set: String, key: String, bins: Seq[Bin], expire: FiniteDuration): Future[Unit]

  def cas(ns: String, set: String, key: String, generation: Int, bins: Seq[Bin]): Future[Unit]

}

object SimpleAsyncAsClient {

  def apply(asSetting: AsConfig): SimpleAsyncAsClient = new SimpleAsyncAsClient(asSetting)

}

class SimpleAsyncAsClient(config: AsConfig) extends AsClient {

  private[this] val timeout = config.timeout.toMillis.toInt

  private[this] val (underlying, eventLoops) =  {

    val hosts = config.hosts
      .map(h => URI.create("as://" + h))
      .map(u => new Host(u.getHost, u.getPort))

    val policy = new ClientPolicy()
    policy.maxConnsPerNode = 1024

    policy.failIfNotConnected = true

    val eventPolicy = new EventPolicy()

    // Create 4 Netty event loops using the Netty API.
    // val group = new EpollEventLoopGroup(4)
    val group: NioEventLoopGroup = new NioEventLoopGroup(4)

    // Create Aerospike compatibile wrapper of Netty event loops.
    val eventLoops = new NettyEventLoops(eventPolicy, group)

    policy.eventLoops = eventLoops
    policy.timeout = timeout

    val u = new AerospikeClient(policy, hosts:_*)

    (u, eventLoops)
  }

  private[this] val defaultWritePolicy: WritePolicy = new WritePolicy(underlying.getWritePolicyDefault)

  def get(ns: String, set: String, key: String): Future[Option[Record]] = {
    val timer = SimpleMetrics.aerospikeLatency.labels("get").startTimer()
    val eventLoop: EventLoop = eventLoops.next()
    val p = Promise[Option[Record]]
    val listener: RecordListener = new RecordListener {
      override def onFailure(exception: AerospikeException): Unit = {
        timer.observeDuration()
        p.failure(exception)
      }
      override def onSuccess(key: Key, record: Record): Unit = {
        timer.observeDuration()
        p.success(Option(record)) // record can be null
      }
    }

    underlying.get(eventLoop, listener, null, new Key(ns, set, key))
    p.future
  }

  def delete(ns: String, set: String, key: String): Future[Boolean] = {
    val timer = SimpleMetrics.aerospikeLatency.labels("delete").startTimer()
    val eventLoop = eventLoops.next()
    val p = Promise[Boolean]

    val listener = new DeleteListener {
      override def onFailure(exception: AerospikeException): Unit = {
        timer.observeDuration()
        p.failure(exception)
      }
      override def onSuccess(key: Key, existed: Boolean): Unit = {
        timer.observeDuration()
        p.success(existed)
      }
    }

    underlying.delete(eventLoop, listener, null, new Key(ns, set, key))

    p.future
  }

  def delete(ns: String, set: String, key: String, binNames: String*): Future[Unit] = {
    val timer = SimpleMetrics.aerospikeLatency.labels("delete").startTimer()
    val eventLoop: NettyEventLoop = eventLoops.next()

    val p = Promise[Unit]

    val listener: WriteListener = new WriteListener {
      override def onFailure(exception: AerospikeException): Unit = {
        timer.observeDuration()
        p.failure(exception)
      }
      override def onSuccess(key: Key): Unit = {
        timer.observeDuration()
        p.success(())
      }
    }

    val k: Key = new Key(ns, set, key)

    val bs: Seq[Bin] = binNames.map(Bin.asNull)

    underlying.put(eventLoop, listener, defaultWritePolicy, k, bs: _*)

    p.future

  }

  private[this] def put(ns: String, set: String, key: String, bins: Seq[Bin], policy: WritePolicy): Future[Unit] = {
    val timer = SimpleMetrics.aerospikeLatency.labels("put").startTimer()
    val eventLoop: NettyEventLoop = eventLoops.next()
    val p = Promise[Unit]
    val listener: WriteListener = new WriteListener {
      override def onFailure(exception: AerospikeException): Unit = {
        timer.observeDuration()
        exception.printStackTrace()
        p.failure(exception)
      }
      override def onSuccess(key: Key): Unit = {
        timer.observeDuration()
        p.success(())
      }
    }

    val k = new Key(ns, set, key)

    underlying.put(eventLoop, listener, policy, k, bins:_*)

    p.future

  }

  def put(ns: String, set: String, key: String, bins: Seq[Bin]): Future[Unit] = {
    put(ns, set, key, bins, defaultWritePolicy)
  }

  def put(ns: String, set: String, key: String, bins: Seq[Bin], expire: FiniteDuration): Future[Unit] = {
    val p = new WritePolicy(defaultWritePolicy)
    p.expiration = expire.toSeconds.toInt
    put(ns, set, key, bins, p)
  }

  def cas(ns: String, set: String, key: String, generation: Int, bins: Seq[Bin]): Future[Unit] = {
    val p = new WritePolicy(defaultWritePolicy)
    p.generationPolicy = GenerationPolicy.EXPECT_GEN_EQUAL
    put(ns, set, key, bins, p)
  }

}
