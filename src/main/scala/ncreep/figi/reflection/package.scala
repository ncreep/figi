//package ncreep.figi
//import scala.reflect.mirror._
//
//package object reflection {
//  /** Designates the type of a configuration setting. */
//  type ConfSetting = String
//  /** Designates the type of a configuration value. */
//  type ConfValue = String
//  type MethodName = String
//  /** A converter between a configuration value and a single value. */
//  type ConfigConverter = ConfValue => Any
//  /** A converter between a value and a higher kind (* -> *). */
//  type HigherKindConverter = Any => Any //TODO
//  
//  private[reflection] type Typed[+A] = (Type, A)
//  private[reflection] type ConversionResult = Either[Exception, Any]
//}