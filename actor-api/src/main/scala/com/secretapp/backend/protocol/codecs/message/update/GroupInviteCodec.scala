package com.secretapp.backend.protocol.codecs.message.update

import com.secretapp.backend.protocol.codecs._
import com.secretapp.backend.data.message.update._
import com.secretapp.backend.protocol.codecs.utils.protobuf._
import scodec.bits._
import scodec.Codec
import scodec.codecs._
import scalaz._
import Scalaz._
import scala.util.Success
import im.actor.messenger.{ api => protobuf }

object GroupInviteCodec extends Codec[GroupInvite] with utils.ProtobufCodec {
  def encode(u: GroupInvite) = {
    val boxed = protobuf.UpdateGroupInvite(u.groupId, u.inviteUid, u.date)
    encodeToBitVector(boxed)
  }

  def decode(buf: BitVector) = {
    decodeProtobuf(protobuf.UpdateGroupInvite.parseFrom(buf.toByteArray)) {
      case Success(protobuf.UpdateGroupInvite(groupId, inviteUid, date)) => GroupInvite(groupId, inviteUid, date)
    }
  }
}
