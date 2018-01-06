package com.json

/**
  * 遍历修改json工具类
  */
object Optics {

  def main(args: Array[String]): Unit = {
    import io.circe._, io.circe.parser._

    val json: Json = parse("""
    {
      "order": {
        "customer": {
          "name": "Custy McCustomer",
          "contactDetails": {
            "address": "1 Fake Street, London, England",
            "phone": "0123-456-789"
          }
        },
        "items": [{
          "id": 123,
          "description": "banana",
          "quantity": 1
        }, {
          "id": 456,
          "description": "apple",
          "quantity": 2
        }],
        "total": 123.45
      }
    }
    """).getOrElse(Json.Null)

    //使用指针方式遍历单个值
    val phoneNum: Option[String] = json.hcursor.
      downField("order").
      downField("customer").
      downField("contactDetails").
      get[String]("phone").
      toOption
    println(phoneNum)

    //使用指针方式遍历多个值
    val items: Vector[Json] = json.hcursor.
      downField("order").
      downField("items").
      focus.
      flatMap(_.asArray).
      getOrElse(Vector.empty)
    val quantities: Vector[Int] = items.flatMap(_.hcursor.get[Int]("quantity").toOption)
    println(quantities)


    //使用optics方式遍历
    import io.circe.optics.JsonPath._
    val _phoneNum = root.order.customer.contactDetails.phone.string
    val phoneNum2: Option[String] = _phoneNum.getOption(json)
    println(phoneNum2)

    val items2: List[Int] =root.order.items.each.quantity.int.getAll(json)
    println(items2)

    //使用optics方式修改
    val doubleQuantities: Json => Json = root.order.items.each.quantity.int.modify(_ * 2)
    val modifiedJson = doubleQuantities(json)
    println(modifiedJson)
  }

}
