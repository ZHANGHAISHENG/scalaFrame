package sms

class SmsBean {
  private var sequence = 0 //唯一序列号

  private var prefix = "" //扩展端口

  private var phone = "" //手机号码

  private var content = "" //短信内容

  private var pubcode = ""
  private var smsType = 0

  def getSequence: Int = sequence

  def setSequence(sequence: Int): Unit = {
    this.sequence = sequence
  }

  def getPrefix: String = prefix

  def setPrefix(prefix: String): Unit = {
    this.prefix = prefix
  }

  def getContent: String = content

  def setContent(content: String): Unit = {
    this.content = content
  }

  def getPubcode: String = pubcode

  def setPubcode(pubcode: String): Unit = {
    this.pubcode = pubcode
  }

  def getSmsType: Int = smsType

  def setSmsType(smsType: Int): Unit = {
    this.smsType = smsType
  }

  def getPhone: String = phone

  def setPhone(phone: String): Unit = {
    this.phone = phone
  }
}
