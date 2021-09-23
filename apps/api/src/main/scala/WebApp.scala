package ru.otus.sales.leads.generator.apps.api

import cats.syntax.all._
import io.circe.generic.auto._
import org.http4s._
import org.http4s.server.Router
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.syntax.kleisli._
import ru.otus.sales.leads.generator.apps.api.api.UserApi
import ru.otus.sales.leads.generator.apps.api.config.{ApiConfig, Configuration}
import ru.otus.sales.leads.generator.inf.repository.config.DbConfiguration.DbConfiguration
import zio.console.Console
import ru.otus.sales.leads.generator.inf.repository.config.DbConfiguration
import ru.otus.sales.leads.generator.inf.repository.transactors.{DBTransactor, DbHikariTransactor}
import ru.otus.sales.leads.generator.services.cores.users.bootstrap.UserRegConfig
import ru.otus.sales.leads.generator.services.cores.users.services.UserRegService.UserRegService
import sttp.tapir.json.circe._
import sttp.tapir.generic.auto._
import sttp.tapir.server.http4s.ztapir.ZHttp4sServerInterpreter
import sttp.tapir.swagger.http4s.SwaggerHttp4s
import zio.blocking.Blocking
//import sttp.tapir.swagger.SwaggerUI
import sttp.tapir.ztapir._
import zio.clock.Clock
import zio.interop.catz._
import zio.{App, ExitCode, IO, RIO, UIO, URIO, ZEnv, ZIO}
import cats.effect.{ExitCode => CatsExitCode}

//https://github.com/softwaremill/tapir/tree/master/examples/src/main/scala/sttp/tapir/examples
object WebApp extends App {

  type AppEnvironment = UserRegService
    with DBTransactor
    with Clock
    with Blocking
    with Configuration
    with Console

  type AppTask[A] = RIO[AppEnvironment, A]

  val yaml: String = {
    import sttp.tapir.docs.openapi.OpenAPIDocsInterpreter
    import sttp.tapir.openapi.circe.yaml._
    OpenAPIDocsInterpreter()
      .toOpenAPI(new UserApi[AppEnvironment].registerEndpoint, "Пользователи", "1.0")
      .toYaml
  }

  val swaggerRoutes: HttpRoutes[AppTask] = new SwaggerHttp4s(yaml).routes[AppTask]

  val appEnvironment =
    Configuration.live >+> Blocking.live >+> DbHikariTransactor.live >+> UserRegConfig.live

  val httApp = Router[AppTask](
    "/" -> new UserApi[AppEnvironment]().registerRoutes,
    "/" -> swaggerRoutes
  ).orNotFound

  val program = for {
    config <- ZIO.service[ApiConfig]
    server <- ZIO.runtime[AppEnvironment].flatMap {
      implicit runtime => // This is needed to derive cats-effect instances for that are needed by http4s
        BlazeServerBuilder[AppTask](
          runtime.platform.executor.asEC
        )
          .bindHttp(config.port, config.host)
          .withHttpApp(httApp) //<+>
          .serve
          .compile[AppTask, AppTask, CatsExitCode]
          .drain
    }
  } yield server

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    program.provideSomeLayer[zio.ZEnv](appEnvironment).exitCode
}
