//package ncreep.figi.reflection
//
//import scala.reflect.mirror._
//
////TODO make package private
//object ReflectionUtil {
//  /** @return A type instance for the parameterization of the method. */
////  def typeOf[A : Manifest]: Type = implicitly[Manifest[A]].tpe
//  /** @return A type instance for the parameterization of the method. */
//  
////  def typeOfKind[A[_]](implicit m: ClassManifest[A[_]]): Type = m.tpe
//  
//  
//  //RM
//  /** @return A predicate that returns true for types the conform to the given type argument. */
//  def conforms(thisType: Type) = (thatType: Type) => thatType <:< thisType
//
//  //TODO find a way to avoid the wrappers
//  
//  /** A wrapper to avoid errors related to erasure. */
//  object NullaryMethodTypeWrapper {
//    def unapply(tpe: Type): Option[Type] = {
//      tpe match {
//        case NullaryMethodType(t) => Some(t)
//        case _ => None
//      }
//    }
//  }
//
//  /** A wrapper to avoid errors related to erasure. */
//  object MethodTypeWrapper {
//    def unapply(tpe: Type): Option[(List[Symbol], Type)] = {
//      tpe match {
//        case MethodType(params, t) => Some((params, t))
//        case _ => None
//      }
//    }
//  }
//}
//
