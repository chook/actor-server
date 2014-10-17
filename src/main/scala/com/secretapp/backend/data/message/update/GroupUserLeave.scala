package com.secretapp.backend.data.message.update

import com.secretapp.backend.data.message.struct.UserId
import scala.collection.immutable
import scodec.bits.BitVector

case class GroupUserLeave(
  chatId: Int,
  userId: Int
) extends SeqUpdateMessage {
  val seqUpdateHeader = GroupUserLeave.seqUpdateHeader

  def userIds: Set[Int] = Set(userId)
}

object GroupUserLeave extends SeqUpdateMessageObject {
  val seqUpdateHeader = 0x17
}