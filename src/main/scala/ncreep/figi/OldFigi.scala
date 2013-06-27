package ncreep.figi

import language.experimental.macros
import scala.reflect.macros.Context
import scala.reflect.api._
import Figi._

//trait ConfConverter[+A] {
//  def apply(conf: Either[Exception, ConfValue]): A
//}
//
//trait Conf {
//  def get[A](conf: ConfNames)(implicit conv: ConfConverter[A]): A
//  def getWithDefault[A](conf: ConfNames, default: A)(implicit conv: ConfConverter[A]): A
//}
//
//object Conf extends Conf {
//  def get[A](conf: ConfNames)(implicit conv: ConfConverter[A]): A = { println(conf); null.asInstanceOf[A] }
//  def getWithDefault[A](conf: ConfNames, default: A)(implicit conv: ConfConverter[A]): A = { println(conf + " - " + default); null.asInstanceOf[A] }
//}
//
//trait ConfNameChainer {
//  def apply(confs: ConfNames): ConfName
//}
//
//object DefaultConfNameChainer extends ConfNameChainer {
//  def apply(confs: ConfNames) = confs.reduce(_ + "." + _)
//}

//object OldFigi {
//
//  //TODO macros do not support default args
//  def makeConf[A](//TODO type aliases, defaults
//      conf: Conf, 
//      prefix: List[String]): A = macro makeConfImpl[A] 
//  def makeConfImpl[A: c.WeakTypeTag](c: Context)(
//      conf: c.Expr[Conf], 
//      prefix: c.Expr[List[String]]): c.Expr[A] = {
//    import c.universe._
//
//    val tag = implicitly[c.WeakTypeTag[A]]
//    import tag._
//    
//    def fetchConf(tpe: Type, confName: ConfName): Tree = {
//      val tree =
//        Apply(
//          TypeApply( //TODO how do you splice a type?
//            Select(conf.tree, newTermName("get")), //TODO remove method reference
//            List(TypeTree(tpe))),
//          List(prefix.tree)
////          List(reify(prefix.eval :+ c.literal(confName).eval).tree)
//        )
//      println(tree)
//      tree
//    }
//
//    def canImplement(mods: Set[Modifier]): Boolean =
//      !(mods.contains(Modifier.`final`) || mods.contains(Modifier.`private`))
//
//    def reservedMethod(mem: Symbol): Boolean =
//      Set(
//        "$asInstanceOf",
//        "$isInstanceOf",
//        "synchronized",
//        "$hash$hash",
//        "$bang$eq",
//        "$eq$eq",
//        "ne",
//        "eq",
//        "finalize",
//        "wait",
//        "notifyAll",
//        "notify",
//        "toString",
//        "clone",
//        "equals",
//        "hashCode",
//        "getClass",
//        "$lessinit$greater",
//        "asInstanceOf",
//        "isInstanceOf",
//        "$bang$eq",
//        "$eq$eq"
//      ).contains(mem.name.encoded)
//
//    val members: Iterable[Option[Tree]] = for {
//      mem <- tpe.members
//      if (mem.isMethod && !reservedMethod(mem) && !mem.isFinal && !mem.isPrivate)
////      mods = mem.modifiers
////      if (canImplement(mods))
//      kind = mem.kind
//      tpe = mem.asType;
//      name = mem.name.decoded; termName = newTermName(name)
//      newMods = Modifiers(mods - Modifier.deferred + Modifier.`override`)
//    } yield {
//      def defDef(t: Type) = Some(DefDef(newMods, termName, List(), List(), TypeTree(t), fetchConf(t, name)))
//      def valDef(t: Type) = Some(ValDef(newMods, termName, TypeTree(t), fetchConf(t, name)))
//      tpe match {
//        //TODO is there a more direct way to distinguish a def without () from a val?
//        case NullaryMethodType(t) if kind == "value" => valDef(t)
//        case NullaryMethodType(t) if kind == "method" => defDef(t)
//        case NullaryMethodType(t) => valDef(t) // just in case there are other kinds
//        case MethodType(Nil, t) => defDef(t)
//        case _ => None //sys.error("Invalid member type: " + tpe + ": " + mem)
//      }
//    }
//    val memberList = members.toList.flatten(_.toList) // TODO why doesn't the implicit conversion work here?
//    memberList foreach println //RM
//
//    // anyway to avoid this?
//    val init = DefDef(Modifiers(), newTermName("<init>"), List(), List(List()),
//      TypeTree(), Block(List(Apply(Select(Super(This(newTypeName("")), newTypeName("")), newTermName("<init>")), List())), Literal(Constant(()))))
//
//    val implName = newTermName(c.fresh("impl$"))
//    val impl = ModuleDef(
//      Modifiers(), implName, Template(
//        List(Ident(newTypeName(sym.name.encoded))),
//        emptyValDef,
//        init :: memberList
//      ))
//
//    println(impl)
//
//    Expr[A](Block(List(impl), Ident(implName)))
//  }
//
//  implicit object Conv extends ConfConverter[Nothing] { def apply(conf) = ??? }
//  trait Foo {
//    def a(): Int
//    val b: Double
//    def c: List[Int]
//  }
//
//  def desugar(a: Any): String = macro desugarImpl
//
//  def desugarImpl(c: Context)(a: c.Expr[Any]) = {
//    import c.mirror._
//
//    val s = show(a.tree)
//    c.Expr(Literal(Constant(s)))
//  }
//
//  def log[A](a: A): A = macro logImpl[A]
//
//  def logImpl[A: c.TypeTag](c: Context)(a: c.Expr[A]): c.Expr[A] = {
//    import c.mirror._
//    val aCode = c.Expr[String](Literal(Constant(show(a.tree))))
//    c.reify {
//      val temp = a.eval // replace with `a.value` when that works.
//      println(aCode.eval + " = " + temp)
//      temp
//    }
//  }
//
//}