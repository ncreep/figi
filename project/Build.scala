import sbt._
import Keys._

object BuildSettings {
  val buildSettings = Defaults.defaultSettings ++ Seq(
	version := "0.1",
    scalaVersion := "2.10.2",
    scalaOrganization := "org.scala-lang",
    resolvers += Resolver.sonatypeRepo("snapshots")
  )
}

object FigiBuild extends Build {
  import BuildSettings._

  lazy val figi: Project = Project(
    "figi",
    file("."),
    settings = buildSettings
  ) aggregate(figiMacros, core)

  lazy val figiMacros: Project = Project(
    "figi-macros",
    file("macros"),
    settings = buildSettings ++ Seq(
      // NOTE: macros are compiled with macro paradise 2.10
      scalaVersion := "2.10.2-SNAPSHOT",
      scalaOrganization := "org.scala-lang.macro-paradise",
      libraryDependencies <+= (scalaVersion)("org.scala-lang.macro-paradise" % "scala-reflect" % _)
    )
  )

  lazy val core: Project = Project(
    "figi-core",
    file("core"),
    settings = buildSettings ++ Seq(
		libraryDependencies += "org.specs2" %% "specs2" % "2.1-SNAPSHOT" % "test",
		scalacOptions in Test ++= Seq("-Yrangepos"),
		resolvers ++= Seq("snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
                    	  "releases"  at "http://oss.sonatype.org/content/repositories/releases")
	)
  ) dependsOn(figiMacros)
}