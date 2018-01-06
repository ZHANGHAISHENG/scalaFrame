package slick

/**
  * 逆向生成table文件
  */
object AutoCode {

  def main(args: Array[String]): Unit = {
    val profile = "slick.jdbc.PostgresProfile"
    val jdbcDriver = "org.postgresql.Driver"
    val url = "jdbc:postgresql://localhost/sqltest"
    val outputFolder = "./src/main/scala"
    val pkg = "slick.tables"
    val user = "super"
    val password = "aszx"
    slick.codegen.SourceCodeGenerator.main(
      Array(profile, jdbcDriver, url, outputFolder, pkg, user, password)
    )


  }

}
