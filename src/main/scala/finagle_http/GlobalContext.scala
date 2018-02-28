package finagle_http

import akka.actor.ActorSystem
import akka.stream.Materializer
//import com.chinamobiad.adx.edge.DspHttpClient
//import com.chinamobiad.adx.edge.aerospike.{AsConfig, SimpleAsyncAsClient}
//import com.chinamobiad.adx.edge.cache.placement.PlacementCache
//import com.chinamobiad.adx.edge.database.{DatabaseComponent, DatabaseExtension}

/**
  * Created by weiwen on 17-6-24.
  */
object GlobalContext {
  def apply()(implicit system: ActorSystem, mat: Materializer): GlobalContext = {
    //implicit val dc: DatabaseComponent = DatabaseExtension(system).databaseComponent
    //val cache: PlacementCache = PlacementCache()
    GlobalContext(system = system, mat = mat)
  }
}


final case class GlobalContext(system: ActorSystem
                                , mat: Materializer
                                //, dc: DatabaseComponent
                                //, cache: PlacementCache
                                , cache: String = "undefine"
                              ) {

  //lazy val aerospike: SimpleAsyncAsClient = SimpleAsyncAsClient(AsConfig("aerospike.cluster.main"))

  val a = ""
  val dspClient: DspHttpClient = DspHttpClient()(system)

}
