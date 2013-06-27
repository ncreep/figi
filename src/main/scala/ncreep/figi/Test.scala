//package ncreep.figi
//
//import scala.reflect.mirror._
//import Figi._
//
//object Test {
//
//  Block(List(ModuleDef(Modifiers(), newTermName("Foo"), Template(
//    List(Ident(newTypeName("Object"))), emptyValDef, List(
//      DefDef(Modifiers(), newTermName("a"), List(), List(List()), Ident(newTypeName("String")), Literal(Constant(""))),
//      DefDef(Modifiers(), newTermName("b"), List(), List(), Ident(newTypeName("Int")), Literal(Constant(3))),
//      ValDef(Modifiers(), newTermName("c"), Ident(newTypeName("Nothing")), Apply(Select(Select(Select(Ident(newTermName("scala")), newTermName("sys")), newTermName("package")), newTermName("error")), List(Literal(Constant("b"))))))))), Literal(Constant(())))
//
//  Apply(
//    Apply(
//      TypeApply(
//        Select(
//          Select(
//            Select(
//              Ident(newTermName("ncreep")),
//              newTermName("figi")),
//            newTermName("Conf")),
//          newTermName("get")),
//        List(Ident(newTypeName("Int")))),
//      List(Apply(Select(Select(This(newTypeName("immutable")), newTermName("List")), newTermName("apply")), List(Literal(Constant("abc")))))),
//    List(Literal(Constant(null))))
//
//  Block(List(ModuleDef(Modifiers(), newTermName("A"), Template(List(Ident(newTypeName("Object"))), emptyValDef,
//    List(
//
//      DefDef(Modifiers(), newTermName("<init>"), List(), List(List()),
//        TypeTree(), Block(List(Apply(Select(Super(This(newTypeName("")), newTypeName("")), newTermName("<init>")), List())), Literal(Constant(())))))))), Literal(Constant(())))
//}
//
//
