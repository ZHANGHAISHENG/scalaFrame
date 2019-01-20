package dsp_auto_code

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths, StandardOpenOption}
import scala.collection.JavaConverters._

object FileUtil {

  def write(filePath:String, contents: String) = {
    Files.write(Paths.get(filePath), contents.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE)
  }

  def read(filePath: String): Seq[String] = {
    Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8).asScala
  }

}
