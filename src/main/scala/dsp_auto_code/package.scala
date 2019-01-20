package object dsp_auto_code {

  case class EnumField(name: String = "", value: Int = 0, desc: String = "")

  case class Enum(name: String = "", fields: Seq[EnumField] = Seq.empty)

  case class MsgField(name: String = "", fieldType: String = "", optionaled: Boolean = false, listed: Boolean = false, no: Int = 0, desc: String = "")

  case class Message(name: String = "", fields: Seq[MsgField] = Seq.empty)

  case class Proto(syntax: String = "", pkg: String = "", imports: Seq[String] = Seq.empty, enums: Seq[Enum] = Seq.empty, msgs: Seq[Message] = Seq.empty)

  sealed abstract  class NodeType

  final case object UnknownType extends NodeType

  final case object EnumType extends NodeType

  final case object MessageType extends NodeType

  case class Node(nodeType: NodeType = UnknownType, lines: Seq[String] = Seq.empty)

}
