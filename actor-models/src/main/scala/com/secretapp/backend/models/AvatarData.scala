package com.secretapp.backend.models

import scala.collection.immutable

case class AvatarData(
  smallAvatarFileId: Option[Int] = None,
  smallAvatarFileHash: Option[Long] = None,
  smallAvatarFileSize: Option[Int] = None,
  largeAvatarFileId: Option[Int] = None,
  largeAvatarFileHash: Option[Long] = None,
  largeAvatarFileSize: Option[Int] = None,
  fullAvatarFileId: Option[Int] = None,
  fullAvatarFileHash: Option[Long] = None,
  fullAvatarFileSize: Option[Int] = None,
  fullAvatarWidth: Option[Int] = None,
  fullAvatarHeight: Option[Int] = None
) {
  lazy val smallAvatarImage =
    for (
      id <- smallAvatarFileId;
      hash <- smallAvatarFileHash;
      size <- smallAvatarFileSize
    ) yield AvatarImage(FileLocation(id, hash), 100, 100, size)

  lazy val largeAvatarImage =
    for (
      id <- largeAvatarFileId;
      hash <- largeAvatarFileHash;
      size <- largeAvatarFileSize
    ) yield AvatarImage(FileLocation(id, hash), 200, 200, size)

  lazy val fullAvatarImage =
    for (
      id <- fullAvatarFileId;
      hash <- fullAvatarFileHash;
      size <- fullAvatarFileSize;
      w <- fullAvatarWidth;
      h <- fullAvatarHeight
    ) yield AvatarImage(FileLocation(id, hash), w, h, size)

  lazy val avatar =
    if (immutable.Seq(smallAvatarImage, largeAvatarImage, fullAvatarImage).exists(_.isDefined))
      Some(Avatar(smallAvatarImage, largeAvatarImage, fullAvatarImage))
    else
      None
}