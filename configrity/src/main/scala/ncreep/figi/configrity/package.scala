package ncreep.figi

import org.streum.configrity._
import converter._
import util.Try

/** Provides figi adapters for [[org.streum.configrity.Configrity]]. */
package object configrity {
  implicit def configritiyConvereterAdapter[A](implicit conv: ValueConverter[A]) =
    new ConfConverter[A] {
      def apply(conf: Try[ConfValue]): A = conf.map(conv parse _).get
    }

  private def confNamesToString(confNames: ConfNames) = confNames.mkString(".")

  implicit class ConfigrityAdapter(conf: Configuration) extends Conf {
    def toVal[A](confVal: => ConfValue)(implicit conv: ConfConverter[A]): A = conv(Try(confVal))

    def get[A: ConfConverter](confNames: ConfNames): A =
      toVal(conf[ConfValue](confNamesToString(confNames)))

    def getWithDefault[A: ConfConverter](confNames: ConfNames, default: A): A = {
      val name = confNamesToString(confNames)
      if (conf contains name) toVal(conf[ConfValue](name))
      else default
    }
  }

}