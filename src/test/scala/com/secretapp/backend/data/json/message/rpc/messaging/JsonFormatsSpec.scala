package com.secretapp.backend.data.json.message.rpc.messaging

import com.secretapp.backend.data.json._
import com.secretapp.backend.data.json.JsonSpec
import com.secretapp.backend.data.json.JsonSpec._
import com.secretapp.backend.data.message.rpc.RpcRequestMessage
import com.secretapp.backend.data.message.rpc.messaging._
import play.api.libs.json._
import JsonFormatsSpec._
import scala.collection.immutable
import scalaz._
import Scalaz._
import scodec.bits._

class JsonFormatsSpec extends JsonSpec {

  "(de)serializer" should {

    "(de)serialize EncryptedKey" in {
      val (v, j) = genEncryptedKey
      testToAndFromJson(j, v)
    }

    "(de)serialize EncryptedMessage" in {
      val (v, j) = genEncryptedMessage
      testToAndFromJson(j, v)
    }

  }

  "RpcRequestMessage (de)serializer" should {

    "(de)serialize RequestSendMessage" in {
      val (encryptedMessage1, encryptedMessage1Json) = genEncryptedMessage
      val (encryptedMessage2, encryptedMessage2Json) = genEncryptedMessage
      val v = RequestSendMessage(1, 2, 3, encryptedMessage1, encryptedMessage2.some)
      val j = withHeader(RequestSendMessage.requestType)(
        "uid" -> 1,
        "accessHash" -> "2",
        "randomId" -> "3",
        "message" -> encryptedMessage1Json,
        "selfMessage" -> encryptedMessage2Json
      )
      testToAndFromJson[RpcRequestMessage](j, v)
    }

    "(de)serialize RequestMessageRead" in {
      val v = RequestMessageRead(1, 2, 3)
      val j = withHeader(RequestMessageRead.requestType)(
        "uid" -> 1,
        "randomId" -> "2",
        "accessHash" -> "3"
      )
      testToAndFromJson[RpcRequestMessage](j, v)
    }

    "(de)serialize RequestMessageReceived" in {
      val v = RequestMessageReceived(1, 2, 3)
      val j = withHeader(RequestMessageReceived.requestType)(
        "uid" -> 1,
        "randomId" -> "2",
        "accessHash" -> "3"
      )
      testToAndFromJson[RpcRequestMessage](j, v)
    }

    "(de)serialize RequestSendGroupMessage" in {
      val keyHash = hex"ac1d".bits
      val message = hex"123456abcdf".bits
      val v = RequestSendGroupMessage(1, 2, 3, keyHash, message)
      val j = withHeader(RequestSendGroupMessage.requestType)(
        "chatId" -> 1,
        "accessHash" -> "2",
        "randomId" -> "3",
        "keyHash" -> keyHash.toBase64,
        "message" -> message.toBase64
      )
      testToAndFromJson[RpcRequestMessage](j, v)
    }
  }

}

object JsonFormatsSpec {

  def genEncryptedKey = {
    val (bitVector, bitVectorJson) = genBitVector

    (
      EncryptedKey(1, bitVector),
      Json.obj(
        "keyHash"         -> "1",
        "aesEncryptedKey" -> bitVectorJson
      )
    )
  }

  def genEncryptedMessage = {
    val (bitVector, bitVectorJson) = genBitVector
    val (encryptedKey, encryptedKeyJson) = genEncryptedKey

    (
      EncryptedMessage(bitVector, immutable.Seq(encryptedKey)),
      Json.obj(
        "message" -> bitVectorJson,
        "keys"    -> Json.arr(encryptedKeyJson)
      )
    )
  }

}
