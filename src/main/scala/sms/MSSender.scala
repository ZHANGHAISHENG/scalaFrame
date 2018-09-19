package sms

import com.sms.impl.{GSTEngineFactory, Queue}

object MSSender extends App {

  private val smsAccount = "zzcmdati"
  private val smsPwd = "Zzcm2018"
  private val warnCoun = 500
  val fac: GSTEngineFactory = GSTEngineFactory.getInstance(this.smsAccount, this.smsPwd)
  val sendQueue = new Queue

  def start() = {
    if (fac.getConn == null) {
      if (fac.loginEngin == 0) {
        println("-[三三得玖--[爱头条][zhzhong]]用户登陆成功连接!")
        //checkFee() //检查余额
      } else {
        println("-[三三得玖--[爱头条][zhzhong]]用户登陆连接失败!")
      }
    }
  }

  def send() = {
    if (!sendQueue.isEmpty) {
      val smsBean = sendQueue.pop.asInstanceOf[SmsBean]
      if (smsBean.getPhone != null && !("" == smsBean.getPhone)) { //	fac.setSequenceSMS(smsBean.getSequence()); // 设置状态报告应答的标志匹配符
        val flag = fac.sendSigleSMS(smsBean.getPrefix, smsBean.getPhone, smsBean.getContent)
        if (flag == 0) {
          println("【单发短信】提交中!")
        }
        else {
          println("三三得玖[[爱头条][zhzhong]]发送短信 提交失败！短信目的地：[" + smsBean.getPhone + "],短信内容：[" + smsBean.getContent + "]")
        }
      }
    }
  }


}
