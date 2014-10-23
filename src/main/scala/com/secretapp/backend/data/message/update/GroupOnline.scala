package com.secretapp.backend.data.message.update

case class GroupOnline(groupId: Int, count: Int) extends WeakUpdateMessage {
  val weakUpdateHeader = GroupOnline.weakUpdateHeader
}

object GroupOnline extends WeakUpdateMessageObject {
  val weakUpdateHeader = 0x21
}
