package cats

import cats.free.Trampoline
import cats.instances.vector._
import cats.instances.function._
import cats.syntax.traverse._
import io.circe._
import io.circe.parser.parse

object Test1 {
  private[this] def sc2cc(in: String): String = "_([a-z\\d])".r.replaceAllIn(in, _.group(1).toUpperCase)

  private[this] def transformObjectKeys(obj: JsonObject, f: String => String): JsonObject =
    JsonObject.fromIterable(
      obj.toList.map {
        case (k, v) => f(k) -> v
      }
    )

  /**延迟执行**/
  private[this] def transformKeys(json: Json, f: String => String): Trampoline[Json] =
    json.arrayOrObject(
      Trampoline.done(json),
      // _.traverse(j => Trampoline.suspend(transformKeys(j, f))).map(Json.fromValues),
      _.traverse(j => Trampoline.defer(transformKeys(j, f))).map(Json.fromValues),
      transformObjectKeys(_, f).traverse(obj => Trampoline.defer(transformKeys(obj, f))).map(Json.fromJsonObject)
    )

  def main(args: Array[String]): Unit = {
    case class Persion(name:String,age:Int)
    //val p: Persion = Persion("zhs_ls",10)

    val raw: String = """
      {
        "first_name": "zhs_ls",
        "age": 20
      }
      """
    val json: Json = parse(new String(raw)).right.getOrElse(Json.Null)

    println(transformKeys(json,sc2cc).run)
    //println(transformObjectKeys(json.asObject.get,sc2cc))

  }

}


