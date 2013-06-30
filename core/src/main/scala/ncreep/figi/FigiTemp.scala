package ncreep.figi

import language.experimental.macros
import reflect.macros.Context
import reflect.api._
import ncreep.figi._

object FigiTemp {
	
//    def makeConf[A](conf: Conf): A = macro makeConfImpl[A]
//    def makeConfImpl[A](c: Context)(conf: c.Expr[Conf])(implicit tag: c.WeakTypeTag[A]): c.Expr[A] = {
//      import c.universe._
//      reify(FigiMacros.makeConf[A](conf.splice, Vector()))
//    }

  //TODO testing only
  trait Conv[+A] 
  implicit object Conv extends Conv[Nothing] 
  implicit object Conv2 extends Conv[Int] 
  
  trait Config {
    def get[A](conf: ConfNames)(implicit conv: Conv[A]): A = { println(conf); null.asInstanceOf[A] }
    def getWithDefault[A](conf: ConfNames, default: A)(implicit conv: Conv[A]): A = { println(conf + " - " + default); null.asInstanceOf[A] }
  }
  object Con extends Config
  
//  trait PartConf extends Conf[Config, PartConf] {
//	  type CC[A] = Conv[A]
//  }
//  
//  implicitly[Conv[String] =:= PartConf#CC[String]]
  
  trait ConfConf extends Conf[Config, ConfConf] {
    type CC[A] = Conv[A]
    def get[A](conf: Config, confNames: ConfNames)(implicit conv: Conv[A]): A = conf.get(confNames)
    def getWithDefault[A](conf: Config, confNames: ConfNames, default: A)(implicit conv: Conv[A]): A = conf.getWithDefault(confNames, default)
  }
  
  implicit val confConf = new ConfConf {}
  
  val inst = new InstanceWithConf(Con: Config)(confConf)
  
  def makeConf = Figi.makeConf[Foo](inst)

  trait Foo {
    def a(): Int
//    val b: Double
//    def c: List[Int]
//    def d(c: Int): Double
//
//    def baz: Baz
//    def qux: Qux
//    val bar: Bar
  }

  import scala.language.implicitConversions
  trait Bar extends ConfChainer {
    def a: Int
  }
  implicit class QuxToConf(q: Qux) extends ConfChainer
  trait Qux
  implicit def bazToConf(baz: Baz) = new ConfChainer {}
  trait Baz

}