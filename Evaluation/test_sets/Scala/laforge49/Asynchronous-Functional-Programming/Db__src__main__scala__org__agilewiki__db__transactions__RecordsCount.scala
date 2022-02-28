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
package db
package transactions

import blip._
import incDes._
import blocks._

object RecordsCount {
  def apply(db: Actor) = {
    val je = (new RecordsCountFactory).newActor(null).asInstanceOf[IncDes]
    val chain = new Chain
    chain.op(db, TransactionRequest(je))
    chain
  }
}

class RecordsCountFactory extends IncDesFactory(DBT_RECORDS_COUNT) {
  override protected def instantiate = {
    val req = super.instantiate
    addComponent(new QueryRequestComponent(req))
    addComponent(new RecordsCountComponent(req))
    req
  }
}

class RecordsCountComponent(actor: Actor)
  extends Component(actor) {
  bindMessageLogic(classOf[Process], new ChainFactory(process))

  private def process(msg: AnyRef, chain: Chain) {
    chain.op(systemServices, Records(), "records")
    chain.op(Unit => chain("records"), Size())
  }
}
