package ncreep.figi

import language.experimental.macros
import reflect.macros.Context
import reflect.api._
import ncreep.figi._

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
      
      def isImplicitlyConfChainer(tpe: Type): Boolean = 
        tpe <:< typeOf[ConfChainer] ||
        c.inferImplicitView(EmptyTree, tpe, typeOf[ConfChainer]) != EmptyTree
        
    def hasImplicitConverter(tpe: Type): Boolean =  {
        // applying ConfConverter to tpe
        val convType = typeOf[ConfConverter[Nothing]] match { case TypeRef(p, s, _) => TypeRef(p, s, List(tpe)) }
        c.inferImplicitValue(convType) != EmptyTree
      }
      
      
      val tpe = tag.tpe
      
      def abort(msg: String) = c.abort(c.enclosingPosition, msg)
      
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
    	  val (isConfChainer, hasConverter) = (isImplicitlyConfChainer(t), hasImplicitConverter(t))
          if (!isConfChainer && !hasConverter) abort(s"No implicit instance of ${q"ncreep.figi.ConfConverter[$t]"} found to convert the result of method $name")
          
    	  val confName = q"$prefix :+ $name"
    	  val getter = 
    	    // creating chaining invocation
    	    if (isConfChainer) q"ncreep.figi.FigiMacros.makeConf[$t]($conf, $confName)" 
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
              else abort(s"Type mismatch in default configuration argument for method $name, $argType does not (weakly) conform to $t")
            }
            case _ => abort(s"Too many arguments in method ${name}")
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
}