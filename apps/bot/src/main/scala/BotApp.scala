package ru.otus.sales.leads.generator.apps.bot

import canoe.api.{Bot, Scenario, TelegramClient}
import fs2.Stream
import org.http4s.client.blaze.BlazeClientBuilder
import ru.otus.sales.leads.generator.apps.bot.config.{AppConfig, BotConfig, Configuration}
import ru.otus.sales.leads.generator.apps.bot.logging.Logger
import ru.otus.sales.leads.generator.services.cores.bot.config.BotApiConfiguration.BotApiConfig
import ru.otus.sales.leads.generator.services.cores.bot.services.{BotLeadService, BotRegService}
import ru.otus.sales.leads.generator.services.cores.bot.services.BotRegService.BotRegService
import zio.blocking.Blocking
import zio.logging.Logging
import zio.clock.Clock
import zio.interop.catz._
import zio.{App, ExitCode, IO, RIO, UIO, URIO, ZEnv, ZIO}
import cats.effect.{ExitCode => CatsExitCode}
import ru.otus.sales.leads.generator.services.cores.bot.services.BotLeadService.BotLeadService

object BotApp extends App {
  type AppEnvironment = BotLeadService
    with BotRegService
    with Configuration
    with Clock
    with Blocking
    with Logging
  type AppTask[A] = RIO[AppEnvironment, A]

  val appEnvironment =
    Logger.liveWithMdc >+> Clock.live >+> Configuration.live >+> BotRegService.live >+> BotLeadService.live

  val program = for {
    botConfig <- ZIO.service[BotConfig]
    apiConfig <- ZIO.service[BotApiConfig]
    _ <- Logging.info(s"Запускаем телеграм бота с настройками: $botConfig и $apiConfig")
    regService <- ZIO.service[BotRegService.Service]
    leadService <- ZIO.service[BotLeadService.Service]
    server <- ZIO.runtime[AppEnvironment].flatMap { implicit runtime =>
      Stream
        .resource(BlazeClientBuilder[AppTask](runtime.platform.executor.asEC).resource)
        .map(TelegramClient.fromHttp4sClient[AppTask](botConfig.token)(_))
        .flatMap { implicit client =>
          Bot
            .polling[AppTask]
            .follow(regService.register[AppEnvironment], leadService.create[AppEnvironment])
        }
        .compile
        .drain
    }
  } yield server

  override def run(args: List[String]): URIO[ZEnv, ExitCode] =
    program.provideSomeLayer[ZEnv](appEnvironment).exitCode

}
