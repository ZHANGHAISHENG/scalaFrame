package slick.tables
// AUTO-GENERATED Slick data model
/** Stand-alone Slick data model for immediate use */
object Tables extends {
  val profile = slick.jdbc.PostgresProfile
} with Tables

trait Tables {
  val profile: slick.jdbc.JdbcProfile
  import profile.api._
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}

  case class DepartmentRow(id: Long, no: Option[Int] = None, name: Option[String] = None)
  implicit def GetResultDepartmentRow(implicit e0: GR[Long], e1: GR[Option[Int]], e2: GR[Option[String]]): GR[DepartmentRow] = GR{
    prs => import prs._
    DepartmentRow.tupled((<<[Long], <<?[Int], <<?[String]))
  }
  class Department(_tableTag: Tag) extends profile.api.Table[DepartmentRow](_tableTag, "department") {
    def * = (id, no, name) <> (DepartmentRow.tupled, DepartmentRow.unapply)
    def ? = (Rep.Some(id), no, name).shaped.<>({r=>import r._; _1.map(_=> DepartmentRow.tupled((_1.get, _2, _3)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    val id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    val no: Rep[Option[Int]] = column[Option[Int]]("no", O.Default(None))
    val name: Rep[Option[String]] = column[Option[String]]("name", O.Length(255,varying=true), O.Default(None))
  }
  lazy val Department = new TableQuery(tag => new Department(tag))
}
