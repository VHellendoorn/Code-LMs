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
package jit

import sequence.basic.ListSequence

object JitNamedJitList {
  val defaultRoleName = JIT_NAMED_JIT_LIST_ROLE_NAME

  def createJit(context: SystemComposite) = Jits(context).createJit(defaultRoleName).asInstanceOf[JitNamedJitList]
}

class JitNamedJitList extends JitNamedVariableJitTreeMap {
  private var list = new java.util.ArrayList[String]

  override def iterator = list.iterator

  override protected def initialValue(cursor: JitMutableCursor, name: String): Jit = {
    list.add(name)
    super.initialValue(cursor, name)
  }

  override def remove(name: String): Jit = {
    val rv = super.remove(name)
    if (rv != null) list.remove(name)
    rv
  }

  override def put(name: String, jit: Jit) {
    writeLock
    list.add(name)
    super.put(name, jit)
  }

  def move(moveKey: String, beforeKey: String, after: Boolean) {
    writeLock
    if (!list.contains(moveKey) || !list.contains(beforeKey))
      throw new IllegalArgumentException
    if (moveKey == beforeKey) return
    list.remove(moveKey)
    var i = list.indexOf(beforeKey)
    if (after) i += 1
    list.add(i, moveKey)
    jitUpdater(0,this)
  }

  def get(ndx: Int) = list.get(ndx)

  def indexOf(name: String) = list.indexOf(name)

  override def jitSequence(reverse: Boolean) = {
    new ListSequence(list, reverse)
  }
}
