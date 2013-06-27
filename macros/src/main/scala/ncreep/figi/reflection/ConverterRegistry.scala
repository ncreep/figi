package ncreep.figi.reflection


/**
 * A converter registry: maps type to converter instances.
 * An empty instance of the converter may be obtained via the companion object.
 */
//private[figi] class ConverterRegistry private (converters: List[(Type, ConfigConverter[Any])]) {
//  def getConverter(t: Type): Option[ConfigConverter[Any]] = {
//    val pred = conforms(t)
//    // this is guaranteed to contain a converter of the correct type
//    val convType = converters.find { case (t, c) => pred(t) }
//    val conv = convType.map(_._2)
//
//    conv
//  }
//
//  /** @return A new registry with the given converter added to the top of its converters list. */
//  def add[A: Manifest](conv: ConfigConverter[A]) = {
//    new ConverterRegistry((typeOf[A], conv) :: converters)
//  }
//}
//
//object ConverterRegistry {
//  def apply() = new ConverterRegistry(Nil)
//}