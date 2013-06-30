package ncreep

import scala.language.higherKinds
import util.Try

package object figi {
  type ConfName = String
  type ConfNames = Vector[String]
  type ConfValue = String

  /** A type class for a configuration data type. 
   * @tparam C The type of configuration being used.
   * @tparam CC A type for the configuration converter: ConfValue => A  
   */
  trait Conf[C, Self <: Conf[C, _]] {
    type CC[A]
    
    /** @return The value that corresponds to the given configuration key. */
    def get[A](conf: C, confNames: ConfNames)(implicit conv: Self#CC[A]): A
    
    /** 
     *  @return The value that corresponds to the given configuration key, or the default in case the key is missing. 
     */
    def getWithDefault[A](conf: C, confNames: ConfNames, default: A)(implicit conv: Self#CC[A]): A
  }
  
  /** Binds configuration values to their corresponding typeclass. 
   *  Thus avoiding type parameterization on methods the use them. 
   */
  implicit class InstanceWithConf[C](val config: C)(implicit val confTypeClass: T forSome {type T <: Conf[C, T]})
  
  /** A marker trait for configuration types that should chain `Figi.makeConf` invocations. */
  trait ConfChainer
  
  /** A marker typeclass for configuration types that should chain `Figi.makeConf` invocations. */
  trait IsConfChainer[+A]
}