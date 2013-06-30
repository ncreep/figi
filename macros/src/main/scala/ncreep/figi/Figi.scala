package ncreep.figi

import language.experimental.macros
import reflect.macros.Context
import reflect.api._
import ncreep.figi._

/** Use [[Figi.makeConf]] to produce configuration instances.
 *  TODO example */
object Figi {

  /** Implements a config trait of the given type. */
  def makeConf[A](cnf: Conf): A = macro Macros.makeConfImpl[A]

  /** Wrapper object for macros to be hidden from the API.
   *  Using this, as macro methods cannot be marked `private`.
   */
  private[figi] object Macros {
    def makeConfImpl[A](c: Context)(cnf: c.Expr[Conf])
      (implicit tag: c.WeakTypeTag[A]): c.Expr[A] = {
      new Helper[c.type](c) {
        def tpe = tag.tpe
        def conf = cnf
        def prefix = c.universe.reify(Vector())
      }.res
    }
  }

  private abstract class Helper[C <: Context](val c: C) extends QuasiquoteCompat { helper =>
    def tpe: c.Type // The type of the trait being implemented
    def conf: c.Expr[Conf]
    def prefix: c.Expr[ConfNames]
    
    import c.universe._
    
    def isImplicitlyConfChainer(tpe: Type): Boolean =
      tpe <:< typeOf[ConfChainer] ||
        c.inferImplicitView(EmptyTree, tpe, typeOf[ConfChainer]) != EmptyTree

    def hasImplicitConverter(tpe: Type): Boolean = {
      // applying ConfConverter to tpe
      // must be a cleaner way to apply a type
      val convType = typeOf[ConfConverter[Nothing]] match { case TypeRef(p, s, _) => TypeRef(p, s, List(tpe)) }
      c.inferImplicitValue(convType) != EmptyTree
    }

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
      //TODO this error should be emitted after checking for too many arguments, as it is irrelevant in that case
      if (!isConfChainer && !hasConverter) abort(s"No implicit instance of ${q"ncreep.figi.ConfConverter[$t]"} found to convert the result of method $name")
      val confName = q"$prefix :+ $name"
      val getter =
        // creating chaining invocation
        if (isConfChainer) {
          // generating inner invocation on the fly, this way there's no need 
          // to expose another macro method in the API
          new Helper[c.type](c) {
            def tpe = t
            def conf = helper.conf
            def prefix = c.Expr(confName)
          }.res.tree
        }
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
//    println(impl) //RM
    val res = c.Expr(impl)
  }
}