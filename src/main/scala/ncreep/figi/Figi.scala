package ncreep.figi

import language.experimental.macros
import reflect.macros.Context
import reflect.api._
import util.Try
import com.sun.xml.internal.ws.wsdl.writer.document.ParamType

trait ConfConverter[+A] {
  def apply(conf: Try[ConfValue]): A
}

trait Conf {
  def get[A](conf: ConfNames)(implicit conv: ConfConverter[A]): A
  def getWithDefault[A](conf: ConfNames, default: A)(implicit conv: ConfConverter[A]): A
}

//TODO testing only
object Conf extends Conf {
  def get[A](conf: ConfNames)(implicit conv: ConfConverter[A]): A = { println(conf); null.asInstanceOf[A] }
  def getWithDefault[A](conf: ConfNames, default: A)(implicit conv: ConfConverter[A]): A = { println(conf + " - " + default); null.asInstanceOf[A] }
}

object Figi {
  //TODO macros do not support default args
  def makeConf[A](
      conf: Conf, 
      prefix: ConfNames = Vector()): A = macro makeConfImpl[A]

  def makeConfImpl[A](con: Context)(
    conf: con.Expr[Conf],
    prefix: con.Expr[ConfNames])(implicit tag: con.WeakTypeTag[A]): con.Expr[A] = {
    import con.universe._

    class Helper extends QuasiquoteCompat {
      val c: con.type = con
      import c.universe._
      val tpe = tag.tpe

      val impls: Iterable[Tree] = for {
        mem <- tpe.members
        if mem.isMethod
        //TODO avoid using internal Scala implementation (currently not in the public API)
        meth = mem.asInstanceOf[reflect.internal.Symbols$MethodSymbol]
        if meth.isDeferred
        name = meth.name.decoded
        termName = newTermName(name)
        t = meth.returnType.asInstanceOf[Type]
      } yield {
    	  val getter = q"$conf.get[$t]($prefix :+ $name)"
        //TODO is there a way to avoid dupilcation here?
        if (meth.isStable) { // val
          q"val $termName = $getter"
        } else { // def
          q"def $termName = $getter"
        }
      }

      val typeName = newTypeName(tpe.typeSymbol.name.encoded)
      val impl = q"new $typeName {..$impls}"

      val res = c.Expr[A](impl)
    }

    (new Helper).res
  }
  
  implicit object Conv extends ConfConverter[Nothing] { def apply(conf: Try[ConfValue]) = ??? }
  trait Foo {
    def a(): Int
    val b: Double
    def c: List[Int]
  }

}