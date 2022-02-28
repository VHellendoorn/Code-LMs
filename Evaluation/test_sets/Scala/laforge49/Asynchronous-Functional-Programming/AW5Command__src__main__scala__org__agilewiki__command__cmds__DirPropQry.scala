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
package org.agilewiki.command.cmds

import org.agilewiki.actors.ActorLayer
import java.util.{Properties}
import java.io.{FileInputStream, InputStreamReader, FileReader, File}
import org.agilewiki.util.SystemComposite

class DirPropQry(systemContext: SystemComposite, uuid: String)
        extends SimpleQuery(systemContext, uuid) {
  override protected def query: String = {
    var dir = "."
    if (context.contains("path"))
      dir = context.get("path")
    var f = new File(dir)
    if (!f.isAbsolute) {
      val activeTemplatePathname = context.get("activeTemplatePathname")
      f = new File(new File(activeTemplatePathname), dir)
    }
    f = new File(f, "dir.prop")
    if (f.exists) {
      var prefix = "dir"
      if (context.contains("dirPrefix"))
        prefix = context.get("dirPrefix")
      val fis = new FileInputStream(f)
      val isr = new InputStreamReader(fis,"UTF-8")
      val p = new Properties
      p.load(isr)
      isr.close
      val pns = p.stringPropertyNames
      val it = pns.iterator
      while (it.hasNext) {
        val n = it.next.asInstanceOf[String]
        if (n != "index.html") {
          val v = p.get(n).asInstanceOf[String]
          context.setCon(prefix+"."+n, v)
        }
      }
    }
    null
  }
}

object DirPropQry {
  val name = "qry-dirProp"
  val cls = "org.agilewiki.command.cmds.DirPropQry"
}
