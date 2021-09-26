package ru.otus.sales.leads.generator.apps.api

import cats.syntax.all._
import io.circe.generic.auto._
import org.http4s._
import org.http4s.server.Router
//import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.syntax.kleisli._
import ru.otus.sales.leads.generator.apps.api.api.{LeadApi, SwaggerApi, UserApi}
import ru.otus.sales.leads.generator.apps.api.config.{ApiConfig, Configuration}
import ru.otus.sales.leads.generator.apps.api.logging.Logger
import ru.otus.sales.leads.generator.inf.repository.transactors.{DBTransactor, DbHikariTransactor}
import ru.otus.sales.leads.generator.services.cores.users.bootstrap.{UserLoginConfig, UserRegConfig}
import ru.otus.sales.leads.generator.services.cores.users.services.UserRegService.UserRegService
import zio.blocking.Blocking
import zio.logging.Logging
import zio.clock.Clock
import zio.interop.catz._
import zio.{App, ExitCode, IO, RIO, UIO, URIO, ZEnv, ZIO}
import cats.effect.{ExitCode => CatsExitCode}
import ru.otus.sales.leads.generator.services.cores.leads.bootstrap.LeadConfig
import ru.otus.sales.leads.generator.services.cores.leads.services.LeadService.LeadService
import ru.otus.sales.leads.generator.services.cores.users.services.UserLoginService.UserLoginService
import ru.otus.sales.leads.generator.services.ui.leads.bootstrap.LeadViewConfig

object WebApp extends App {
  val appEnvironment =
    Logger.liveWithMdc >+> Configuration.live >+> Blocking.live >+> DbHikariTransactor.live >+>
      UserRegConfig.live >+> UserLoginConfig.live >+> LeadConfig.live >+> LeadViewConfig.live

  val httApp = Router[AppTask](
    "/" -> new UserApi[AppEnvironment]().registerRoutes,
    "/" -> new LeadApi[AppEnvironment]().createRoutes,
    "/" -> new LeadApi[AppEnvironment]().getActiveRoutes,
    "/" -> SwaggerApi.routes
  ).orNotFound

  val program = for {
    config <- ZIO.service[ApiConfig]
    server <- ZIO.runtime[AppEnvironment].flatMap { implicit runtime =>
      BlazeServerBuilder[AppTask](
        runtime.platform.executor.asEC
      )
        .bindHttp(config.port, config.host)
        .withHttpApp(httApp)
        .serve
        .compile[AppTask, AppTask, CatsExitCode]
        .drain
    }
  } yield server

  override def run(args: List[String]): URIO[ZEnv, ExitCode] =
    program.provideSomeLayer[ZEnv](appEnvironment).exitCode
}
