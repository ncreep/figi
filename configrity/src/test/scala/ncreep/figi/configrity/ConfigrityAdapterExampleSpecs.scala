package ncreep.figi.configrity

import org.specs2.mutable._
import org.junit.runner._
import org.specs2.runner.JUnitRunner
import java.io.File

@RunWith(classOf[JUnitRunner])
class ConfigrityAdapterExampleSpecs extends Specification {
  "The figi configrity adapter" should {
    "have a test that resembles a real usage example" in {
      // basic figi imports
      import ncreep.figi._

      // required for Configrity support
      import ncreep.figi.configrity._
      import org.streum.configrity._
      import org.streum.configrity.converter.Extra._

      // the traits specifying the settings we need
      // excepts chained types, all types must have implicit converters in scope
      trait Setup {
        val user: String
        val pass: Int
        def port(default: Int = 80): Int
        def missing: Int // using a val here will fail fast when generating a setup instance
        val dev: Flags
        val prod: Flags
        def upCaseUser = user.toUpperCase // will not be overridden by the macro
      }

      trait Flags {
        val log: Boolean
        val root: File
      }
      
      // tells the compiler that the Flags trait can be chained
      implicit object flagsIsConfChainer extends IsConfChainer[Flags]

      val base = Configuration(Map("user" -> "figi", "pass" -> "1234"))
      val dev = Configuration(Map("log" -> "true", "root" -> "/dev/root/"))
      val prod = Configuration(Map("log" -> "false", "root" -> "/prod/root/"))
      
      val cnf = base.attach("dev", dev).attach("prod", prod)
      
      // populating the fields of Setup instance from cnf
      val setup = Figi.makeConf[Setup](cnf)
      
      setup.user mustEqual "figi"
      setup.pass mustEqual 1234
      setup.port() mustEqual 80
      setup.port(8080) mustEqual 8080
      setup.missing must throwA[NoSuchElementException]
      setup.dev.log mustEqual true
      setup.dev.root mustEqual new File("/dev/root")
      setup.prod.log mustEqual false
      setup.prod.root mustEqual new File("/prod/root")
      setup.upCaseUser mustEqual "FIGI"
    }
  }
}