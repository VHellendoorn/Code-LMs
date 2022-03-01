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
package org.agilewiki
package web
package template
package composer

import java.util.ArrayDeque
import org.agilewiki.web.template.player.SaxListPlayerActor
import org.agilewiki.web.template.player.StartPlayerMsg
import org.agilewiki.web.template.router.TemplateRouterActor
import org.agilewiki.web.template.saxmessages.CharactersMsg
import org.agilewiki.web.template.saxmessages.EndElementMsg
import org.agilewiki.web.template.saxmessages.StartElementMsg
import util.actors._
import util.actors.res._
import org.agilewiki.actors.application.Context
import command.{ExtendedContext, XmlComposer}
import util.SystemComposite
import templatecache._

class TemplateComposerActor(systemContext: SystemComposite, uuid: String)
  extends SynchronousActor(systemContext, uuid)
  with XmlComposer {
  val stack = new ArrayDeque[InternalAddress]
  val sbStack = new ArrayDeque[StringBuilder]
  var sb = new StringBuilder
  var reply: InternalAddress = null
  var emptyElement = false

  override def pushSb(sb: StringBuilder) {
    sbStack.addFirst(this.sb)
    this.sb = sb
  }

  override def popSb: StringBuilder = {
    val rv = sb
    sb = sbStack.removeFirst
    rv
  }

  override def messageHandler = {
    case msg: StartElementMsg => startElement(msg)
    case msg: CharactersMsg => characters(msg)
    case msg: EndElementMsg => endElement(msg)
    case msg: PushMsg => push(msg)
    case msg: PopMsg => pop(msg)
    case msg => unexpectedMsg(reply, msg)
  }

  def startElement(msg: StartElementMsg) {
    debug(msg)
    if (emptyElement)
      sb.append(">")
    sb.append("<")
    sb.append(msg.qName)
    val attributes = msg.attributes
    val l = attributes.getLength
    var i = 0
    while (i < l) {
      sb.append(" ")
      sb.append(attributes.getQName(i))
      sb.append("=\"")
      var content = attributes.getValue(i)
      if (encodeAttributes) {
        content = entify(content)
      }
      sb.append(content)
      sb.append("\"")
      i += 1
    }
    emptyElement = true
    ack
  }

  def entify(s: String) = {
    var content = s
    content = content.
      replace("&", "&amp;").
      replace("<", "&lt;").
      replace(">", "&gt;").
      replace("'", "&apos;").
      replace("\"", "&quot;")
    if (encodeNl) {
      content = content.
        replace("\r\n", "\n").
        replace("\r", "\n").
        replace(" ", "&nbsp;").
        replace("\n", "<br />")
    }
    content
  }

  def characters(msg: CharactersMsg) {
    debug(msg)
    if (emptyElement)
      sb.append(">")
    var content = msg.content
    if (encodeContents) {
      content = entify(content)
    }
    sb.append(content)
    emptyElement = false
    ack
  }

  def endElement(msg: EndElementMsg) {
    debug(msg)
    if (emptyElement) sb.append(" />")
    else {
      sb.append("</")
      sb.append(msg.qName)
      sb.append(">")
    }
    emptyElement = false
    ack
  }

  def push(msg: PushMsg) {
    debug(msg)
    stack.addFirst(msg.actor)
    ack
  }

  def pop(msg: PopMsg) {
    debug(msg)
    stack.removeFirst
    if (stack.isEmpty) reply ! TemplateResultMsg(sb.toString)
    else ack
  }

  def ack = stack.peekFirst ! AckMsg()
}

object TemplateComposerActor {
  def composerActor(systemContext: SystemComposite) = Actors(systemContext).
    actorFromClassName(ClassName(classOf[TemplateComposerActor])).asInstanceOf[TemplateComposerActor]

  def apply(systemContext: SystemComposite,
            userUuid: String,
            context: Context,
            requester: InternalAddress,
            extendedContext: ExtendedContext,
            templateUrl: String): Unit = {
    val mh: PartialFunction[AnyRef, Unit] = {
      case msg:TemplateResponse => {
        val template = msg.template
        val xmlComposer = TemplateComposerActor.composerActor(systemContext)
        xmlComposer.reply = requester
        extendedContext.xmlComposer = xmlComposer
        val player = SaxListPlayerActor(systemContext, template)
        val router = TemplateRouterActor(systemContext, userUuid, context, player, xmlComposer)
        player ! StartPlayerMsg(router)
      }
    }
    val req = TemplateRequest(new AnonymousSynchronousActor(systemContext, mh), null, templateUrl)
    val templateCacheActor = Web(systemContext).templateCacheActor
    templateCacheActor ! req
  }
}
