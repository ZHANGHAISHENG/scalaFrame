package slick

import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class UserDaoSpec extends WordSpecLike
                  with BeforeAndAfterAll
                  with Matchers {

  "first test" in {
    val r = UserDao.findById(1)
    r shouldEqual(1, "张三")
  }

}
