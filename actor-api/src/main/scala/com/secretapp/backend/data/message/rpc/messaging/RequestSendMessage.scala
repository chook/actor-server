package com.secretapp.backend.data.message.rpc.messaging

import com.secretapp.backend.data.message.rpc._
import com.secretapp.backend.data.message.struct

@SerialVersionUID(1L)
case class RequestSendMessage(peer: struct.OutPeer, randomId: Long, message: MessageContent) extends RpcRequestMessage {
  val header = RequestSendMessage.header
}

object RequestSendMessage extends RpcRequestMessageObject {
  val header = 0x5C
}
