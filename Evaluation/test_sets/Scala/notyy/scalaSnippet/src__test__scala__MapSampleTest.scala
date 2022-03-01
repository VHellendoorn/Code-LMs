import org.scalatest.{FunSpec, Matchers}

import scala.collection.JavaConversions._

class MapSampleTest extends FunSpec with Matchers {
  describe("a MapSample"){
    it("should select element that is >2 , then do * 2 to each element"){
      val mapSample = new MapSample()
      val list = List[Integer](1,2,3,4,5)
      val rs = mapSample.process(list)
      rs.size() should be (3)
      rs.toList should be (List(6,8,10))
    }
    it("should select element that is >2 , then do * 2 to each element, with functional programming"){
      val mapSample = new MapSample()
      val list = List[Integer](1,2,3,4,5)
      val rs = mapSample.funcProcess(list)
      rs.size() should be (3)
      rs.toList should be (List(6,8,10))
    }
  }

}
