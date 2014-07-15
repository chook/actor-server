package com.secretapp.backend.data.transport

import com.secretapp.backend.data.message.TransportMessage

case class Package(authId: Long, sessionId: Long, messageBox: MessageBox) {
  def replyWith(messageId: Long, tm: TransportMessage): Package = {
    Package(authId, sessionId, MessageBox(messageId, tm))
  }
}
