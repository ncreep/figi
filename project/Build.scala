import sbt._
import Keys._

object BuildSettings {
  val buildSettings = Defaults.defaultSettings ++ Seq(
	version := "0.1",
    scalaVersion := "2.10.3",
    scalaOrganization := "org.scala-lang",
	scalacOptions  ++= Seq("-unchecked", "-deprecation", "-feature"),
    resolvers += Resolver.sonatypeRepo("snapshots"),
	addCompilerPlugin("org.scala-lang.plugins" % "macro-paradise" % "2.0.0-SNAPSHOT" cross CrossVersion.full)
  )
  
  val buildTestSettings = buildSettings ++ Seq(
	libraryDependencies ++= Seq(
		"org.specs2" %% "specs2" % "2.1.1" % "test",
		"junit" % "junit" % "4.8.1" % "test"
	),
	resolvers ++= Seq("snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
	                  "releases"  at "http://oss.sonatype.org/content/repositories/releases")
// Doesn't seem to work with macros
//		scalacOptions in Test ++= Seq("-Yrangepos"),
  )
}

object FigiBuild extends Build {
  import BuildSettings._

  lazy val figi: Project = Project(
    "figi",
    file("."),
    settings = buildSettings ++ Seq(
      run <<= run in Compile in core
    )
  ) aggregate(macros, core, configrity)

  lazy val macros: Project = Project(
    "figi-macros",
    file("macros"),
    settings = buildSettings ++ Seq(
      libraryDependencies <+= (scalaVersion)("org.scala-lang" % "scala-reflect" % _),
      initialCommands in console := """import scala.reflect.runtime.universe._;import ncreep.figi.Figi._;import ncreep.figi._;""".stripMargin
    )
  )

  lazy val core: Project = Project(
    "figi-core",
    file("core"),
    settings = buildTestSettings
  ) dependsOn(macros)
  
  lazy val configrity: Project = Project(
    "figi-configrity",
    file("configrity"),
    settings = buildTestSettings ++ Seq(
	  libraryDependencies ++= Seq(
	    "org.streum" %% "configrity-core" % "1.0.0"
	  )
	)
  ) dependsOn(core)
}