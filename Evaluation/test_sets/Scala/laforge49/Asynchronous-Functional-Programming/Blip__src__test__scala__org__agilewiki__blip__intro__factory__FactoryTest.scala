package org.agilewiki.blip
package intro.factory

import org.specs.SpecificationWithJUnit
import bind._

class ConstantFactory(_constant: Any)
  extends Factory(null) {

  def constant = _constant

  override protected def instantiate = new ConstantActor
}

case class Constant()

class ConstantActor
  extends Actor {

  bindMessageLogic(classOf[Constant], new ConcurrentData(
    Unit => {
      //factory is not initialized until after the actor is constructed.
      factory.asInstanceOf[ConstantFactory].constant
    }
  ))
}

class FactoryTest extends SpecificationWithJUnit {
  "FactoryTest" should {
    "create an actor" in {
      val constantFactory = new ConstantFactory(99)
      val constantActor = constantFactory.newActor(null)
      val chain = new Chain
      chain.op(constantActor, Constant())
      //ConcurrentData binding is not supported by Future
      val constant = Future(constantActor, chain)
      constant must be equalTo (99)
    }
  }
}
