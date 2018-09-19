package sms

import com.sms.impl.Queue

object SenderTest extends  App {
  final case class SmsRequest(content: String, phone: String)
  /*val sender = new SmsMsgSend

  sender.start

  Thread.sleep(3000)

  sender.checkFee()*/

  /*val logger = LoggerFactory.getLogger(classOf[SmsMsgSend])
  logger.info("hello")*/

  /*val sendQueue = new Queue
  sendQueue.push(SmsRequest("开心爱答题注册验证码", "13560742265"))
  val obj = sendQueue.pop().asInstanceOf[SmsRequest]
  println(obj)*/

}