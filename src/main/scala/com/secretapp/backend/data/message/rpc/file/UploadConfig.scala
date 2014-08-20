package com.secretapp.backend.data.message.rpc.file

import com.secretapp.backend.data.message.ProtobufMessage
import com.secretapp.backend.protocol.codecs.utils.protobuf._
import com.secretapp.{ proto => protobuf }
import scodec.bits.BitVector

case class UploadConfig(serverData: BitVector) extends ProtobufMessage
{
  def toProto = protobuf.UploadConfig(serverData)
}

object UploadConfig {
  def fromProto(r: protobuf.UploadConfig): UploadConfig = r match {
    case protobuf.UploadConfig(serverData) => UploadConfig(serverData)
  }
}
