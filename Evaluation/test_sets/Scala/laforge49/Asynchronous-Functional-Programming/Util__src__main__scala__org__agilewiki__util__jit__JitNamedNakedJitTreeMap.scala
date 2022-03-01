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
package org.agilewiki.util.jit

class JitNamedNakedJitTreeMap extends JitNamedJitTreeMap {

  override protected def wrapper(cursor: JitMutableCursor, name: String) = {
    val jit = jitRole.createSubJit
    jit.partness(this, name, this)
    jit.loadJit(cursor)
    jit
  }

  override protected def wrapper(jit: Jit, name: String) = {
    jit
  }

  override def get(name: String) = {
    val j = treeMap.get(name)
    j
  }

  override def getWrapper(name: String): Jit = get(name)

  override def put(name: String, jit: Jit) = {
    if (jit.jitRole.roleName != jitRole.subRoleName) {
      System.err.println("in role " + jitRole.roleName)
      if (jitContainer != null) {
        System.err.println("in container " + jitContainer.getClass.getName)
        val cc = jitContainer.jitContainer
        if (cc != null)
          System.err.println("in container " + cc.getClass.getName)
      }
      System.err.println("expecting content of role " + jitRole.subRoleName)
      throw new IllegalArgumentException("jit is wrong role: " + jit.jitRole.roleName)
    }
    super.put(name, jit)
  }

  override def remove(name: String): Jit = {
    writeLock
    validateByteLength
    val jit = treeMap.remove(name)
    if (jit == null) return null
    if (debugJit) {
      System.err.println()
      //System.err.println(name+" = "+jit.asInstanceOf[JitString].getString)
      System.err.println("remove " + (-stringByteLength(name) - jit.jitByteLength))
    }
    jitUpdater(-stringByteLength(name) - jit.jitByteLength, this)
    validateByteLength
    jit.clearJitContainer
    jit
  }

  override def removeWrapper(name: String) = remove(name)
}
