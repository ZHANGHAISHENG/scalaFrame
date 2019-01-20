package comomns

import akka.NotUsed

import scala.concurrent.duration._
import akka.testkit.AkkaSpec
import akka.stream.scaladsl._
import akka.stream._

import scala.concurrent.Future
import akka.testkit.TestProbe
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props, Status}
import com.typesafe.config.ConfigFactory
import akka.util.Timeout

import scala.concurrent.ExecutionContext
import java.util.concurrent.atomic.AtomicInteger

import akka.stream.scaladsl.Flow
import akka.Done
import akka.actor.Status.Status
import akka.stream.QueueOfferResult.{Dropped, Enqueued}

object Test3 extends App {
  import TwitterStreamQuickstartDocSpec._
  implicit val materializer = ActorMaterializer()
  implicit def system: ActorSystem = ActorSystem("test")

  final case class Email(to: String, title: String, body: String)
  final case class TextMessage(to: String, body: String)

  class AddressSystem {
    //#email-address-lookup
    def lookupEmail(handle: String): Future[Option[String]] =
    //#email-address-lookup
      Future.successful(Some(handle + "@somewhere.com"))

    //#phone-lookup
    def lookupPhoneNumber(handle: String): Future[Option[String]] =
    //#phone-lookup
      Future.successful(Some(handle.hashCode.toString))
  }
  class EmailServer(probe: ActorRef) {
    //#email-server-send
    def send(email: Email): Future[Unit] = {
      // ...
      //#email-server-send
      probe ! email.to
      Future.successful(())
      //#email-server-send
    }
    //#email-server-send
  }


  val probe = TestProbe()
  val addressSystem = new AddressSystem
  val emailServer = new EmailServer(probe.ref)

  //#tweet-authors
  val authors: Source[Author, NotUsed] =
    tweets
      .filter(_.hashtags.contains(akkaTag))
      .map(_.author)
  //#tweet-authors

  //#email-addresses-mapAsync
  val emailAddresses: Source[String, NotUsed] =
    authors
      .mapAsync(4)(author ⇒ addressSystem.lookupEmail(author.handle))
      .collect { case Some(emailAddress) ⇒ emailAddress }
  //#email-addresses-mapAsync

  //#send-emails
  val sendEmails: RunnableGraph[NotUsed] =
    emailAddresses
      .mapAsync(4)(address ⇒ {
        emailServer.send(
          Email(to = address, title = "Akka", body = "I like your tweet"))
      })
      .to(Sink.ignore)

  sendEmails.run()
  //#send-emails

  probe.expectMsg("rolandkuhn@somewhere.com")
  probe.expectMsg("patriknw@somewhere.com")
  probe.expectMsg("bantonsson@somewhere.com")
  probe.expectMsg("drewhk@somewhere.com")
  probe.expectMsg("ktosopl@somewhere.com")
  probe.expectMsg("mmartynas@somewhere.com")
  probe.expectMsg("akkateam@somewhere.com")

}
