package ncreep.figi

import language.experimental.macros
import reflect.macros.Context
import reflect.api._


private[figi] object FigiMacros {
  def makeConf[A](
      conf: Conf, 
      prefix: ConfNames): A = macro makeConfImpl[A]

  def makeConfImpl[A](con: Context)(
    conf: con.Expr[Conf],
    prefix: con.Expr[ConfNames])(implicit tag: con.WeakTypeTag[A]): con.Expr[A] = {

    class Helper extends QuasiquoteCompat {
      val c: con.type = con
      import c.universe._
      
      def isImplicitlyConfChainer(tpe: Type) = 
        tpe <:< typeOf[ConfChainer] ||
        c.inferImplicitView(EmptyTree, tpe, typeOf[ConfChainer]) != EmptyTree
      
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
    	  val confName = q"$prefix :+ $name"
    	  val getter = 
    	    // creating chaining invocation
    	    if (isImplicitlyConfChainer(t)) q"makeConf[$t]($conf, $confName)" 
    	    else q"$conf.get[$t]($confName)"
        if (meth.isStable) { // val
          q"val $termName = $getter"
        } else { // def
          def nullaryDef = q"def $termName = $getter"
          mem.typeSignature match {
            case NullaryMethodType(_) => nullaryDef
            case MethodType(Nil, _) => nullaryDef
            case MethodType(arg :: Nil, _) => {
              val argType = arg.typeSignature
              val argName = newTermName("arg")
              if (argType weak_<:< t) q"def $termName($argName: ${arg.typeSignature}) = $conf.getWithDefault[$t]($confName, $argName)"
              else c.abort(c.enclosingPosition, s"Type mismatch in default configuration argument for method $name, $argType does not (weakly) conform to $t")
            }
            case _ => c.abort(c.enclosingPosition, s"Too many arguments in method ${name}")
          }
        } 
      }

      val typeName = newTypeName(tpe.typeSymbol.name.encoded)
      val impl = q"new $typeName {..$impls}"
      println(impl)//RM
      val res = c.Expr[A](impl)
    }

    (new Helper).res
  }

  //TODO testing only
  object Conf extends Conf {
    def get[A](conf: ConfNames)(implicit conv: ConfConverter[A]): A = { println(conf); null.asInstanceOf[A] }
    def getWithDefault[A](conf: ConfNames, default: A)(implicit conv: ConfConverter[A]): A = { println(conf + " - " + default); null.asInstanceOf[A] }
  }

  import util.Try
  implicit object Conv extends ConfConverter[Nothing] { def apply(conf: Try[ConfValue]) = ??? }
  trait Foo {
    def a(): Int
    val b: Double
    def c: List[Int]
    def d(c:Int): Double
    
    def baz: Baz
    def qux: Qux
    val bar: Bar
  }
  
  trait Bar extends ConfChainer {
    def a: Int
  }
  implicit class QuxToConf(q: Qux) extends ConfChainer
  trait Qux 
  implicit def bazToConf(baz: Baz) = new ConfChainer {}
  trait Baz

}