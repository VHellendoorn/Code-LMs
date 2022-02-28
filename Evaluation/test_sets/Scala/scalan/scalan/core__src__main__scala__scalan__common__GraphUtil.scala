package scalan.common

import scala.collection.mutable.Map
import scala.collection.mutable.HashMap
import scala.collection.mutable.Stack
import scala.collection.mutable.Buffer
import scala.collection.mutable.ArrayBuffer


object GraphUtil {
   def depthFirstSetFrom[A](starts: Set[A])(neighbours: A => TraversableOnce[A]): Set[A] = {
     var visited = Set[A]()

     def visit(s: A): Unit = {
       if (!(visited contains s)) {
         visited += s
         neighbours(s) foreach visit
       }
     }

     starts foreach visit
     visited
   }

  /**
   * Returns the strongly connected components
   * of the graph rooted at the first argument,
   * whose edges are given by the function argument.
   *
   * The scc are returned in _reverse_ topological order.
   * Tarjan's algorithm (linear).
   */
  def stronglyConnectedComponents[T](start: Seq[T])(succ: T => Seq[T]): Seq[Seq[T]] = {
    val tarjan = new Tarjan(succ)

    for (node <- start)
      tarjan.visit(node)

    tarjan.res
  }

  private final class IntRef(init: Int) {
    var value: Int = init
  }

  private final class Tarjan[T](succ: T => Seq[T]) {
    private var id = 0
    private val stack: Stack[T] = new Stack()
    private val mark: Map[T, Int] = new HashMap()

    val res: Buffer[Seq[T]] = new ArrayBuffer()

    def visit(node: T): Int = {
      mark.getOrElse(node, {
        id += 1

        mark.put(node, id)
        stack.push(node)
        //    println("push " + node)

        var min: Int = id
        for (child <- succ(node)) {
          val m = visit(child)

          if (m < min)
            min = m
        }

        if (min == mark(node)) {
          val scc: Buffer[T] = new ArrayBuffer()

          var loop: Boolean = true
          do {
            val element = stack.pop()
            //        println("appending " + element)
            scc.append(element)
            mark.put(element, Integer.MAX_VALUE)
            loop = element != node
          } while (loop)

          res.append(scc.toSeq)
        }
        min
      })
    }
  }
}
