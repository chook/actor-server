package com.secretapp.backend.data.message.rpc.update

import com.secretapp.backend.data.message.rpc._
import java.util.UUID

case class ResponseSeq(seq: Int, state: Option[UUID]) extends RpcResponseMessage

object ResponseSeq extends RpcResponseMessageObject {
  val responseType = 0x48
}