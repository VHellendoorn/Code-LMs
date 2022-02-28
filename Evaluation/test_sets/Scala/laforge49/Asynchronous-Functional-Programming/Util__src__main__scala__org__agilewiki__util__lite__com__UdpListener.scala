/*
 * Copyright 2011 Bill La Forge
 *
 * This file is part of AgileWiki and is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License (LGPL) as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 * or navigate to the following url http://www.gnu.org/licenses/lgpl-2.1.txt
 *
 * Note however that only Scala, Java and JavaScript files are being covered by LGPL.
 * All other files are covered by the Common Public License (CPL).
 * A copy of this license is also included and can be
 * found as well at http://www.opensource.org/licenses/cpl1.0.txt
 */
package org.agilewiki
package util
package lite
package com

import java.net.{DatagramPacket, DatagramSocket}
import scala.collection.mutable.HashMap

class UdpListener(localHostPort: HostPort, insideActor: LiteActor)
  extends LiteSrc {
  private var udpRunner = new Thread

  private def started = udpRunner.isAlive

  private var socket: DatagramSocket = null
  private var buffer: Array[Byte] = new Array[Byte](0)
  private var sc: Option[SystemContext] = None

  def systemContext: SystemContext = sc match {
    case None => null
    case Some(x) => x
  }

  def send(actor: LiteActor, messageContent: AnyRef) {
    val req = new LiteReqMsg(actor, null, null, messageContent, this)
    val reactor = actor.liteReactor
    reactor.request(req)
  }

  def receiveIncomingMessages = {
    try {
      do {
        val datagram = new DatagramPacket(buffer, buffer.length)
        socket.receive(datagram)
        receive(datagram)
      } while (true)
    } finally {
      socket.close
      socket = null
    }
  }

  def receive(packet: DatagramPacket) {
    val l = packet.getLength
    if (l == 0) return
    val liteManager = Udp(systemContext).liteManager
    val bytes = new Array[Byte](l)
    System.arraycopy(packet.getData, packet.getOffset, bytes, 0, l)
    val payload = new DataInputStack(bytes)
    val isReply = payload.readByte.asInstanceOf[Boolean] //messageType
    val srcServer = payload.readUTF //sender server
    val msgUuid = payload.readUTF //message UUID
    val dstServer = payload.readUTF //dest server
    val dstActor = ActorName(payload.readUTF) //dest actor name
    if (LocalServerName(systemContext).name != dstServer) return
    val hostPort = HostPort(packet.getAddress, packet.getPort)
    val msg = IncomingPacketReq(isReply, msgUuid, hostPort, ServerName(srcServer), dstActor, payload)
    send(insideActor, msg)
  }

  private def startReceivingIncomingMessages {
    val bufferSize = Udp(systemContext).maxPayloadSize
    try {
      socket = new DatagramSocket(localHostPort.port, localHostPort.inetAddress)
    } catch {
      case ex: Throwable => {
        ex.printStackTrace
      }
    }
    buffer = new Array[Byte](bufferSize)
    receiveIncomingMessages
  }

  def startUdp(systemContext: SystemContext) {
    sc = Some(systemContext)
    stopUdp
    udpRunner = new Thread {
      override def run() {
        startReceivingIncomingMessages
      }
    }
    udpRunner.start
  }

  def stopUdp {
    try {
      if (socket != null) socket.close
    } catch {
      case ex =>
    }
    udpRunner.interrupt
    while (started) {
      Thread.sleep(1)
    }
  }
}

object UdpListener {
  private val listeners = new HashMap[HostPort, UdpListener]

  private def apply(localHostPort: HostPort, insideActor: LiteActor): UdpListener = {
    val listener = listeners.get(localHostPort)
    listener match {
      case None => listeners += (localHostPort -> new UdpListener(localHostPort, insideActor))
      case Some(x) =>
    }
    listeners(localHostPort)
  }

  def apply(systemContext: SystemContext, insideActor: LiteActor): UdpListener = {
    val listener = apply(Udp(systemContext).localHostPort, insideActor)
    listener.startUdp(systemContext)
    listener
  }

}

