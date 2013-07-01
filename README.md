# Figi 

Figi is a small macro based utility for Scala 2.10 that allows you to easily create simple type safe wrappers for configuration objects.

Currently, Figi comes with built in support for the [Configrity](https://github.com/paradigmatic/Configrity) library.

Figi relies on [Macro Paradise for 2.10](http://docs.scala-lang.org/overviews/macros/paradise.html#macro_paradise_for_210x) for its macro implementation.

## Rationale


Say we want to read configuration data relying on some configuration library. The data contains:
```
user = figi
pass = 1234
```

We might model it as the following trait:

```scala
trait Setup {
  def user: String
  def pass: Int
}
```

And use that definition to pass around our code. At some point though, we will have to populate the members of a `Setup` instance:

```scala
val config = ...// read config from a file or something
val setup = new Setup {
  def user = config[String]("user")
  def pass = config[Int]("pass")
}
```

Figi saves you the trouble of manually writing this boilerplate, and generates the code using a `def` macro, like so:
```scala
val config = // read config from a file or something
val setup = Figi.makeConf[Setup]
```
And we can continue using our `Setup` instance as usual.

## Features
- Provide your own custom configuration traits, requires no dependencies on Figi
- Code generation for `val`s and `def`s
- Code generation ignores any non abstract methods
- Default values for missing keys
- Chaining configuration traits (see example)
- Easily extend support for your favorite configuration library (example [here](https://github.com/ncreep/figi/blob/master/configrity/src/main/scala/ncreep/figi/configrity/package.scala))
- Support for arbitrary converters provided by the configuration library
- Support for [Configrity](https://github.com/paradigmatic/Configrity) out of the box

As Figi relies on macros to generate code, it may output some scary compilation error messages when something goes wrong; though a special effort was made to generate intelligible error messages where possible. 

In any case, don't try being too smart when defining your trait.

## Example

What follows is a more complete example based on the Configrity library. The full code can be found [here](https://github.com/ncreep/figi/blob/master/configrity/src/test/scala/ncreep/figi/configrity/ConfigrityAdapterExampleSpecs.scala).

```scala
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
```