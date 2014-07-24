package com.secretapp.backend.persist

import com.datastax.driver.core.{ ResultSet, Row, Session }
import com.newzly.phantom.Implicits._
import com.secretapp.backend.data.Implicits._
import com.secretapp.backend.data._
import com.secretapp.backend.data.models._
import com.secretapp.backend.crypto.ec.PublicKey
import java.util.{ Date, UUID }
import scodec.bits.BitVector
import scala.concurrent.Future
import scala.math.BigInt
import scala.collection.immutable
import scalaz._
import Scalaz._
import scala.util.{ Success, Failure }

sealed class UserRecord extends CassandraTable[UserRecord, User] {
  override lazy val tableName = "users"

  object uid extends IntColumn(this) with PartitionKey[Int]
  object authId extends LongColumn(this) with PrimaryKey[Long] {
    override lazy val name = "auth_id"
  }
  object publicKeyHash extends LongColumn(this) {
    override lazy val name = "public_key_hash"
  }
  object publicKey extends BigIntColumn(this) {
    override lazy val name = "public_key"
  }
  object keyHashes extends SetColumn[UserRecord, User, Long](this) with StaticColumn[Set[Long]] {
    override lazy val name = "key_hashes"
  }
  object accessSalt extends StringColumn(this) with StaticColumn[String] {
    override lazy val name = "access_salt"
  }
  object phoneNumber extends LongColumn(this) with StaticColumn[Long] {
    override lazy val name = "phone_number"
  }
  object firstName extends StringColumn(this) with StaticColumn[String] {
    override lazy val name = "first_name"
  }
  object lastName extends OptionalStringColumn(this) with StaticColumn[Option[String]] {
    override lazy val name = "last_name"
  }
  object sex extends IntColumn(this) with StaticColumn[Int]

  override def fromRow(row: Row): User = {
    User(
      uid = uid(row),
      authId = authId(row),
      publicKeyHash = publicKeyHash(row),
      publicKey = BitVector(publicKey(row).toByteArray),
      keyHashes = keyHashes(row).toIndexedSeq,
      accessSalt = accessSalt(row),
      phoneNumber = phoneNumber(row),
      firstName = firstName(row),
      lastName = lastName(row),
      sex = intToSex(sex(row))
    )
  }
}

object UserRecord extends UserRecord with DBConnector {
  def insertEntityWithPhone(entity: User)(implicit session: Session): Future[ResultSet] = {
    val phone = Phone(number = entity.phoneNumber, userId = entity.uid, userAccessSalt = entity.accessSalt,
      userKeyHashes = immutable.Seq(entity.publicKeyHash), userFirstName = entity.firstName,
      userLastName = entity.lastName, userSex = sexToInt(entity.sex))

    insert.value(_.uid, entity.uid)
      .value(_.authId, entity.authId)
      .value(_.publicKeyHash, entity.publicKeyHash)
      .value(_.publicKey, BigInt(entity.publicKey.toByteArray))
      .value(_.keyHashes, Set(entity.publicKeyHash))
      .value(_.accessSalt, entity.accessSalt)
      .value(_.phoneNumber, entity.phoneNumber)
      .value(_.firstName, entity.firstName)
      .value(_.lastName, entity.lastName)
      .value(_.sex, sexToInt(entity.sex))
      .future().flatMap(_ => PhoneRecord.insertEntity(phone))
  }

  def insertPartEntity(uid: Int, authId: Long, publicKey: BitVector, phoneNumber: Long)(implicit session: Session): Future[ResultSet] = {
    val publicKeyHash = PublicKey.keyHash(publicKey)
    insert.value(_.uid, uid)
      .value(_.authId, authId)
      .value(_.publicKeyHash, publicKeyHash)
      .value(_.publicKey, BigInt(publicKey.toByteArray))
      .future().flatMap(_ => addKeyHash(uid, publicKeyHash, phoneNumber))
  }

  private def addKeyHash(uid: Int, publicKeyHash: Long, phoneNumber: Long)(implicit session: Session) = {
    update.where(_.uid eqs uid).modify(_.keyHashes add publicKeyHash).
      future().flatMap(_ => PhoneRecord.addKeyHash(phoneNumber, publicKeyHash))
  }

  def getEntities(uid: Int)(implicit session: Session): Future[Seq[User]] = {
    select.where(_.uid eqs uid).fetch()
  }

  def getEntity(uid: Int)(implicit session: Session): Future[Option[User]] = {
    select.where(_.uid eqs uid).one()
  }

  def getEntity(uid: Int, authId: Long)(implicit session: Session): Future[Option[User]] = {
    select.where(_.uid eqs uid).and(_.authId eqs authId).one()
  }
}
