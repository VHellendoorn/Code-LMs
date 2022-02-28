package org.agilewiki
package db
package transactions
package dbSeq
package dbIntSeq

import blip._
import bind._
import services._
import seq._
import incDes._
import org.specs.SpecificationWithJUnit

class DbIntSeqTest extends SpecificationWithJUnit {
  "DbIntSeqTest" should {
    "sequence" in {
      val systemServices = SystemServices(new ServicesRootComponentFactory)
      val dbName = "DbIntSeqNoLog.db"
      val file = new java.io.File(dbName)
      file.delete
      val properties = new Properties
      properties.put("dbPathname", dbName)
      val db = Subsystem(
        systemServices,
        new SmallNoLogComponentFactory,
        properties = properties,
        actorId = ActorId("db"))
      val one = IncDesString(null)
      val two = IncDesString(null)
      val three = IncDesString(null)
      val ten = IncDesString(null)
      val dbIntSeq = new DbIntSeq[IncDesString](db, "/$")
      val chain = new Chain
      chain.op(systemServices, Register(db))
      chain.op(db, SetRequest(db, "/$", IncDesIntStringMap(null, db)))
      chain.op(one, Set(null, "One"))
      chain.op(db, SetRequest(db, "/$/1", one))
      chain.op(two, Set(null, "Two"))
      chain.op(db, SetRequest(db, "/$/2", two))
      chain.op(db, SetRequest(db, "/$/2", null))
      chain.op(three, Set(null, "Three"))
      chain.op(db, SetRequest(db, "/$/3", three))
      chain.op(ten, Set(null, "Ten"))
      chain.op(db, SetRequest(db, "/$/10", ten))
      chain.op(dbIntSeq, First(), "a")
      chain.op(dbIntSeq, Current(3), "b")
      chain.op(dbIntSeq, Next(3), "c")
      Future(systemServices, chain)
      println(chain.results)
      systemServices.close
    }
  }
}