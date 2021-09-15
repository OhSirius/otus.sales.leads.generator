import sbt._

object Deps {
  //lazy val logBack = "ch.qos.logback" % "logback-classic" % "1.2.3"
  //lazy val logging = "com.typesafe.scala-logging" % "scala-logging_2.13" % "3.9.4"
  //https://github.com/OhSirius/scala-dev-mooc-2021-03/blob/master/project/Dependencies.scala
  object V {
    lazy val KindProjector = "0.10.3"
    lazy val Logback = "1.2.5"
    lazy val Doobie = "0.8.8"
  }

  lazy val kindProjector = "org.typelevel" %% "kind-projector" % V.KindProjector

  // logging
  lazy val logback = "ch.qos.logback" % "logback-classic" % V.Logback

  //doobie
  lazy val doobie: Seq[ModuleID] = Seq(
    "org.tpolecat" %% "doobie-core" % V.Doobie,
    "org.tpolecat" %% "doobie-postgres" % V.Doobie,
    "org.tpolecat" %% "doobie-hikari" % V.Doobie,
    "org.tpolecat" %% "doobie-quill" % V.Doobie
  )

}
