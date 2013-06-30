package ncreep

import util.Try

package object figi {
  type ConfName = String
  type ConfNames = Vector[String]
  type ConfValue = String

  /** Converter between configuration names and configuration values. */
  trait ConfConverter[+A] {
    def apply(conf: Try[ConfValue]): A
  }

  /** And abstraction for a configuration data type. */
  trait Conf {
    /** @return The value that corresponds to the give configuration key. */
    def get[A](conf: ConfNames)(implicit conv: ConfConverter[A]): A
    /** 
     *  @return The value that corresponds to the give configuration key, or the default in case the key is missing. 
     */
    def getWithDefault[A](conf: ConfNames, default: A)(implicit conv: ConfConverter[A]): A
  }

  /** A marker trait for configuration types that should chain `Figi.makeConf` invocations. */
  trait ConfChainer
  
  /** A marker type class for configuration types that should chain `Figi.makeConf` invocations. */
  trait IsConfChainer[+T]
}