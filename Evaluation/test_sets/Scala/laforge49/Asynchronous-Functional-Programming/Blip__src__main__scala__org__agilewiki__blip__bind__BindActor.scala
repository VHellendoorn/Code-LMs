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
package org.agilewiki.blip.bind

import org.agilewiki.blip.exchange._

/**
 * Objects which implement BindActor support message binding,
 * default message processing via a hierarchy of actors and
 * can share a common exchange messenger.
 */
trait BindActor
  extends ExchangeMessengerActor
  with Bindings {

  /**
   * The exchange messenger for this actor.
   */
  private var _exchangeMessenger: Mailbox = null

  /**
   * The actor which processes any messages not bound to this actor.
   */
  private var _superior: BindActor = null

  /**
   * "This" actor.
   */
  private val _activeActor = ActiveActor(this)

  /**
   * Set to true when initialization is complete.
   */
  private var _opened = false

  /**
   * "This" actor is implicit.
   */
  implicit def activeActor: ActiveActor = _activeActor

  /**
   * Returns the exchange messenger object used by the actor.
   */
  override def exchangeMessenger = _exchangeMessenger

  /**
   * Set the exchange messenger for this actor.
   * (Valid only during actor initialization.)
   */
  def setExchangeMessenger(exchangeMessenger: Mailbox) {
    if (isOpen) throw new IllegalStateException
    _exchangeMessenger = exchangeMessenger
  }

  /**
   * Returns the mailboxFactory/threadManager.
   */
  def mailboxFactory = exchangeMessenger.mailboxFactory

  /**
   * Returns an asynchronous exchange.
   */
  def newAsyncMailbox = mailboxFactory.newAsyncMailbox

  /**
   * Returns a synchronous exchange.
   */
  def newSyncMailbox = mailboxFactory.newSyncMailbox

  /**
   * Returns true when initialization is complete.
   */
  def isOpen = _opened

  /**
   * Complete actor initialization if it is not already complete.
   */
  def initialize {
    if (!isOpen) opener
  }

  /**
   * Complete actor initialization.
   */
  protected def opener {
    open
    _opened = true
  }

  /**
   * Perform application-specific actor initialization.
   */
  protected def open {}

  /**
   * Specify the actor to process messages not bound by this actor.
   * (Not valid once initialization is complete.)
   */
  def setSuperior(superior: BindActor) {
    if (isOpen) throw new IllegalStateException
    _superior = superior
  }

  /**
   * Returns the actor which processes messages not bound by this actor.
   */
  def superior = _superior

  /**
   * Create a new BindRequest.
   */
  def newRequest(rf: Any => Unit,
                 data: AnyRef,
                 bound: QueuedLogic,
                 src: ExchangeMessengerSource) =
    new BindRequest(this, rf, data, bound, src)

  /**
   * If initialization is not complete, then complete it.
   * Once complete, process the application request.
   */
  def apply(msg: AnyRef)
           (responseFunction: Any => Unit)
           (implicit srcActor: ActiveActor) {
    initialize
    val messageLogic = messageLogics.get(msg.getClass)
    if (messageLogic != null) {
      messageLogic.func(this, msg, responseFunction)(srcActor)
    }
    else if (superior != null) superior(msg)(responseFunction)(srcActor)
    else {
      System.err.println("bindActor = " + this.getClass.getName)
      throw new IllegalArgumentException("Unknown type of message: " + msg.getClass.getName)
    }
  }

  /**
   * Check that this actor, or one of its superiors, can process a given
   * class of application request.
   * (To be called from within an application-specific open method.)
   */
  def requiredService(reqClass: Class[_ <: AnyRef]) {
    if (isOpen) throw new IllegalStateException
    var actor: BindActor = this
    while (!actor.messageLogics.containsKey(reqClass)) {
      if (superior == null)
        throw new UnsupportedOperationException("service missing for " + reqClass.getName)
      actor = superior
    }
  }
}
