import sbt._

object Deps {
  //lazy val logBack = "ch.qos.logback" % "logback-classic" % "1.2.3"
  //lazy val logging = "com.typesafe.scala-logging" % "scala-logging_2.13" % "3.9.4"
  //https://github.com/OhSirius/scala-dev-mooc-2021-03/blob/master/project/Dependencies.scala
  object V {
    lazy val KindProjector = "0.13.0"
    lazy val Logback = "1.2.6"
    lazy val Doobie = "0.13.4"
    lazy val Tapir = "0.17.4" //"0.18.3"
    lazy val TelegramBot = "0.5.1"
    lazy val Zio = "1.0.11"
    lazy val Postgres = "42.2.20"
    lazy val Liquibase = "3.4.2"
    lazy val Pureconfig = "0.12.3"
  }

  //pure
  lazy val pureconfig: Seq[ModuleID] = Seq(
    "com.github.pureconfig" %% "pureconfig" % V.Pureconfig,
    "com.github.pureconfig" %% "pureconfig-cats-effect" % V.Pureconfig)

  //kind
  lazy val kindProjector =
    ("org.typelevel" %% "kind-projector" % V.KindProjector).cross(CrossVersion.full)

  // logging
  lazy val logback: Seq[ModuleID] = Seq(
    "ch.qos.logback" % "logback-classic" % V.Logback,
    "net.logstash.logback" % "logstash-logback-encoder" % "6.6")

  //liquibase
  lazy val liquibase = "org.liquibase" % "liquibase-core" % V.Liquibase

  // postgres
  lazy val postgres = "org.postgresql" % "postgresql" % V.Postgres

  //doobie
  lazy val doobie: Seq[ModuleID] = Seq(
    "org.tpolecat" %% "doobie-core" % V.Doobie,
    "org.tpolecat" %% "doobie-postgres" % V.Doobie,
    "org.tpolecat" %% "doobie-hikari" % V.Doobie,
    ("org.tpolecat" %% "doobie-quill" % V.Doobie).exclude("org.slf4j", "*")
  )

  //tapir
  lazy val commonTapir: Seq[ModuleID] = Seq(
    "com.softwaremill.sttp.tapir" %% "tapir-zio" % V.Tapir,
    "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % V.Tapir,
    "io.circe" %% "circe-generic" % "0.13.0"
  )

  lazy val clientTapir: Seq[ModuleID] = Seq(
    "com.softwaremill.sttp.tapir" %% "tapir-zio" % V.Tapir,
    "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % V.Tapir,
    "com.softwaremill.sttp.tapir" %% "tapir-sttp-client" % V.Tapir
  )

  lazy val serverTapir: Seq[ModuleID] = Seq(
    "com.softwaremill.sttp.tapir" %% "tapir-zio" % V.Tapir,
    "com.softwaremill.sttp.tapir" %% "tapir-zio-http4s-server" % V.Tapir,
    "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % V.Tapir,
    "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-http4s" % V.Tapir,
    "com.softwaremill.sttp.tapir" %% "tapir-openapi-docs" % V.Tapir,
    "com.softwaremill.sttp.tapir" %% "tapir-openapi-circe" % V.Tapir,
    "com.softwaremill.sttp.tapir" %% "tapir-openapi-circe-yaml" % V.Tapir
    //"com.softwaremill.sttp.tapir" %% "tapir-http4s-client" % V.Tapir
  )

  //bot
  lazy val bot =
    "org.augustjune" %% "canoe" % V.TelegramBot //.exclude("org.http4s", "*") //:http4s-dsl

  //zio
  lazy val zio: Seq[ModuleID] = Seq(
    "dev.zio" %% "zio" % V.Zio,
    "dev.zio" %% "zio-interop-cats" % "2.5.1.0", //"3.1.1.0",
    "dev.zio" %% "zio-logging-slf4j" % "0.5.12",
    //"dev.zio" %% "zio-logging-slf4j-bridge" % "0.5.12",
    "dev.zio" %% "zio-test" % V.Zio, //1.0.8
    //"dev.zio" %% "zio-test-sbt" % V.Zio,
    "dev.zio" %% "zio-macros" % V.Zio
  )

}
