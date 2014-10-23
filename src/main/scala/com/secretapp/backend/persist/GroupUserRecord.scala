package com.secretapp.backend.persist

import com.datastax.driver.core.{ ResultSet, Row, Session }
import com.websudos.phantom.Implicits._
import com.secretapp.backend.data.Implicits._
import com.secretapp.backend.data.models._
import com.secretapp.backend.data.types._
import scodec.bits.BitVector
import scala.concurrent.Future
import scala.collection.immutable
import scalaz._
import Scalaz._

sealed class GroupUserRecord extends CassandraTable[GroupUserRecord, Int] {
  override lazy val tableName = "group_group_users"

  object groupId extends IntColumn(this) with PartitionKey[Int] {
    override lazy val name = "group_id"
  }
  object userId extends IntColumn(this) with PrimaryKey[Int] {
    override lazy val name = "user_id"
  }
  object keyHashes extends SetColumn[GroupUserRecord, Int, Long](this) {
    override lazy val name = "key_hashes"
  }

  override def fromRow(row: Row): Int = {
    userId(row)
  }
}

object GroupUserRecord extends GroupUserRecord with DBConnector {
  def addUser(groupId: Int, userId: Int)(implicit session: Session): Future[ResultSet] = {
    insert
      .value(_.groupId, groupId).value(_.userId, userId).future()
  }

  def addUser(groupId: Int, userId: Int, keyHashes: Set[Long])(implicit session: Session): Future[ResultSet \/ ResultSet] = {
    select(_.userId).where(_.groupId eqs groupId).and(_.userId eqs userId).one() flatMap {
      case Some(_) =>
        update
          .where(_.groupId eqs groupId).and(_.userId eqs userId).modify(_.keyHashes addAll keyHashes).future() map (_.left)
      case None =>
        insert
          .value(_.groupId, groupId).value(_.userId, userId).value(_.keyHashes, keyHashes).future() map (_.right)
    }
  }

  def removeUser(groupId: Int, userId: Int)(implicit session: Session): Future[ResultSet] = {
    delete.where(_.groupId eqs groupId).and(_.userId eqs userId).future()
  }

  def getUsers(groupId: Int)(implicit session: Session): Future[Seq[Int]] = {
    select.where(_.groupId eqs groupId).fetch()
  }

  def getUsersWithKeyHashes(groupId: Int)(implicit session: Session): Future[Seq[(Int, Set[Long])]] = {
    select(_.userId, _.keyHashes).where(_.groupId eqs groupId).fetch()
  }

  def addUserKeyHash(groupId: Int, userId: Int, keyHash: Long)(implicit session: Session): Future[ResultSet] = {
    update.where(_.groupId eqs groupId).and(_.userId eqs userId).modify(_.keyHashes add keyHash).future()
  }

  def removeUserKeyHash(userId: Int, keyHash: Long)(implicit session: Session): Future[Seq[ResultSet]] = {
    UserGroupsRecord.getGroups(userId) flatMap { groupIds =>
      Future.sequence(
        groupIds map { groupId =>
          update.where(_.groupId eqs groupId).and(_.userId eqs userId).modify(_.keyHashes remove keyHash).future()
        }
      )
    }
  }
}
