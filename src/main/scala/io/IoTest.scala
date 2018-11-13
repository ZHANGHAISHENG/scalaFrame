package io

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths, StandardOpenOption}
import scala.collection.JavaConverters._

object IoTest extends App {

  def _write(filePath:String, contents: String) = {
    Files.write(Paths.get(filePath), contents.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE)
  }

  def _read(filePath: String): String = {
    val list = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8).asScala
    list.filter(_.trim.nonEmpty).foreach(println)
    ""
  }

  val str = _read("/home/hamlt/work/projects/adx-edge/idl/src/main/protobuf/adapter/lingpeng_response.proto")
  //str.split("\n").toList.foreach(println)

}
