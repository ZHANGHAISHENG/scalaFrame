package slick
import akka.actor.ActorSystem
import com.github.tminglei.slickpg._
import com.github.tminglei.slickpg.PgArraySupport
import scala.concurrent.{Await}
import scala.concurrent.duration.Duration
import slick.jdbc.PostgresProfile.api._
import slick.ast.BaseTypedType

object ListColumnTest extends  ExPostgresProfile with PgArraySupport
{

  // Definition of the DEPARTMENT table
  implicit val listType: BaseTypedType[List[String]] = MappedColumnType.base[List[String], String](
    x => x.foldLeft("")((x, y) => x + "," + y),
    {s: String =>
      val list: List[String] = s.split(",").toList.map(y => y.trim)
      list
    }
  )
  class Topic(tag: Tag) extends Table[(Long, String, List[String])](tag, "topic") { //department 注意大小写

    def id = column[Long]("id", O.PrimaryKey,O.AutoInc) // This is the primary key column
    def name = column[String]("name")
    def subIds = column[List[String]]("subIds")
    def * = (id, name,subIds )
  }
  val topics = TableQuery[Topic]
  val db = Database.forConfig("pg")

  def create(): Unit ={
    val setup = DBIO.seq(
      //topics.schema.create,
      topics ++= Seq(
        (0L, "zhs", List("a", "b"))
        ,(0L, "b", List("c", "d"))
      )
    )
    val r2 = Await.result(db.run(setup.transactionally),Duration.Inf)
    println(r2)
  }

  def main(args: Array[String]): Unit = {
    import MyPostgresProfile.api._
    implicit val system = ActorSystem()
    implicit val ec = system.dispatcher

    val q1  =  topics.filter(_.id === 1L).map(_.subIds).result.headOption
    val r = db.run(q1.transactionally).map{
      case Some(subIds) =>
       val q2  =  topics.filter(_.name.inSetBind(subIds)).result
        Await.result(db.run(q2.transactionally),Duration.Inf)
      case _ =>
    }
    val r2 = Await.result(r,Duration.Inf)
    println(r2)

    val r3 = for{
      a <- topics.filter(_.id === 1L).map(_.subIds).result.headOption
      b <- topics.filter(_.name.inSetBind(a.getOrElse(List.empty))).result
    } yield b
    val r4 = Await.result(db.run(r3),Duration.Inf)
    println(r4)

    //>>缺陷：一次把所有的结果拿了回来
    val r5 = topics.result.map(x => x.filter( y =>y._3.contains("a")))
    println(Await.result(db.run(r5),Duration.Inf))


/*
    query = query.filter {
      case (_, item, _, _, _) => {
        split(item.name.toLowerCase, " "). // ?
        // something like .exists(_ => similarTo("keyword", _)
      }
    }
*/

    /*val r6 = topics.filter(x => x.subIds @> List("a"))
    println(Await.result(db.run(r6.result),Duration.Inf))*/
  }
}
