package ncreep

import util.Try

package object figi {
  type ConfName = String
  type ConfNames = Vector[String]
  type ConfValue = String

  trait ConfConverter[+A] {
    def apply(conf: Try[ConfValue]): A
  }

  trait Conf {
    def get[A](conf: ConfNames)(implicit conv: ConfConverter[A]): A
    def getWithDefault[A](conf: ConfNames, default: A)(implicit conv: ConfConverter[A]): A
  }

  /** Marker trait for configuration types that should chain `Figi.makeConf` invocations. */
  trait ConfChainer
}