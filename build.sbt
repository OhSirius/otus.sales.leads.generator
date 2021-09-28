import Deps._
import com.permutive.sbtliquibase.SbtLiquibase

//Базовые настройки
lazy val _version = "0.1"
lazy val _scalaVersion = "2.13.6"
lazy val _idePackagePrefix = Some("ru.otus.sales.leads.generator")
lazy val _scalacOptions = Seq(
  "-deprecation",
  "-Ymacro-annotations"
  //"-language:postfixOps"
  //      "-target:jvm-1.8",
  //      "-encoding", "UTF-8",
  //      "-unchecked",
  //      "-deprecation",
  //      "-Xfuture",
  //      "-Yno-adapted-args",
  //      "-Ywarn-dead-code",
  //      "-Ywarn-numeric-widen",
  //      "-Ywarn-value-discard",
  //      "-Ywarn-unused"
)

//Настройка webApi проекта
lazy val webApi = (project in file("apps/api"))
  .settings(
    name := "otus.sales.leads.generator.app.api",
    mainClass := Some("ru.otus.sales.leads.generator.apps.api.WebApp"),
    version := _version,
    scalaVersion := _scalaVersion,
    idePackagePrefix := _idePackagePrefix.map(_ + ".apps.api"),
    addCompilerPlugin(kindProjector),
    scalacOptions ++= _scalacOptions,
    libraryDependencies ++= serverTapir,
    libraryDependencies ++= zio,
    libraryDependencies ++= pureconfig,
    libraryDependencies ++= logback,
    //assembly / test := {},
    assemblyJarName := s"otus-sales-leads-generator-app-api.jar",
    assemblyMergeStrategy := {
      //case PathList("app.conf") => MergeStrategy.concat
      case PathList("logback.dev.xml") => MergeStrategy.discard
      case PathList("logback.test.xml") => MergeStrategy.discard
      case PathList("logback.prod.xml") => MergeStrategy.discard
      case PathList("logback.xml") => MergeStrategy.discard
      case PathList("application.conf") => MergeStrategy.discard
      case PathList("application.dev.conf") => MergeStrategy.discard
      case PathList("application.test.conf") => MergeStrategy.discard
      case PathList("application.prod.conf") => MergeStrategy.discard
      case PathList("reference.conf") => MergeStrategy.concat
      case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
      //https://stackoverflow.com/questions/999489/invalid-signature-file-when-attempting-to-run-a-jar
      //zip -d ob-streams-pipeline-app-certs-processor.jar 'META-INF/*.SF' 'META-INF/*.RSA' 'META-INF/*SF'
      //Необходимо выполнить команду выше, чтобы удалить лишние файлы типа *.SF, *.RSA, *SF, иначе jar файл падает с шибкой
      //Почему-то не всегда срабатывает код исключения ниже
      case PathList("META-INF", "*.SF") => MergeStrategy.discard
      case PathList("META-INF", "*.RSA") => MergeStrategy.discard
      case PathList("META-INF", "*SF") => MergeStrategy.discard
      //deleting: META-INF/BC1024KE.SF
      //deleting: META-INF/BC2048KE.SF
      //case PathList("META-INF", "BC1024KE.SF") => MergeStrategy.discard
      //case PathList("META-INF", "BC2048KE.SF") => MergeStrategy.discard
      case _ => MergeStrategy.first
      //case x =>{
      //  val oldStrategy = (assemblyMergeStrategy in assembly).value
      // oldStrategy(x) }
    }
    //logLevel in assembly := Level.Debug
  )
  .dependsOn(common, userService, leadService, leadUiService, botService, entityRepository)
  .enablePlugins(AssemblyPlugin)

//Настройка webApi проекта
lazy val telegramBot = (project in file("apps/bot"))
  .settings(
    name := "otus.sales.leads.generator.app.bot",
    mainClass := Some("ru.otus.sales.leads.generator.apps.bot.BotApp"),
    version := _version,
    scalaVersion := _scalaVersion,
    idePackagePrefix := _idePackagePrefix.map(_ + ".apps.bot"),
    scalacOptions ++= _scalacOptions,
    libraryDependencies ++= pureconfig,
    libraryDependencies ++= logback,
    libraryDependencies ++= serverTapir,
    //libraryDependencies += "org.http4s" %% "http4s-blaze-client" % "0.22.0",
    //libraryDependencies ++= clientTapir,
    libraryDependencies ++= Seq(
      bot
    ),
    //assembly / test := {},
    assemblyJarName := s"otus-sales-leads-generator-app-bot.jar",
    assemblyMergeStrategy := {
      //case PathList("app.conf") => MergeStrategy.concat
      case PathList("logback.dev.xml") => MergeStrategy.discard
      case PathList("logback.test.xml") => MergeStrategy.discard
      case PathList("logback.prod.xml") => MergeStrategy.discard
      case PathList("logback.xml") => MergeStrategy.discard
      case PathList("application.conf") => MergeStrategy.discard
      case PathList("application.dev.conf") => MergeStrategy.discard
      case PathList("application.test.conf") => MergeStrategy.discard
      case PathList("application.prod.conf") => MergeStrategy.discard
      case PathList("reference.conf") => MergeStrategy.concat
      case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
      //https://stackoverflow.com/questions/999489/invalid-signature-file-when-attempting-to-run-a-jar
      //zip -d ob-streams-pipeline-app-certs-processor.jar 'META-INF/*.SF' 'META-INF/*.RSA' 'META-INF/*SF'
      //Необходимо выполнить команду выше, чтобы удалить лишние файлы типа *.SF, *.RSA, *SF, иначе jar файл падает с шибкой
      //Почему-то не всегда срабатывает код исключения ниже
      case PathList("META-INF", "*.SF") => MergeStrategy.discard
      case PathList("META-INF", "*.RSA") => MergeStrategy.discard
      case PathList("META-INF", "*SF") => MergeStrategy.discard
      //deleting: META-INF/BC1024KE.SF
      //deleting: META-INF/BC2048KE.SF
      //case PathList("META-INF", "BC1024KE.SF") => MergeStrategy.discard
      //case PathList("META-INF", "BC2048KE.SF") => MergeStrategy.discard
      case _ => MergeStrategy.first
      //case x =>{
      //  val oldStrategy = (assemblyMergeStrategy in assembly).value
      // oldStrategy(x) }
    }
    //logLevel in assembly := Level.Debug
  )
  .dependsOn(common, botService)
  .enablePlugins(AssemblyPlugin)

