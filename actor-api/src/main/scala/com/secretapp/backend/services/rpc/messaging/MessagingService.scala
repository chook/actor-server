package com.secretapp.backend.services.rpc.messaging

import akka.actor._
import akka.pattern.{ ask, pipe }
import akka.util.Timeout
import com.datastax.driver.core.{ Session => CSession }
import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap
import com.secretapp.backend.api.ApiBrokerService
import com.secretapp.backend.api.rpc.RpcProtocol
import com.secretapp.backend.data.message.rpc.messaging._
import com.secretapp.backend.data.message.rpc.history._
import com.secretapp.backend.data.message.rpc._
import com.secretapp.backend.models.User
import com.secretapp.backend.persist
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Success
import scalaz.Scalaz._
import scalaz._

trait MessagingService {
  this: ApiBrokerService =>

  import context.dispatcher

  lazy val messagingHandler = context.actorOf(
    Props(
      new Handler(
        updatesBrokerRegion,
        socialBrokerRegion,
        singletons.dialogManagerRegion,
        fileRecord,
        currentUser.get
      )
    ), "messaging")

  val randomIds = new ConcurrentLinkedHashMap.Builder[Long, RpcResponse]
    .initialCapacity(10).maximumWeightedCapacity(100).build

  def handleRpcMessaging: PartialFunction[RpcRequestMessage, \/[Throwable, Future[RpcResponse]]] = {
    case r: RequestWithRandomId =>
      authorizedRequest {
        withUniqRandomId(r.randomId)(ask(messagingHandler, RpcProtocol.Request(r)).mapTo[RpcResponse])
      }
    case r @ (
      _: RequestClearChat
        | _: RequestDeleteChat
        | _: RequestCreateGroup
        | _: RequestDeleteGroup
        | _: RequestEditGroupTitle
        | _: RequestInviteUsers
        | _: RequestLeaveGroup
        | _: RequestRemoveUsers
        | _: RequestEditGroupAvatar
        | _: RequestRemoveGroupAvatar
        | _: RequestMessageDelete
        | _: RequestMessageRead
        | _: RequestMessageReceived
        | _: RequestEncryptedRead
        | _: RequestEncryptedReceived
        | _: RequestMessageDelete
        | _: RequestLoadHistory
        | _: RequestLoadDialogs
    ) => authorizedRequest {
        ask(messagingHandler, RpcProtocol.Request(r)).mapTo[RpcResponse]
      }
  }

  private def withUniqRandomId(randomId: Long)(f: => Future[RpcResponse]): Future[RpcResponse] = {
    Option(randomIds.get(randomId)) match {
      case Some(resFuture) =>
        Future.successful(resFuture)
      case None =>
        val res = f recover {
          case err =>
            Error(400, "INTERNAL_SERVER_ERROR", err.getMessage, true)
        }

        res andThen {
          case Success(resp: Ok) =>
            randomIds.put(randomId, resp)
        }
    }
  }
}