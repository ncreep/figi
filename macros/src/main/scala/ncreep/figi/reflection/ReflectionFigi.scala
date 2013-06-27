//package ncreep.figi.reflection
//
//import ReflectionUtil.MethodTypeWrapper
//import ReflectionUtil.NullaryMethodTypeWrapper
//import java.lang.reflect.InvocationHandler
//import java.lang.reflect.Method
//import scala.reflect.mirror.Type
//import scala.reflect.mirror.TypeRef
//import scala.reflect.mirror.newTermName
//
//trait ConfigAdapter {
//  def apply(prop: ConfSetting): Either[Exception, ConfValue]
//}
//
//trait MethodToConf {
//  def apply(methodName: MethodName): ConfSetting
//}
//
///** A converter that can be applied to specific types (assumed to be simple, non higher kind types). */
//private[reflection] trait TypedConfigConverter {
//  /** The type associated with this converter. */
//  def selfType: Type
//  /** Normalization to be performed before comparison, default to identity. */
//  def normalize(tpe: Type) = tpe
//  
//  /** @return True if this converter is considered equivalent to the given type. */
//  def equivalent(tpe: Type): Boolean = normalize(selfType) =:= normalize(tpe)
//  /** @return True if this converter is considered to conform to the given type. */
//  def conforms(tpe: Type): Boolean = normalize(selfType) <:< normalize(tpe)
//  
//  def apply(conf: ConfValue, tpe: Type): ConversionResult
//}
//
///** A converter for plain types (*). */
//private[reflection] case class TypeConverter(val selfType: Type, conv: ConfigConverter) extends TypedConfigConverter {
//  def apply(conf: ConfValue, tpe: Type) = try {Right(conv(conf))} catch { case e: Exception => Left(e)} 
//}
//
///** A converter for type constructors of the form * -> *.
// * @param thisType Assumed to be * -> *, i.e. the method `isHigherKinded` should return true. */
//private[reflection] case class TypeToTypeConverter(
//    val selfType: Type, 
//    conv: HigherKindConverter, 
//    registry: ConverterRegistry) extends TypedConfigConverter {
//  assert(selfType.isHigherKinded, "Must provide a higher kind for type to type converter.")
//  override def normalize(tpe: Type) = tpe.typeSymbol.asTypeConstructor
//  
//  //TODO multi type parameters
//  def typeParameter(tpe: Type) = tpe match { case TypeRef(_, _, List(p)) => p }
//  def apply(conf: ConfValue, tpe: Type) =  {
//    val res = registry.convert(conf, typeParameter(tpe))
//    res.right map conv //TODO let the converter use the full conversion result
//  }
//}
//
//case class ConverterRegistryBuilder private (
//    typeConverters: List[Typed[ConfigConverter]],
//    typeToTypeConverters: List[Typed[HigherKindConverter]]) {
//  
////  def addConverter[A: Manifest](conv: ConfValue => A) = 
////    this.copy(typeConverters = (typeOf[A], conv) :: typeConverters)
//
////  def addHigherKindConverter[A[_]](conv: Any => A[_])(implicit m: ClassManifest[A[_]]) = {
////     val TypeRef(_, t, p) = typeOfKind[A]; println(t); println(p); //RM
////	  this.copy(typeToTypeConverters = (typeOfKind[A], conv) :: typeToTypeConverters)
////  }
//
//  def build() = ConverterRegistry(typeConverters, typeToTypeConverters) 
//}
//
//object ConverterRegistryBuilder extends ConverterRegistryBuilder(Nil, Nil)
//
//private[reflection] case class ConverterRegistry(    
//    typeConverters: List[Typed[ConfigConverter]],
//    typeToTypeConverters: List[Typed[HigherKindConverter]]) {
//  
//  private def missingConv(tpe: Type) = new IllegalArgumentException("Missing converter for type: " + tpe)
//  
//  private val simpleConverters: List[TypedConfigConverter] = typeConverters map { case (t, c) => new TypeConverter(t, c) }
//  private val complexConverters: List[TypedConfigConverter] = typeToTypeConverters map { case (t, c) => new TypeToTypeConverter(t, c, this) }
//  /** Simple converters have higher priority. */
//  private val converters: List[TypedConfigConverter] = simpleConverters ::: complexConverters
//  
//  def equivConv(tpe: Type) = converters find (_ equivalent tpe)
//  def conformConv(tpe: Type) = converters find (_ conforms tpe)
//  /** Trying to find a converter, priority for equivalent converters. */
//  def converter(tpe: Type) = equivConv(tpe).orElse(conformConv(tpe))
//    
//  def convert(conf: ConfValue, tpe: Type): ConversionResult = for {
//      conv <- converter(tpe).toRight(missingConv(tpe)).right
//      res <- conv(conf, tpe).right
//    } yield res
//}
//
///** A marker interface for configuration settings that should be chained. */
//trait Conf
//
///** A chaining strategy for configuration names.*/
//trait ConfChainer {
//  def apply(confNames: Vector[ConfSetting]): ConfSetting
//}
//
//private[reflection] case class Config(
//  implType: Type,
//  prefix: Vector[ConfSetting],
//  conf: ConfigAdapter,
//  converters: ConverterRegistry,
//  methodToConf: MethodToConf,
//  confChainer: ConfChainer) {
//  
//  def getHandler() = new InvocationHandler {
//    def invoke(invoke: Object, method: Method, arg: Array[Object]): Object = {
//      val methName = method.getName
//      println(methName) //RM
//      val fullMethodType = implType.member(newTermName(methName)).asType 
//      
//      val methodType = fullMethodType match {
//    	case NullaryMethodTypeWrapper(t) => t 
//        case MethodTypeWrapper(Nil, t) => t
//        case _ => sys.error("Only methods that take no arguments are allowed as configuration.")
//      }
//      
//      val confName = methodToConf(confChainer(prefix :+ methName))
//      val confVal = conf(confName)
//      val result =
//        for {
//          value <- confVal.right
//          converted <- converters.convert(value, methodType).right
//        } yield converted
//      
//      result match { //TODO proper handling
//        case Right(o) => o.asInstanceOf[Object]
//        case Left(e) => throw e
//      }
//    }
//  }
//}
//
////object Config {
////  def apply[A <: AnyRef: Manifest](
////    prefix: Vector[ConfSetting],
////    conf: ConfigAdapter,
////    converters: ConverterRegistry,
////    methodToConf: MethodToConf,
////    confChainer: ConfChainer): A = {
////
////    val implType = typeOf[A]
////    //TODO This won't work on further chaining
////    val handler = Config(implType, prefix, conf, converters, methodToConf, confChainer).getHandler()
////
////    val clazz = typeToClass(implType)
////    Proxy.newProxyInstance(clazz.getClassLoader, Array(clazz), handler).asInstanceOf[A]
////  }
////}
//
//object ReflectionFigi {
//
//  trait Test {
//    def p1(): Int
//    def p2: String
//    def p3: Option[Any]
//    val p4: Option[Int]
//    def p5: Either[Exception, String]
//    def p6: String = "bar"
//  }
//
//  val adap = new ConfigAdapter {
//    def apply(prop: String) = {
//      prop match {
//        case "p1" => Right("1")
//        case "p2" => Right("foo")
//        case "p3" => Right("2")
//        case "p4" => Right("3")
//        case "p5" => Right("bar")
//        case "p6" => Right("quz")
//        case _ => throw new AssertionError("wrong prop")
//      }
//    }
//  }
//
//  val id = (s: ConfSetting) => s
//  val num = (s: ConfSetting) => java.lang.Integer.parseInt(s)
////  object opt extends TypeToTypeConverterHelper { type A = Option[_]; def apply[B](value: B) = Option(value)}
//  val opt = (v: Any) => Option(v)
//  type E[A] = Either[Exception, A]
//  val either: Any => E[_] = (v: Any) => Right(v)
////  val convs = ConverterRegistryBuilder addConverter(id) addConverter(num) addHigherKindConverter(opt) addHigherKindConverter(either) 
//  val methToConf = new MethodToConf { def apply(s: MethodName) = s }
//  val confChainer = new ConfChainer { def apply(confNames: Vector[ConfSetting]) = confNames.mkString(".") }
////  val conf = Config[Test](Vector(), adap, convs.build(), methToConf, confChainer)
//
////  def main(args: Array[String]) {
////
////    val p1 = conf.p1
////    val p2 = conf.p2
////    val p3 = conf.p3
////    val p4 = conf.p4
//////    val p5 = conf.p5
////    println(conf.p6)
////
////    println(p1 + " - " + typeOfInstance(p1))
////    println(p2 + " - " + typeOfInstance(p2))
////    println(p3 + " - " + typeOfInstance(p3))
////    println(p4 + " - " + typeOfInstance(p4))
//////    println(p5 + " - " + typeOfInstance(p5))
////  }
//  
////  def run {
////    main(null)
////  }
//
//}