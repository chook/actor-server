package com.secretapp.backend

import java.net.InetSocketAddress
import java.security.Security
import akka.actor.{ ActorSystem, Props }
import akka.io.{ IO, Tcp }
import akka.kernel.Bootable
import Tcp._
import org.bouncycastle.jce.provider.BouncyCastleProvider
import scala.util.Try
import com.secretapp.backend.persist.DBConnector
import api.Server

class ApiKernel extends Bootable {
  implicit val system = ActorSystem("secret-api-server")

  import Configuration._

  def startup = {
    Security.addProvider(new BouncyCastleProvider())
    val port = Try(serverConfig.getInt("port")).getOrElse(8080)
    val hostname = Try(serverConfig.getString("hostname")).getOrElse("0.0.0.0")
    val session = DBConnector.session
    DBConnector.createTables(session)
    implicit val service = system.actorOf(Props(new Server(session)), "api-service")
    IO(Tcp) ! Bind(service, new InetSocketAddress(hostname, port))
  }

  def shutdown = {
    system.shutdown()
  }
}
