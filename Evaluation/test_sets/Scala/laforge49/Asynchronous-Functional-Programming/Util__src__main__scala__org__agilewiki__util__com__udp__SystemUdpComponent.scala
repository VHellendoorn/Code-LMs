/*
 * Copyright 2010 Bill La Forge
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
package org.agilewiki.util
package com
package udp

import java.util.{Properties}
import actors.res.ClassName
import actors.SystemActorsComponent

object _Udp {
  def defaultConfiguration(serverName: String, host: String, port: Int) = {
    val properties = new Properties
    Configuration.defaultConfiguration(properties, serverName)
    Udp.defaultConfiguration(properties, host, port)
    properties
  }
}

class _Udp(configurationProperties: Properties)
        extends SystemComposite
                with SystemShutdownComponent
                with SystemConfigurationComponent
                with SystemActorsComponent
                with SystemUdpComponent {
  setProperties(configurationProperties)
  udp.start

  def close {
    udp.close
  }
}

object Udp {
  val UDP_BINDING_ADDRESS_PROPERTY = "orgAgileWikiUtilComUdpUdpBindingAddress"
  val UDP_BINDING_PORT_PROPERTY = "orgAgileWikiUtilComUdpUdpBindingPort"
  val UDP_DATAGRAM_BUFFER_SIZE_PROPERTY = "orgAgileWikiUtilComUdpUdpDatagramBufferSize"

  def defaultConfiguration(properties: Properties, host: String, port: Int) {
    properties.put(UDP_DATAGRAM_BUFFER_SIZE_PROPERTY, "" + 102400)
    properties.put(UDP_BINDING_ADDRESS_PROPERTY, host)
    properties.put(UDP_BINDING_PORT_PROPERTY, "" + port)
  }

  def apply(context: SystemComposite) = context.asInstanceOf[SystemUdpComponent].udp
}

trait SystemUdpComponent {
  this: SystemComposite 
          with SystemShutdownComponent
          with SystemConfigurationComponent
          with SystemActorsComponent =>

  protected lazy val _orgAgileWikiUtilComUdpUdp = defineUdp

  protected def defineUdp = new Udp

  def udp = _orgAgileWikiUtilComUdpUdp

  class Udp {
    var udpSenderActor: UdpSender = actors.actorFromClassName(ClassName(classOf[UdpSender])).asInstanceOf[UdpSender]
    private var udpListener: Option[UdpListener] = None
    var host: String = configuration.requiredProperty(Udp.UDP_BINDING_ADDRESS_PROPERTY)
    var port: Int = configuration.requiredIntProperty(Udp.UDP_BINDING_PORT_PROPERTY)

    def start {
      udpListener = Some(UdpListener(SystemUdpComponent.this))
    }

    def close {
      try {
        udpListener match {
          case Some(lsr) => lsr.stopUdp
          case None =>
        }
        udpSenderActor.stopUdp
      } catch {
        case ex: Throwable => {}
      }
    }
  }
}