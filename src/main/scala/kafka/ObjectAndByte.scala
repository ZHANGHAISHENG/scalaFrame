package kafka

//object myObj extends ObjectAndByte[User]
trait ObjectAndByte[T] {
  import java.io.ByteArrayInputStream
  import java.io.ByteArrayOutputStream
  import java.io.IOException
  import java.io.ObjectInputStream
  import java.io.ObjectOutputStream
  /**
    * 对象转数组
    * @param obj
    * @return
    */
  def toByteArray(obj: T): Array[Byte] = {
    var bytes:Array[Byte] = Array()
    val bos = new ByteArrayOutputStream
    try {
      val oos = new ObjectOutputStream(bos)
      oos.writeObject(obj)
      oos.flush()
      bytes = bos.toByteArray
      oos.close()
      bos.close()
    } catch {
      case ex: IOException =>
        ex.printStackTrace()
    }
    bytes
  }

  /**
    * 数组转对象
    *
    * @param bytes
    * @return
    */
  def toObject(bytes: Array[Byte]): T = {
      val bis = new ByteArrayInputStream(bytes)
      val ois = new ObjectInputStream(bis)
      val obj = ois.readObject.asInstanceOf[T]
      ois.close()
      bis.close()
      obj
  }

  /*def main(args: Array[String]): Unit = {
     val a = myObj.toByteArray(User("1","zhs"))
     val b = myObj.toObject(a)
    println(b)
  }*/

}
