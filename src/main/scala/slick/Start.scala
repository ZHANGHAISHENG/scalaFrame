package slick

import slick.Start.Department

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import slick.jdbc.PostgresProfile.api._
import scala.concurrent.ExecutionContext.Implicits.global



object Start {

  // Definition of the DEPARTMENT table
  class Department(tag: Tag) extends Table[(Long, Int, String)](tag, "department") { //department 注意大小写
    def id = column[Long]("id", O.PrimaryKey,O.AutoInc) // This is the primary key column
  def no = column[Int]("no")
    def name = column[String]("name")
    // Every table needs a * projection with the same type as the table's type parameter
    def * = (id, no, name)
  }
  val department = TableQuery[Department]

  class User(tag: Tag) extends Table[(Long, Long, String, Int,  Long, String, String, Long)](tag, "users") {
    def id = column[Long]("id", O.PrimaryKey,O.AutoInc)
    def deptid = column[Long]("deptid")
    def name = column[String]("name")
    def gender = column[Int]("gender")
    def birthday = column[Long]("birthday")
    def phone = column[String]("phone")
    def wx = column[String]("wx")
    def createtime = column[Long]("createtime")
    def * = (id, deptid, name, gender, birthday, phone, wx, createtime)
    //def dept = foreignKey("deptid", deptid, department)(_.id)

  }

  val user = TableQuery[User]
  val db = Database.forConfig("pg")

  //添加user
  def createUser(u: (Long, Long, String, Int,  Long, String, String, Long)) = {
    val action = DBIO.seq(
      user += u
    )
    Await.result(db.run(DBIO.seq(action.transactionally)), Duration.Inf)
  }

  //添加
  def create(dept: (Long, Int, String)) = {
    val action = DBIO.seq(
      department += dept
    )
    Await.result(db.run(DBIO.seq(action.transactionally)), Duration.Inf)
  }

  //添加并返回
  def createAndGet(dept: (Long, Int, String)): Option[(Long, Int, String)] = {
    val q = (department returning department.map(_.id))
    val action = q += dept
    println(q.insertStatement)
    val id: Long = Await.result(db.run(action.transactionally),Duration.Inf)
    val r: Option[(Long, Int, String)] = findById(id)
    r
  }

  //添加或更新
  def createOrUpdate(dept: (Long, Int, String)) = {
    /*
    val action1 = department.insertOrUpdate(dept) //返回添加或更新的记录数
    val r1 = Await.result(db.run(action1.transactionally),Duration.Inf)
    println(r1)
    */
    val action2 = (department returning department).insertOrUpdate(dept) //更新返回None,插入返回插入记录
    val r2 = Await.result(db.run(action2.transactionally),Duration.Inf)
    println(r2)
  }

  //更新
  def updateById(id: Long,dept: (Int, String)): Int = {
    val q = department.filter(_.id === id).map(p => (p.no,p.name))
    val action = q.update(dept)
    println(q.updateStatement)
    val r = Await.result(db.run(action.transactionally),Duration.Inf)
    r
  }
  def updateByName(name: String,dept: (Int, String)): Int = {
    val q = for { d <- department if d.name === name } yield (d.no,d.name)
    val action = q.update(dept)
    println(q.updateStatement)
    val r = Await.result(db.run(action.transactionally),Duration.Inf)
    r
  }

  //删除
  def deleteById(id: Long) = {
    val q = department.filter(_.id === id)
    val action = q.delete
    println(action.statements.head)
    val r = Await.result(db.run(action.transactionally),Duration.Inf)
    r
  }

  //查询
  //根据id查询
  def findById(id: Long): Option[(Long, Int, String)] = {
    val q = department.filter(_.id === id)
    println(q.result.statements.headOption) // select "id", "no", "name" from "department" where "id" = 1
    val r = Await.result(db.run(q.result.headOption),Duration.Inf)
    r
  }

  //查询所有
  def findAll() = {
    val selects: Future[Unit] = db.run(department.result).map(_.foreach {
      case (id, no, name) =>
        println("  " + id + "\t" + no + "\t" + name)
    })
    Await.result(selects,Duration.Inf)
  }

