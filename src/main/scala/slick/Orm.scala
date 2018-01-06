package slick

import slick.jdbc.PostgresProfile.api._

/**
  * slick 不使用这样的实现方式
  */
object Orm {

  type Person = (Int,String,Int,Int)
  class People(tag: Tag) extends Table[Person](tag, "PERSON") {
    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
    def name = column[String]("NAME")
    def age = column[Int]("AGE")
    def addressId = column[Int]("ADDRESS_ID")
    def * = (id,name,age,addressId)
    def address = foreignKey("ADDRESS",addressId,addresses)(_.id)
  }
  lazy val people = TableQuery[People]

  type Address = (Int,String,String)
  class Addresses(tag: Tag) extends Table[Address](tag, "ADDRESS") {
    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
    def street = column[String]("STREET")
    def city = column[String]("CITY")
    def * = (id,street,city)
  }
  lazy val addresses = TableQuery[Addresses]
  
  // fake ORM
  object PeopleFinder{
    def getByIds(ids: Seq[Int]): Seq[Person] = Seq()
    def getById(id: Int): Person = null
  }
  implicit class OrmPersonAddress(person: Person){
    def address: Address = null
  }
  implicit class OrmPrefetch(people: Seq[Person]){
    def prefetch(f: Person => Address) = people
  }
  object session{
    def createQuery(hql: String) = new HqlQuery
    def createCriteria(cls: java.lang.Class[_]) = new Criteria
    def save = ()
  }
  class Criteria{
    def add(r: Restriction) = this
  }
  type Restriction = Criteria
  class HqlQuery{
    def setParameterList(column: String, values: Array[_]): Unit = ()
  }
  object Property{
    def forName(s:String) = new Property
  }
  class Property{
    def in(array: Array[_]): Restriction = new Restriction
    def lt(i: Int) = new Restriction
    def gt(i: Int) = new Restriction
  }
  object Restrictions{
    def disjunction = new Criteria
  }

  def main(args: Array[String]): Unit = {
    val age = Property.forName("age")
    val q = session.createCriteria(classOf[Person])
      .add(
        Restrictions.disjunction
          .add(age lt 5)
          .add(age gt 65)
      )
  }

}
