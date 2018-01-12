package slick

/** @author Stephen Samuel */

import java.util.concurrent.TimeUnit

import org.scalatest.{FlatSpec, OneInstancePerTest}

import scala.concurrent.Await
import scala.concurrent.duration._

// This test was added for clauses that show green when they were never executed.
class SimpleClassTest extends FlatSpec with OneInstancePerTest {

  "a SimpleClassTest" should "test 1" in {
    val result = Await.result(SimpleObject.method(true, true), Duration(1, TimeUnit.SECONDS))
    assert(Some(true) === result)
  }
}