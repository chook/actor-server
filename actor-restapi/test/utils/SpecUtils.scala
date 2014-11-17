package utils

import org.specs2.mutable._
import play.api.http.Writeable
import play.api.libs.json.JsValue
import play.api.test._
import play.api.test.Helpers._
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import models.User
import com.secretapp.backend.models

case class RichFuture[A](f: Future[A]) {

  def sync: A = Await.result(f, Duration(10, SECONDS))

}

case class RichOption[A](o: Option[A]) {

  def defined(implicit s: Specification): A = {
    import s._
    o must beSome
    o.get
  }

}

trait SpecUtils { self: Specification =>

  implicit val s: Specification = this

  implicit def toRichFuture[A](f: Future[A]): RichFuture[A] =
    RichFuture(f)

  implicit def toRichOption[A](o: Option[A]): RichOption[A] =
    RichOption(o)

  def responseStatus[A](implicit r: FakeRequest[A], w: Writeable[A]): Int =
    status(route(r).get)

  def responseJson[A](implicit r: FakeRequest[A], w: Writeable[A]): JsValue =
    contentAsJson(route(r).get)

  def performRequest[A]()(implicit r: FakeRequest[A], w: Writeable[A]): Unit =
    responseStatus

  def createUser(u: User): User = persist.User.create(u).sync.defined

  def createAuthSmsCode(c: models.AuthSmsCode): models.AuthSmsCode =
    persist.AuthSmsCode.save(c).sync

}
