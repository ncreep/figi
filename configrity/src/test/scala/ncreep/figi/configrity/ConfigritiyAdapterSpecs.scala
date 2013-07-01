package ncreep.figi.configrity

import util.Try
import ncreep.figi._
import Figi._
import org.specs2.mutable._
import org.junit.runner._
import org.specs2.runner.JUnitRunner
import org.streum.configrity._
import ncreep.figi.configrity._

@RunWith(classOf[JUnitRunner])
class ConfigritiyAdapterSpecs extends Specification {

  val data = Map(
      "a" -> "1", "b" -> "foo", "e" -> "2", "f" -> "3", "g" -> "4",
      "c" -> "false", "d" -> "[1, 2, 3, 4]", "h" -> "5", "i" -> "6",
      "file" -> "/some/folder/")
  val cnf = Configuration(data)

  "The figi configrity adapter" should {
    "fetch" >> {
      
      "numbers" in {
        trait Foo1 { val a: Byte }
        makeConf[Foo1](cnf).a mustEqual (1: Byte)
        trait Foo2 { val e: Short }
        makeConf[Foo2](cnf).e mustEqual (2: Short)
        trait Foo3 { val f: Int }
        makeConf[Foo3](cnf).f mustEqual 3
        trait Foo4 { val g: Long }
        makeConf[Foo4](cnf).g mustEqual 4L
        trait Foo5 { val h: Float }
        makeConf[Foo5](cnf).h mustEqual 5F
        trait Foo6 { val i: Double }
        makeConf[Foo6](cnf).i mustEqual 6D
      }
      
      "strings" in {
        trait Foo { val b: String }
        makeConf[Foo](cnf).b mustEqual "foo"
      }
      
      "booleans" in {
    	  trait Foo { val c: Boolean }
    	  makeConf[Foo](cnf).c mustEqual false
      }
      
      "lists" in {
    	  trait Foo { val d: List[Int] }
    	  makeConf[Foo](cnf).d mustEqual List(1, 2, 3, 4)
      }
    }
    
    "honor other implicit conversions in scope" in {
      import java.io.File
      import org.streum.configrity.converter.Extra._
      
       trait Foo { def file: File }
       makeConf[Foo](cnf).file mustEqual new File("/some/folder/")
    }
  }
  
  // missing, default values, Option
}