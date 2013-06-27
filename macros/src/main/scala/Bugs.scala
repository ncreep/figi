object Bugs {
  import language.experimental.macros
  import scala.reflect.macros.Context

  def foo(ls: List[_]): Unit = macro fooImpl
  def fooImpl(c: Context)(ls: c.Expr[List[_]]): c.Expr[Unit] = {
    import c.universe._
    c.Expr[Unit](Block(List(ls.tree, ls.tree), c.literalUnit.tree))
  }
}