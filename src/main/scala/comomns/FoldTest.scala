package comomns

object FoldTest {

  def main(args: Array[String]): Unit = {

    val a = Some("a")
    println(a.fold(false)(_.trim.nonEmpty))

  }

}


