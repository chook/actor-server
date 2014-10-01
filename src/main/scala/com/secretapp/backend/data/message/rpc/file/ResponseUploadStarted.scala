package com.secretapp.backend.data.message.rpc.file

import com.secretapp.backend.data.message.rpc._

case class ResponseUploadStarted(config: UploadConfig) extends RpcResponseMessage {
  val header = ResponseUploadStarted.responseType
}

object ResponseUploadStarted extends RpcResponseMessageObject {
  val responseType = 0x13
}