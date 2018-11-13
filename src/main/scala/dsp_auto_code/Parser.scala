package dsp_auto_code

import java.util.regex.Pattern
import io.circe.syntax._
import io.circe.generic.auto._

object Parser extends App {

  def parse(filePath: String): Proto = {
    val lines = FileUtil.read(filePath).map(_.trim).filter(_.nonEmpty)
    parse(lines)
  }

  def parse(lines: Seq[String]): Proto = {
    var proto: Proto = Proto()
    var node: Node = Node()
    lines.foreach {x =>
       if(x.startsWith("syntax")) {
         proto = proto.copy(syntax = parseSyntax(x))
       } else if(x.startsWith("package")) {
         proto = proto.copy(pkg = parsePackage(x))
       } else if(x.startsWith("import")) {
         proto = proto.copy(imports = proto.imports :+ parseImport(x))
       } else if(x.startsWith("enum")) {
         node = Node(nodeType = EnumType, lines = List(x))
       } else if(x.startsWith("message")) {
         node = Node(nodeType = MessageType, lines = List(x))
       } else if(x.startsWith("}")) {
         proto = parseNode(proto, node)
       }else {
         node = node.copy(lines = node.lines :+ x)
       }
    }
    proto
  }

  def parseNode(proto: Proto, node: Node): Proto = {
    node.nodeType match {
      case EnumType =>
        proto.copy(enums = proto.enums :+ parseEnum(node.lines))
      case MessageType =>
        proto.copy(msgs = proto.msgs :+ parseMessage(node.lines))
      case _ =>
        proto
    }
  }

  def parseSyntax(x: String): String = {
    val regEx = "[\\w_\\-\\.]+\\s*=\\s*\"([\\w_\\-\\.]+)\"\\s*;"
    patternMatch(x, regEx).headOption.getOrElse("")
  }

  def parsePackage(x: String): String = {
    val regEx = "package\\s+([\\w_\\-\\.]+)\\s*;"
    patternMatch(x, regEx).headOption.getOrElse("")
  }

  def parseImport(x: String): String = {
    val regEx = "import\\s+\"([\\w_\\-\\./]+)\"\\s*;"
    patternMatch(x, regEx).headOption.getOrElse("")
  }

  def parseEnum(x: Seq[String]): Enum = {
    var e = Enum()
    x.foreach {s =>
      if(s.startsWith("enum")) {
        val regEx = "enum\\s+([\\w_\\-\\.]+)\\s*\\{"
        val name = patternMatch(s, regEx).headOption.getOrElse("")
        e = e.copy(name = name)
      } else {
        val regEx = "([\\w_\\-\\.]+)\\s*=\\s*(\\d+)\\s*;"
        val g = patternMatch(s, regEx)
        val f = EnumField(name = g.head, value = g(1).toInt)
        e = e.copy(fields = e.fields :+ f)
      }
    }
    e
  }

  def parseMessage(x: Seq[String]): Message = {
    var msg = Message()
    x.foreach {s =>
      if(s.startsWith("message")) {
        val regEx = "message\\s+([\\w_\\-\\.]+)\\s*\\{"
        val name = patternMatch(s, regEx).headOption.getOrElse("")
        msg = msg.copy(name = name)
      } else {
        val regEx = "(repeated\\s+)?([\\w_\\-\\.]+)\\s+([\\w_\\-\\.]+)\\s*=\\s*(\\d+)\\s*;"
        val g = patternMatch(s, regEx)
        val listed = if(g.head == null || g.head.isEmpty) false else true
        val b = optionaled(g(1))
        val f = MsgField(name = g(2), fieldType = g(1), no = g(3).toInt, listed = listed, optionaled = b)
        msg = msg.copy(fields = msg.fields :+ f)
      }
    }
    msg
  }

  def optionaled(s: String): Boolean = {
    val list = List("google.protobuf.StringValue"
    , "google.protobuf.DoubleValue"
    , "google.protobuf.Int32Value"
    , "google.protobuf.StringValue"
    , "google.protobuf.StringValue"
    , "google.protobuf.Int32Value"
    , "google.protobuf.Int32Value")
    list.contains(s)
  }

  def patternMatch(x: String, regEx: String): Seq[String] = {
    val pat = Pattern.compile(regEx)
    val mat = pat.matcher(x)
    if (mat.find) {
      (1 to mat.groupCount()).map(mat.group)
    } else {
      List.empty
    }
  }

  /*val regEx = "(repeated\\s+)?([\\w_\\-\\.]+)\\s+([\\w_\\-\\.]+)\\s*=\\s*(\\d+)\\s*;"
  val g = patternMatch("google.protobuf.StringValue  videoUrl    = 1;", regEx)
  println(g.head)*/

  /*val p = parse("/home/hamlt/work/projects/adx-edge/idl/src/main/protobuf/adapter/lingpeng_response.proto")
  println(p.asJson)*/

}
