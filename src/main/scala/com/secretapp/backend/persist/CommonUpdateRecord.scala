package com.secretapp.backend.persist

import com.newzly.phantom.query.ExecutableStatement
import com.secretapp.backend.data.message.{ update => updateProto }
import com.datastax.driver.core.{ ResultSet, Row, Session }
import com.newzly.phantom.Implicits._
import com.secretapp.backend.protocol.codecs.message.UpdateMessageCodec
import java.util.UUID
import collection.JavaConversions._
import scala.concurrent.Future
import scodec.bits._

case class StoredCommonUpdate(seq: Long, update: updateProto.UpdateMessage)

sealed class CommonUpdateRecord extends CassandraTable[CommonUpdateRecord, Entity[(Long, UUID), StoredCommonUpdate]]{
  override lazy val tableName = "common_updates"

  object pubkeyHash extends LongColumn(this) with PartitionKey[Long] {
    override lazy val name = "pubkey_hash"
  }
  object uuid extends TimeUUIDColumn(this) with PrimaryKey[UUID]
  object seq extends LongColumn(this)
  object userIds extends SetColumn[CommonUpdateRecord, Entity[(Long, UUID), StoredCommonUpdate], Long](this) {
    override lazy val name = "user_ids"
  }

  object updateId extends IntColumn(this) {
    override lazy val name = "update_id"
  }

  /**
    * UpdateMessage
    */
  object senderUID extends LongColumn(this) {
    override lazy val name = "sender_uid"
  }
  object destUID extends LongColumn(this) {
    override lazy val name = "dest_uid"
  }
  object mid extends IntColumn(this)
  object keyHash extends LongColumn(this) {
    override lazy val name = "key_hash"
  }
  object useAesKey extends BooleanColumn(this) {
    override lazy val name = "use_aes_key"
  }
  // TODO: Blob type when phanton implements its support in upcoming release
  object aesKeyHex extends OptionalBigIntColumn(this) {
    override lazy val name = "aes_key_hex"
  }
  object message extends BigIntColumn(this)

  override def fromRow(row: Row): Entity[(Long, UUID), StoredCommonUpdate] = {
    updateId(row) match {
      case 1L =>
        Entity((pubkeyHash(row), uuid(row)),
          StoredCommonUpdate(
            seq(row),
            updateProto.Message(senderUID(row), destUID(row), mid(row), keyHash(row), useAesKey(row),
              aesKeyHex(row) map (x => BitVector(x.toByteArray)), BitVector(message(row).toByteArray))
          ))
    }

  }
}

object CommonUpdateRecord extends CommonUpdateRecord with DBConnector {
  //import com.datastax.driver.core.querybuilder._
  //import com.newzly.phantom.query.QueryCondition

  // TODO: limit by size, not rows count
  def getDifference(pubkeyHash: Long, state: UUID, limit: Int = 500)(implicit session: Session): Future[Seq[Entity[(Long, UUID), StoredCommonUpdate]]] = {
    //select.where(c => QueryCondition(QueryBuilder.gte(c.uuid.name, QueryBuilder.fcall("maxTimeuuid")))).limit(limit)
    //  .fetch

    CommonUpdateRecord.select.orderBy(_.uuid.asc).where(_.pubkeyHash eqs pubkeyHash).and(_.uuid gt state).limit(limit).fetch
  }

  def push(pubkeyHash: Long, seq: Long, update: updateProto.UpdateMessage)(implicit session: Session) = Future {
    update match {
      case updateProto.Message(senderUID, destUID, mid, keyHash, useAesKey, aesKey, message) =>
        val userIds: java.util.Set[Long] = Set(senderUID, destUID)
        session.execute("""
          INSERT INTO common_updates (
            pubkey_hash, uuid, seq, user_ids, update_id,
            sender_uid, dest_uid, mid, key_hash, use_aes_key, aes_key_hex, message
          )
          VALUES (?, now(), ?, ?, 1, ?, ?, ?, ?, ?, ?, ?)
        """,
          new java.lang.Long(pubkeyHash), new java.lang.Long(seq), userIds,
          new java.lang.Long(senderUID), new java.lang.Long(destUID), new java.lang.Integer(mid),
          new java.lang.Long(keyHash), new java.lang.Boolean(useAesKey),
          aesKey map (x => BigInt(x.toByteArray).underlying) getOrElse (null), BigInt(message.toByteArray).underlying)
    }
  }


}
