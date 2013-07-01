package ncreep

import scala.language.higherKinds
import util.Try

package object figi {
  type ConfName = String
  type ConfNames = Vector[String]
  type ConfValue = String

  /** A type class for a configuration data type. 
   *  
   *  See an implementation example in [[ncreep.figi.FigiSpecs]].
   *  
   * @tparam C The type of configuration being used.
   * @tparam CC A type for the configuration converter, typically: ConfValue => A  
   * @tparam Self The type of self, to help the compiler cope... (this should 
   * provide a stable value of the type `CC`. Note that this
   * is cyclic, so no way to define `object`s directly inheriting this thing).
   */
  trait Conf[C, Self <: Conf[C, Self]] {
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
  implicit class InstanceWithConf[C, CC <: Conf[C, CC]](val config: C)(implicit val confTypeClass: CC)
  
  //TODO figure out how to make this global conversion work
//  import scala.language.implicitConversions
//  implicit def tooInstanceWithConf[C, CC <: Conf[C, CC]](config: C)(implicit confTypeClass: CC): InstanceWithConf[C, CC] = 
//    new InstanceWithConf[C, CC](config)
  
  /** A marker trait for configuration types that should chain `Figi.makeConf` invocations. */
  trait ConfChainer
  
  /** A marker typeclass for configuration types that should chain `Figi.makeConf` invocations. */
  trait IsConfChainer[+A]
}