//Сервисы
lazy val userService = (project in file("services/cores/users"))
  .settings(
    name := "otus.sales.leads.generator.services.core.users",
    scalaVersion := _scalaVersion,
    version := _version,
    scalacOptions ++= _scalacOptions,
    idePackagePrefix := _idePackagePrefix.map(_ + ".services.cores.users"),
    libraryDependencies ++= doobie,
    libraryDependencies ++= zio,
    libraryDependencies ++= logback,
    libraryDependencies ++= commonTapir
  )
  .dependsOn(entityRepository, common, data)

lazy val leadService = (project in file("services/cores/leads"))
  .settings(
    name := "otus.sales.leads.generator.services.core.leads",
    scalaVersion := _scalaVersion,
    version := _version,
    scalacOptions ++= _scalacOptions,
    idePackagePrefix := _idePackagePrefix.map(_ + ".services.cores.leads"),
    libraryDependencies ++= doobie,
    libraryDependencies ++= zio,
    libraryDependencies ++= logback,
    libraryDependencies ++= commonTapir
  )
  .dependsOn(entityRepository, common, data)

lazy val leadUiService = (project in file("services/ui/leads"))
  .settings(
    name := "otus.sales.leads.generator.services.ui.leads",
    scalaVersion := _scalaVersion,
    version := _version,
    scalacOptions ++= _scalacOptions,
    idePackagePrefix := _idePackagePrefix.map(_ + ".services.ui.leads"),
    libraryDependencies ++= doobie,
    libraryDependencies ++= zio,
    libraryDependencies ++= logback,
    libraryDependencies ++= commonTapir
  )
  .dependsOn(entityRepository, common, data)

lazy val botService = (project in file("services/cores/bot"))
  .settings(
    name := "otus.sales.leads.generator.services.core.bot",
    scalaVersion := _scalaVersion,
    version := _version,
    scalacOptions ++= _scalacOptions,
    idePackagePrefix := _idePackagePrefix.map(_ + ".services.cores.bot"),
    addCompilerPlugin(kindProjector),
    libraryDependencies ++= logback,
    libraryDependencies ++= zio,
    libraryDependencies ++= clientTapir,
    libraryDependencies ++= Seq(
      bot
    )
  )
  .dependsOn(common, data, userService, leadService, leadUiService)

//Данные
lazy val data = (project in file("data/domain"))
  .settings(
    name := "otus.sales.leads.generator.data.domain",
    scalaVersion := _scalaVersion,
    version := _version,
    scalacOptions ++= _scalacOptions,
    idePackagePrefix := _idePackagePrefix.map(_ + ".data.domain"),
    liquibaseUsername := "otus",
    liquibasePassword := "otus",
    liquibaseDriver := "org.postgresql.Driver",
    liquibaseUrl := "jdbc:postgresql://127.0.0.1/otus?createDatabaseIfNotExist=true",
    liquibaseChangelog := new File("data/domain/src/main/resources/liquibase/main.xml"),
    libraryDependencies ++= Seq(
      //logback,
      postgres,
      liquibase
      //logging,
      //spray,
      //scalaCompiler,
      //guiceDI,
      //guiceExDI,
      //scalaGuice
    )
  )
  .enablePlugins(SbtLiquibase)

//Инфраструктура
lazy val common = (project in file("inf/common")).settings(
  name := "otus.sales.leads.generator.inf.common",
  scalaVersion := _scalaVersion,
  version := _version,
  scalacOptions ++= _scalacOptions,
  idePackagePrefix := _idePackagePrefix.map(_ + ".inf.common"),
  libraryDependencies ++= commonTapir
)

lazy val entityRepository = (project in file("inf/repository")).settings(
  name := "otus.sales.leads.generator.inf.repository",
  scalaVersion := _scalaVersion,
  version := _version,
  scalacOptions ++= _scalacOptions,
  idePackagePrefix := _idePackagePrefix.map(_ + ".inf.repository"),
  libraryDependencies ++= doobie,
  libraryDependencies ++= zio
)
