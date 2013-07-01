package ncreep.figi

import language.experimental.macros
import reflect.macros.Context
import reflect.api._
import ncreep.figi._

/** Use [[Figi.makeConf]] to produce configuration instances.
 *  
 *  Assuming proper implicits in scope:
 *  ```
 *  trait Foo { def a: Int; val b: String }
 *  val config = getConfig ...
 *  val foo: Foo = Figi.makeConf[Foo](config)
 *  
 *  println(foo.a)
 *  println(foo.b)
 *  ```
 *  
 *  More examples in [[ncreep.figi.FigiSpecs]]
 */
object Figi {

  /** Implements a config trait of the given type. */
  def makeConf[A](cnf: InstanceWithConf[_, _ <: Conf[_, _]]): A = macro Macros.makeConfImpl[A]

  /** Wrapper object for macros to be hidden from the API.
   *  Using this, as macro methods cannot be marked `private`.
   */
  private[figi] object Macros {
    def makeConfImpl[A](c: Context)(cnf: c.Expr[InstanceWithConf[_, _ <: Conf[_, _]]])(implicit tag: c.WeakTypeTag[A]): c.Expr[A] = {
      new Helper[c.type](c) {
        def tpe = tag.tpe
        def conf = cnf
        // when writing just Vector() the macro misbehaves when not finding implicit
        // converters, go figure...
        def prefix = c.universe.reify(collection.immutable.Vector())
      }.res
    }
  }

  private abstract class Helper[C <: Context](val c: C) extends QuasiquoteCompat { helper =>
    def tpe: c.Type // The type of the trait being implemented
    def conf: c.Expr[InstanceWithConf[_, _ <: Conf[_, _]]]
    def prefix: c.Expr[ConfNames]

    import c.universe._

    /** Applies `tp1` as a type constructor to `tp2` produce a `Type` instance (`tp1[tp2]`).
     *  `tp1` is assumed to be applied to `Nothing` at this stage.
     */
    def applyType(tp1: Type, tp2: Type): Type = {
      // must be a cleaner way to apply a type
      val appliedType = tp1 match { case TypeRef(p, s, _) => TypeRef(p, s, List(tp2)) }
      appliedType
    }

    /** @return true if the an implicit instance of tpe is in scope. */
    def hasImplicitValue(tpe: Type): Boolean = c.inferImplicitValue(tpe) != EmptyTree
    /** @return true if the an implicit instance of tp1[tp2] is in scope. */
    def hasImplicitValue(tp1: Type, tp2: Type): Boolean = hasImplicitValue(applyType(tp1, tp2))

    def isImplicitlyConfChainer(tpe: Type): Boolean =
      tpe <:< typeOf[ConfChainer] ||
        hasImplicitValue(typeOf[IsConfChainer[Nothing]], tpe)

    // ugly hack to get the type currently used as a converter, there must be a better way...
    // using intermediate 'val cnf' to ensure that a stable identifier is used to obtain the type (no idea why does it break a times)
    def converterType(tpe: Type) = c.typeCheck(q"{ val cnf = $conf; ???.asInstanceOf[cnf.confTypeClass.CC[$tpe]] }").tpe
    def hasImplicitConverter(tpe: Type): Boolean = hasImplicitValue(converterType(tpe))

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
      //TODO this error should be emitted after checking for too many arguments, as it may be irrelevant in that case
      if (!isConfChainer && !hasConverter) abort(s"No implicit instance of ${q"${converterType(t).normalize}"} found to convert the result of method $name")
      val confName = q"$prefix :+ $name"
      val getter: Tree =
        // creating chaining invocation
        if (isConfChainer) {
          // generating inner invocation on the fly, this way there's no need 
          // to expose another macro method in the API
          new Helper[c.type](c) {
            def tpe = t
            def conf = helper.conf
            def prefix = c.Expr(confName)
          }.res.tree
        } else q"$conf.confTypeClass.get[$t]($conf.config, $confName)"
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
            if (argType weak_<:< t)
              q"def $termName($argName: ${arg.typeSignature}) = $conf.confTypeClass.getWithDefault[$t]($conf.config, $confName, $argName)"
            else abort(s"Type mismatch in default configuration argument for method $name, $argType does not (weakly) conform to $t")
          }
          case _ => abort(s"Too many arguments in method ${name}")
        }
      }
    }

    val typeName = newTypeName(tpe.typeSymbol.name.encoded)
    val impl = q"new $typeName {..$impls}"
    //    println(impl) //TODO remove
    val res = c.Expr(impl)
  }
}