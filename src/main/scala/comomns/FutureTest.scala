package comomns


import io.circe.generic.auto._
import io.circe.syntax._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits._
import scala.util.{Failure, Success}

object FutureTest {

  def fun1(): Future[String] = Future {
    Thread.sleep(3000)
    "fun1"
  }

  def fun2(): Future[String] = Future {
    Thread.sleep(3000)
    "fun2"
  }

  def fun3(): Future[String] = Future {
    Thread.sleep(3000)
    "fun3"
  }

  //case class saveReqParam(friendOpendId: String, encryptedData: String, iv: String)
  def main(args: Array[String]): Unit = {

    //val r = saveReqParam("","","")
    //println(r.asJson)

    val start = System.currentTimeMillis()
    val f1 = fun1()
    val f2 = fun2()
    val f3 = fun3()

    println("-----")
    val f = for{
      a <- fun1()
      b <- fun2()
      c <- fun3()
    } yield (a, b, c)
    println("-----")

    //Thread.sleep(4000)
    val end = System.currentTimeMillis()
    println(end -start)

    f.onComplete{
      case Success(r) => println(r)
      case _ => println("fail")
    }
  }

}
