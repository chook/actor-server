package persist

import org.specs2.mutable._
import play.api.test._
import utils.{SpecUtils, CassandraSpecification, Gen}

class UserSpec extends Specification with CassandraSpecification with SpecUtils {

  val user = Gen.genUser

  "User.create" should {

    "return user with generated id" in new WithApplication {
      val u = createUser(user)
      u must_== user.copy(uid = u.uid)
    }

    "persist user" in new WithApplication {
      val id = createUser(user).uid
      User.byId(id).sync.defined must_== user.copy(uid = id)
    }

  }

  "User.remove" should {

    "remove user" in new WithApplication {
      val id = createUser(user).uid
      User.remove(id).sync
      User.byId(id).sync must beNone
    }

  }

  "User.list" should {

    "return count users" in new WithApplication {
      val users = Stream.continually(createUser(Gen.genUser)).take(6).toSet
      User.list(2).sync must haveLength(2)
    }

  }

}
