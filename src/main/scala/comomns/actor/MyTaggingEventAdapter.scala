package comomns.actor

import akka.persistence.journal.WriteEventAdapter
import akka.persistence.journal.Tagged

class MyTaggingEventAdapter extends WriteEventAdapter {
  val colors = Set("green", "black", "blue")
  override def toJournal(event: Any): Any = event match {
    case s: String ⇒
      var tags = colors.foldLeft(Set.empty[String]) { (acc, c) ⇒
        if (s.contains(c)) acc + c else acc
      }
      if (tags.isEmpty) event
      else Tagged(event, tags)
    case _ ⇒ event
  }

  override def manifest(event: Any): String = ""
}
