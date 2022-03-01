package dup

object DupModule {
  def repl[A](xs: List[A], times: Int): List[A] = xs.flatMap(multiply(_,times))

  def multiply[A](x: A, times: Int): List[A] = (1 to times).map(_ => x).toList

  repl(List(1,2,3),3 )

  def dup[A] = repl(_:List[A], 2)
}
