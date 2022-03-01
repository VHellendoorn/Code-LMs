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

class UdpSender(reactor: LiteReactor)
  extends LiteActor(reactor, null) {
  var socket: DatagramSocket = null
  val localServer = LocalServerName(systemContext).name

  bind(classOf[OutgoingPacketReq], send)

  private def send(msg: AnyRef, responseProcess: PartialFunction[Any, Unit]) {
    val req = msg.asInstanceOf[OutgoingPacketReq]
    val payload = req.outputPayload
    payload.writeUTF(req.actorName.toString) //dest actor name
    payload.writeUTF(req.server.name) //dest server
    payload.writeUTF(req.msgUuid) //message UUID
    payload.writeByte(req.isReply.asInstanceOf[Byte]) //message type
    payload.writeUTF(localServer) //sender server
    val buffer = payload.getBytes
    //TODO: must throw an exception when the buffer is truncated
    //woops! sorry, no exceptions should be thrown. Only we need to send errors.
    //We also need to trap exceptions and send errors.
    val hostPort = req.hostPort
    val packet = new DatagramPacket(
      buffer, buffer.length,
      hostPort.inetAddress, hostPort.port)
    if (socket == null) socket = new DatagramSocket
    socket send packet
    responseProcess(OutgoingPacketRsp())
  }

  def stopUdp {
    if (socket != null) {
      socket.close
      socket = null
    }
  }
}
