package com.secretapp.backend.persist

import akka.dispatch.Dispatcher
import com.datastax.driver.core.ResultSet
import com.datastax.driver.core.Session
import com.secretapp.backend.data.Implicits._
import com.websudos.phantom.Implicits._
import com.websudos.phantom.keys.ClusteringOrder
import java.nio.ByteBuffer
import java.util.concurrent.Executor
import play.api.libs.iteratee._
import scala.annotation.tailrec
import scala.concurrent.{ExecutionContext, Future}
import scodec.bits._

case class File(fileId: Int, sourceBlocksCount: Int)

class FileRecord(implicit session: Session, context: ExecutionContext with Executor) {
  private lazy val blockRecord = new FileBlockRecord
  private lazy val sourceBlockRecord = new FileSourceBlockRecord

  def createTable(session: Session): Future[Unit] = {
    val b = blockRecord.createTable(session)
    val sb = sourceBlockRecord.createTable(session)
    for {
      _ <- b
      _ <- sb
    } yield Unit
  }

  def truncateTable(session: Session): Future[Unit] = {
    val b = blockRecord.truncateTable(session)
    val sb = sourceBlockRecord.truncateTable(session)
    for {
      _ <- b
      _ <- sb
    } yield Unit
  }

  def createFile(id: Int, accessSalt: String): Future[ResultSet] = {
    blockRecord.insert.value(_.fileId, id).value(_.accessSalt, accessSalt).future()
  }

  def write(id: Int, offset: Int, bytes: Array[Byte], isSourceBlock: Boolean = true) = {
    val f = blockRecord.write(id, offset, bytes)
    if (isSourceBlock) {
      f onSuccess {
        case _ =>
          sourceBlockRecord.insertEntity(FileSourceBlock(id, offset))
      }
    }
    f
  }

  def getFileAccessSalt(fileId: Int): Future[String] = {
    blockRecord.select(_.accessSalt).where(_.fileId eqs fileId).one() map {
      case Some(salt) => salt
      case None => throw new Exception("File does not exists")
    }
  }

  def getFile(fileId: Int, offset: Int, limit: Int): Future[Array[Byte]] = {
    for {
      blocks <- blockRecord.getFileBlocks(fileId, offset, limit)
    } yield {
      val bytes = blocks.foldLeft(Vector.empty[Byte])((a, b) => a ++ BitVector(b).toByteArray)
      bytes.drop(offset % FileBlockRecord.blockSize).take(limit).toArray
    }
  }

  def blocksByFileId(fileId: Int) = blockRecord.blocksByFileId(fileId)

  def countSourceBlocks(fileId: Int) = sourceBlockRecord.countBlocks(fileId)
}