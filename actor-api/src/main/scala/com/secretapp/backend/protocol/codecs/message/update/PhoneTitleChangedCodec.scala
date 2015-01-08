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

object PhoneTitleChangedCodec extends Codec[PhoneTitleChanged] with utils.ProtobufCodec {
  def encode(u: PhoneTitleChanged) = {
    val boxed = protobuf.UpdatePhoneTitleChanged(u.phoneId, u.title)
    encodeToBitVector(boxed)
  }

  def decode(buf: BitVector) = {
    decodeProtobuf(protobuf.UpdatePhoneTitleChanged.parseFrom(buf.toByteArray)) {
      case Success(
        protobuf.UpdatePhoneTitleChanged(
          phoneId, title
        )
      ) =>
        PhoneTitleChanged(
          phoneId, title
        )
    }
  }
}
