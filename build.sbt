name := "figi"

version := "0.1"

// scalaVersion := "2.10.2"

// libraryDependencies ++= Seq(
//   "org.scala-lang" % "scala-reflect" % "2.10.2"
// )


scalaVersion := "2.10.2-SNAPSHOT"

scalaOrganization := "org.scala-lang.macro-paradise"

libraryDependencies <+= (scalaVersion)("org.scala-lang.macro-paradise" % "scala-reflect" % _)


// scalacOptions ++= Seq("-Xexperimental")

resolvers += ScalaToolsSnapshots

initialCommands in console := """import scala.reflect.runtime.universe._;import ncreep.figi.Figi._;import ncreep.figi._;import Bugs._""".stripMargin
//initialCommands in console := """import Bugs._""".stripMargin