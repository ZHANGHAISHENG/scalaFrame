package aerospike
import io.prometheus.client.{CollectorRegistry, Counter, Summary, Histogram}


object SimpleMetrics {

  val registry: CollectorRegistry = new CollectorRegistry()

  val requestLatency: Histogram = Histogram.build()
    .name("request_latency")
    .help("api request latency")
    .labelNames("api")
    .register(registry)

  val s2sQueryCounter: Counter = Counter.build()
    .name("s2s_query_total")
    .help("s2s query count")
    .labelNames("dsp")
    .register(registry)

  val dspLatency: Histogram = Histogram.build()
    .name("dsp_latency")
    .help("dsp request latency")
    .labelNames("dsp")
    .register(registry)

  val kafkaSentCounter: Counter = Counter.build()
    .name("sent_to_kafka_total")
    .help("sent_to_kafka_total")
    .register()

  val finagleClientLatency: Histogram = Histogram.build()
    .name("finagle_client_latency")
    .help("finagle client call latency")
    .labelNames("function")
    .register(registry)

  val aerospikeLatency = Histogram.build()
    .name("aerospike_latency")
    .help("aerospike latency")
    .labelNames("action")
    .register(registry)

}
