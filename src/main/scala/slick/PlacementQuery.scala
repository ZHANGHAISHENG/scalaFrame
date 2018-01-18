package slick

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import slick.jdbc.PostgresProfile.api._
import scala.concurrent.ExecutionContext.Implicits.global

object PlacementQuery {
  class DspApp(tag: Tag) extends Table[(Long, String, Int)](tag, "dsp_app") {
    def id = column[Long]("id", O.PrimaryKey,O.AutoInc)
    def name = column[String]("name")
    def useType = column[Int]("use_type")
    def * = (id, name, useType)
  }
  class PubApp(tag: Tag) extends Table[(Long, String, Boolean)](tag, "pub_app") {
    def id = column[Long]("id", O.PrimaryKey,O.AutoInc)
    def name = column[String]("name")
    def isAg = column[Boolean]("is_ag")
    def * = (id, name, isAg)
  }
  class PubAppPlacement(tag: Tag) extends Table[(Long, Long, String)](tag, "pub_app_placement") {
    def id = column[Long]("id", O.PrimaryKey,O.AutoInc)
    def pubAppId = column[Long]("pub_app_id")
    def name = column[String]("name")
    def * = (id, pubAppId, name)
  }
  class PubAppPlacementConfig(tag: Tag) extends Table[(Long, Long, Long,Int)](tag, "pub_app_placement_config") {
    def id = column[Long]("id", O.PrimaryKey,O.AutoInc)
    def placementId = column[Long]("placement_id")
    def dspAppId = column[Long]("dsp_app_id")
    def epcm = column[Int]("epcm")
    def * = (id, placementId, dspAppId,epcm )
  }

  val db = Database.forConfig("pg")
  val dspApp = TableQuery[DspApp]
  val pubApp = TableQuery[PubApp]
  val pubAppPlacement = TableQuery[PubAppPlacement]
  val pubAppPlacementConfig = TableQuery[PubAppPlacementConfig]

  def main(args: Array[String]): Unit = {
    val q =   for {
      (((p,d),pp),ppc) <- pubApp.join(dspApp).on{case (p,d) => p.isAg === (d.useType === 1)}
               .join(pubAppPlacement).on{case ((p,d),pp) => p.id === pp.pubAppId }
               .joinLeft(pubAppPlacementConfig).on{case (((p,d),pp),ppc) =>
                  List(
                    ppc.placementId === pp.id,
                    ppc.dspAppId === d.id,
                  ).reduceLeftOption(_ && _)
                   .getOrElse(false: Rep[Boolean])
               }.sortBy{case (((p,d),pp),ppc) => d.id.asc}
      } yield (p,d,pp,ppc)

    val r: Future[Unit] = db.run(q.result).map(_.foreach {
      case (p,d,pp,ppc) =>
        println(p +"--" + d + "--" + pp + "--" + ppc)
    })
    Await.result(r,Duration.Inf)

  }

/*  select * from   pub_app a
  JOIN dsp_app b on ( case  when a.is_ag then 1 else 0 end) = b.use_type
  join pub_app_placement c on a.id = c.pub_app_id
  left join pub_app_placement_config d on c.id = d.placement_id and  b.id = d.dsp_app_id
  order by a.id , c.name
  */
}
