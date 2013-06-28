package ncreep.figi

import util.Try
import ncreep.figi._
import Figi._
import org.specs2.mutable._
import org.junit.runner._
import org.specs2.runner.JUnitRunner
import languageFeature.implicitConversions

@RunWith(classOf[JUnitRunner])
class FigiSpecs extends Specification {
  implicit object strConv extends ConfConverter[String] { def apply(conf: Try[ConfValue]) = conf.get }
  implicit object intConv extends ConfConverter[Int] { def apply(conf: Try[ConfValue]) = Integer.parseInt(conf.get) }

  object cnf extends Conf {
    val vals = Map[String, String](
      "a" -> "1", "b" -> "2",
      "c" -> "3", "a.b" -> "4", 
      "a.d.a" -> "5", "a.d.h" -> "6")
    def get[A](conf: ConfNames)(implicit conv: ConfConverter[A]) = conv(Try(vals(conf.mkString("."))))
    def getWithDefault[A](conf: ConfNames, default: A)(implicit conv: ConfConverter[A]) =
      Try(get[A](conf)).getOrElse(default)
  }

  "The figi macro" should {
    "support" >> {
      "support plain vals" in {
        trait Foo { val a: String }
        makeConf[Foo](cnf).a mustEqual "1"
      }

      "zero arg defs without parentheses" in {
        trait Foo { def a: String }
        makeConf[Foo](cnf).a mustEqual "1"
      }

      "zero arg defs with parentheses" in {
        trait Foo { def a(): String }
        makeConf[Foo](cnf).a mustEqual "1"
      }

      "1 arg defs as default valued properties" in {
        trait Foo { def a(d: String): String }
        makeConf[Foo](cnf).a("boo") mustEqual "1"

        trait Bar { def baz(d: String): String }
        makeConf[Bar](cnf).baz("boo") mustEqual "boo"
      }

      s"chaining for subclasses of ${classOf[ConfChainer].getSimpleName}" in {
        trait Foo { def a: Bar }
        trait Bar extends ConfChainer { def b: String }

        makeConf[Foo](cnf).a.b mustEqual "4"
      }

      s"chaining for values implicitly convertible to ${classOf[ConfChainer].getSimpleName}" in {
        trait Foo { def a: Bar }
        trait Bar { def b: String }
        implicit def toConfChainer(b: Bar) = ConfChainer

        makeConf[Foo](cnf).a.b mustEqual "4"
      }

      "1 arg defs as default valued properties" in {
        trait Foo { def a(d: String): String }
        makeConf[Foo](cnf).a("boo") mustEqual "1"

        trait Bar { def baz(d: String): String }
        makeConf[Bar](cnf).baz("boo") mustEqual "boo"
      }
    }
    
    "fail fast on missing val members" in {
      trait Foo { val abc: Int }
      makeConf[Foo](cnf) must throwA[NoSuchElementException]
    }
    
    "delayed fail on missing def members" in {
    	trait Foo { def abc: Int }
    	val foo = makeConf[Foo](cnf) 
    	foo.abc must throwA[NoSuchElementException]
    }
    
    "ignore non abstract methods" in {
       trait Foo { def a(): String; val b = 15; def c = "f"}
       val foo = makeConf[Foo](cnf)
       foo.a mustEqual "1"
       foo.b mustEqual 15
       foo.c mustEqual "f"
    }
    
    "all together now" in {
      trait Foo { def a: Bar; val b: String}
      trait Bar extends ConfChainer { def a(): Int; val c = 13; def d: Baz}
      trait Baz {val a: Int; def f: String; val g = 12; def h(d: String): String; def k(d: Int): Int}
      implicit def toConfChainer(b: Baz) = ConfChainer
      
      val foo = makeConf[Foo](cnf)
      
      foo.b mustEqual "2"
      foo.a.a must throwA[NoSuchElementException]
      foo.a.c mustEqual 13
      foo.a.d.a mustEqual 6
      foo.a.d.f must throwA[NoSuchElementException]
      foo.a.d.g mustEqual 12
      foo.a.d.k(15) mustEqual 15
      foo.a.d.h("bar") mustEqual "6"
    }

  }

/* Tests that shouldn't compile: */
//  "The figi macro " should {
//    "fail to compile when" >> {
//      "methods have more than a single arguments" in {
//        trait Foo { def a(foo: String, bar: Int): String }
//        makeConf[Foo](cnf) mustEqual ???
//      }
//      
//      "a method's argument does not match the return type" in {
//    	  trait Foo { def a(foo: Int): String }
//    	  makeConf[Foo](cnf) mustEqual ???
//      }
//      
//      "an implicit converter is missing" in {
//        trait Foo { def a: Int }
//        makeConf[Foo](cnf)  mustEqual ???
//      }
//    }
//  }

}