  /**
     排序、分页
     过滤查询:
      string like =
      int = > <
    */
  def query(start: Int,size: Int) = {
    val strOpt1 = Option("部门1")
    val strOpt2 = Option("部门2")
    val strOpt3 = Option("部门3")
    val strOpt4 = Option("部门4")
    val q = department.withFilter{
                    d => d.name.like("%部门%")
                  }.withFilter{ d =>
                    List(
                      strOpt1.map(d.name === _),
                      strOpt2.map(d.name === _),
                      strOpt3.map(d.name === _),
                      strOpt4.map(d.name === _)
                    ).collect({case Some(criteria)  => criteria})
                      .reduceLeftOption(_ || _)
                      .getOrElse(true: Rep[Boolean])
                    // 等效于：d.name === strOpt1 || d.name === strOpt2 || d.name === strOpt3 || d.name === strOpt4
                  }.withFilter{
                    _.no < 50
                  }.sortBy(_.no.desc.nullsFirst)
                  .drop(start)//offset (start 从0开始)
                  .take(size)//limit(size)

    val action = q.result
    println(action.statements.head)
    val selects: Future[Unit] = db.run(action).map(_.foreach {
      case (id, no, name) =>
        println("  " + id + "\t" + no + "\t" + name)
    })
    Await.result(selects,Duration.Inf)
  }


  /***
    *关联，group by，聚合函数使用
    * */
  def query2() = {
    val q = for {
      (u, d) <- user joinLeft department on (_.deptid === _.id)
    } yield (u.deptid,u.name,u.gender,d.map(_.name))

    val q2 = q.groupBy(_._1)
    val q3 = q2.map{ case (deptid, ud) =>
      (deptid,ud.length,ud.map(_._1).avg)
    }
    println(q.result.statements.head)
    println(q3.result.statements.head)

    val selects = db.run(q3.result).map(_.foreach{
      case (deptid, count, avg) =>
        println("  " + deptid + "\t" + count + "\t" + avg)
    })
    Await.result(selects,Duration.Inf)
  }

  def query3() = {
    //val a = (x:(User,Rep.Some[Department]), y:Department) => if(x._2.map(_.id) == y.id) Rep.Some(true) else Rep.Some(false)
    val q = for {
      ((u, d),c) <- user joinLeft department on (_.deptid === _.id) joinLeft department on{ case((user,dept),d) => dept.map(_.id) === d.id }// _._2.map(_.id) === _.id
    } yield (u,d,c)

    println(q.result.statements.head)

    val selects = db.run(q.result).map(_.foreach{
      case (u,d,c) =>
        println(u+"--"+d+"--"+c)
    })
    Await.result(selects,Duration.Inf)
  }

  //使用sql查询
  def selectSql() = {
     val sql = sql"select * from department".as[(Long, Int, String)]
     val selects = db.run(sql).map(_.foreach{
       case (deptid, count, avg) =>
         println("  " + deptid + "\t" + count + "\t" + avg)
     })
    Await.result(selects,Duration.Inf)
  }

  //批量处理
  def batchQuery(detps: List[(Long, Int, String)]) = {
    //批量插入或更新，如果是更新返回None,否则返回id
    val qs = detps.map(x => (department returning department.map(_.id)).insertOrUpdate(x)) // 返回List(None, Some(261), Some(262))，如果是更新返回None,否则返回id
    val action = DBIO.sequence(qs) // DBIO.fold(a,0)((x, y) => x + y)
    val r = Await.result(db.run(action.transactionally), Duration.Inf)

    //合并id（有id直接从r取否则从detps取，即插入从返回结果取id，更新从原始传入参数取id）
    val r2: Seq[(Long, Any)] = detps.map(_._1) zip r.map(_.getOrElse(None)) //List((213,None), (0,263), (0,264))
    val r3: Seq[Long] = r2.map(x => if(x._2 == None) x._1.toLong else x._2.toString.toLong) // List(213, 265, 266)

    //根据id重新查询
    val action2 = department.filter(_.id inSetBind (r3))
    val r4: Seq[(Long, Int, String)] = Await.result(db.run(action2.result),Duration.Inf) // Vector((213,1,部门测试111), (267,1,部门测试2), (268,1,部门测试11))

    //结果封装成map返回
    val r5 = r4.collect{
      case (id, no, name) => (id , (id, no, name))
    }.toMap
    r5
  }

  def main(args: Array[String]): Unit = {

    //println(batchQuery(List((213,1,null),(0,11,"部门测试"),(0,12,"部门测试"))))

    //create((0,3,"部门测试"))

    //println(findById(213))

    // println(createAndGet((0,2,"部门测试")))

    // println(createOrUpdate((7,4,"部门测试")))

    //println(updateById(1,(1,"部门1")))

    //println(updateByName("部门测试X",(1,"部门1")))

    //println(deleteById(5))

    //findAll()

    //query(1,2)
    //val u: (Long, Long, String, Int, Long, String, String, Long) = (0L,111L,"赵六",1,System.currentTimeMillis(),"1369384995","wx3",System.currentTimeMillis())
    //createUser(u)

    //query2()

   // query3()

    //selectSql()

   /* val q = for{a <- department
                b <- user.filter(_.deptid === a.id)
            } yield b
    println(q.result.statements)*/



  }
}
