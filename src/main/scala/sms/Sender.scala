package sms

import com.sms.impl.GSTEngineFactory
import com.sms.impl.Queue
import com.sms.pub.MyRunnable


object SmsMsgSend {
}

class SmsMsgSend() extends MyRunnable("开始启动短信发送线程=========") {
  private val smsAccount = "zzcmdati"
  private val smsPwd = "Zzcm2018"
  private val warnCoun = 500
  val fac: GSTEngineFactory = GSTEngineFactory.getInstance(this.smsAccount, this.smsPwd)
  val sendQueue = new Queue

  override def run(): Unit = {
    println(getName + " 线程已开启")
    bStop = false
    // 设置账号和密码
    while (!bStop) try {
      if (fac.getConn == null) {
        if (fac.loginEngin == 0) {
          println("-[三三得玖--[爱头条][zhzhong]]用户登陆成功连接!")
          checkFee() //检查余额
        } else {
          println("-[三三得玖--[爱头条][zhzhong]]用户登陆连接失败!")
        }
      }
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
      Thread.sleep(1000)
    } catch {
      case e: Throwable =>
        try {
          Thread.sleep(1000)
          println(getName + " 运行异常:" + e)
        } catch {
          case e1: InterruptedException =>
            e1.printStackTrace()
        }
    }
  }

  /**
    * 检查账号余额
    */
  def checkFee(): Unit = {
    if (fac != null && fac.getConn != null) { //获取用户当前余额  12.36=1236
      val fee = fac.showFeeTotal
      println("-[开心爱答题--[开心爱答题][zhzhong]]用户当前余额:" + fee)
      if (Integer.valueOf(fee) <= warnCoun) {
      // 余额少于200元的时候通知
      val msg = "-[开心爱答题--[开心爱答题][zhzhong]]用户当前余额少于" + warnCoun + "元"
      //总管
      val smsBean = new SmsBean
      smsBean.setPrefix("")
      smsBean.setPhone("13560742265")
      smsBean.setContent(msg)
      sendQueue.push(smsBean)
      }
    }
  }

  override def end(): Unit = {
  }
}
