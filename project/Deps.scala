import sbt._

object Deps {
  //lazy val logBack = "ch.qos.logback" % "logback-classic" % "1.2.3"
  //lazy val logging = "com.typesafe.scala-logging" % "scala-logging_2.13" % "3.9.4"
  //https://github.com/OhSirius/scala-dev-mooc-2021-03/blob/master/project/Dependencies.scala
  object V {
    lazy val KindProjector = "0.13.0"
    lazy val Logback = "1.2.6"
    lazy val Doobie = "0.13.4"
    lazy val Tapir = "0.18.3"
    lazy val TelegramBot = "0.5.1"
    lazy val Zio = "1.0.11"
  }

  lazy val kindProjector =
    ("org.typelevel" %% "kind-projector" % V.KindProjector).cross(CrossVersion.full)

  // logging
  lazy val logback = "ch.qos.logback" % "logback-classic" % V.Logback

  //doobie
  lazy val doobie: Seq[ModuleID] = Seq(
    "org.tpolecat" %% "doobie-core" % V.Doobie,
    "org.tpolecat" %% "doobie-postgres" % V.Doobie,
    "org.tpolecat" %% "doobie-hikari" % V.Doobie,
    "org.tpolecat" %% "doobie-quill" % V.Doobie
  )

  //tapir
  lazy val tapir: Seq[ModuleID] = Seq(
    "com.softwaremill.sttp.tapir" %% "tapir-zio" % V.Tapir,
    "com.softwaremill.sttp.tapir" %% "tapir-zio-http4s-server" % V.Tapir,
    "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % V.Tapir,
    "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-http4s" % V.Tapir,
    "com.softwaremill.sttp.tapir" %% "tapir-openapi-docs" % V.Tapir,
    "com.softwaremill.sttp.tapir" %% "tapir-openapi-circe" % V.Tapir,
    "com.softwaremill.sttp.tapir" %% "tapir-openapi-circe-yaml" % V.Tapir
  )

  //bot
  lazy val bot = "org.augustjune" %% "canoe" % V.TelegramBot

  //zio
  lazy val zio: Seq[ModuleID] = Seq(
    "dev.zio" %% "zio" % V.Zio,
    "dev.zio" %% "zio-interop-cats" % "2.5.1.0", //"3.1.1.0",
    //"dev.zio" %% "zio-logging-slf4j" % "0.5.6",
    "dev.zio" %% "zio-test" % V.Zio, //1.0.8
    //"dev.zio" %% "zio-test-sbt" % V.Zio,
    "dev.zio" %% "zio-macros" % V.Zio
  )

}
