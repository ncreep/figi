package ncreep.figi

import util.Try
import ncreep.figi._
import Figi._
import org.specs2.mutable._
import org.junit.runner._
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class FigiSpecs extends Specification {
  
  trait Conv[A] { def apply(c: ConfValue): A }
  implicit object strConv extends Conv[String] { def apply(conf: ConfValue) = conf }
  implicit object intConv extends Conv[Int] { def apply(conf: ConfValue) = Integer.parseInt(conf) }

  type MapSS = Map[String, String]
  trait MapConf extends Conf[MapSS, MapConf] {
    type CC[A] = Conv[A]
    
    def mkStr(confNames: ConfNames) = confNames mkString "."
    def get[A](conf: MapSS, confNames: ConfNames)(implicit conv: Conv[A]): A = conv(conf(mkStr(confNames)))
    def getWithDefault[A](conf: MapSS, confNames: ConfNames, default: A)(implicit conv: Conv[A]): A = 
      conf.get(mkStr(confNames)).map(conv.apply _).getOrElse(default)
  }
  implicit val mapConf = new MapConf { }
  implicit def toConf(m: MapSS): InstanceWithConf[MapSS, MapConf] = 
    new InstanceWithConf(m)(mapConf)
  
  val cnf = Map[String, String](
      "a" -> "1", "b" -> "2",
      "c" -> "3", "a.b" -> "4", 
      "a.d.a" -> "5", "a.d.h" -> "6")

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
        
        trait Qux { def qux(d: String = "brr"): String }
        makeConf[Qux](cnf).qux() mustEqual "brr"
      }

      s"chaining for subclasses of ${classOf[ConfChainer].getSimpleName}" in {
        trait Foo { def a: Bar }
        trait Bar extends ConfChainer { def b: String }

        makeConf[Foo](cnf).a.b mustEqual "4"
      }

      s"chaining for values instances of the typeclass ${classOf[IsConfChainer[_]].getSimpleName}" in {
        trait Foo { def a: Bar }
        trait Bar { def b: String }
        implicit object barConfChainer extends IsConfChainer[Bar]

        makeConf[Foo](cnf).a.b mustEqual "4"
      }

      "1 arg defs as default valued properties" in {
        trait Foo { def a(d: String): String }
        makeConf[Foo](cnf).a("boo") mustEqual "1"

        trait Bar { def baz(d: String): String }
        makeConf[Bar](cnf).baz("boo") mustEqual "boo"
      }
    }
    
    "do nothing when no abstract methods are present" in {
      trait Foo { def a = 5 }
       makeConf[Foo](cnf).a mustEqual 5
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
    
    "all together now..." in {
      trait Foo { def a: Bar; val b: String}
      trait Bar extends ConfChainer { def a(): Int; val c = 13; def d: Baz}
      trait Baz {val a: Int; def f: String; val g = 12; def h(d: String): String; def k(d: Int): Int}
      implicit object bazConfChainer extends IsConfChainer[Baz]
      
      val foo = makeConf[Foo](cnf)
      
      foo.b mustEqual "2"
      foo.a.a must throwA[NoSuchElementException]
      foo.a.c mustEqual 13
      foo.a.d.a mustEqual 5
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
//      //TODO this test gives a cryptic compilation error
//      "an implicit converter is missing" in {
//        trait Foo { def a: Any }
//        makeConf[Foo](cnf)  mustEqual ???
//      }
//    }
//  }

}