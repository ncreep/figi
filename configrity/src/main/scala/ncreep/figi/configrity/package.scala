package ncreep.figi

import org.streum.configrity._
import converter._

/** Provides figi adapters for [[org.streum.configrity.Configrity]]. */
package object configrity {
  trait ConfigrityAdapter extends Conf[Configuration, ConfigrityAdapter] {
    type CC[A] = ValueConverter[A]

    def mkStr(confNames: ConfNames) = confNames mkString "."
    
    def get[A](conf: Configuration, confNames: ConfNames)(implicit conv: ValueConverter[A]): A = conf[A](mkStr(confNames))
    def getWithDefault[A](conf: Configuration, confNames: ConfNames, default: A)(implicit conv: ValueConverter[A]): A = 
     conf[A](mkStr(confNames), default)
  }
  
  implicit val configrity = new ConfigrityAdapter {}
  
  import scala.language.implicitConversions
  implicit def toConf(c: Configuration): InstanceWithConf[Configuration, ConfigrityAdapter] = new InstanceWithConf(c)
  
}