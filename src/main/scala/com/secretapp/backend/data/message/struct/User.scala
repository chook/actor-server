package com.secretapp.backend.data.message.struct

import scala.collection.immutable
import scala.language.implicitConversions
import com.secretapp.backend.data.types
import com.secretapp.backend.data.message.ProtobufMessage
import com.secretapp.{ proto => protobuf }
import scalaz._
import Scalaz._

case class User(uid: Int,
                accessHash: Long,
                firstName: String,
                lastName: Option[String],
                sex: Option[types.Sex],
                keyHashes: immutable.Set[Long],
                phoneNumber: Long) extends ProtobufMessage
{
  def toProto = protobuf.User(uid, accessHash, firstName, lastName, sex.flatMap(_.toProto.some),
    keyHashes.toIndexedSeq, phoneNumber)
}

object User {
  def fromProto(u: protobuf.User): User = u match {
    case protobuf.User(uid, accessHash, firstName, lastName, sex, keyHashes, phoneNumber) =>
      User(uid, accessHash, firstName, lastName, sex.flatMap(types.Sex.fromProto(_).some), keyHashes.toSet, phoneNumber)
  }
}
