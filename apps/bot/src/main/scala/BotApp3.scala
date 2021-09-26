package ru.otus.sales.leads.generator.apps.bot

import canoe.api.{Bot, Scenario, TelegramClient}
import canoe.api.clients.Http4sTelegramClient
import canoe.models.messages.{AnimationMessage, StickerMessage, TelegramMessage, TextMessage}
import canoe.syntax.any
import cats.effect.IO
import cats.syntax.all._
import fs2.Stream
import io.circe.generic.auto._
import org.http4s._
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.server.Router
import ru.otus.sales.leads.generator.apps.bot.BotApp.echos
import ru.otus.sales.leads.generator.services.cores.bot.services.BotRegService
import ru.otus.sales.leads.generator.services.cores.bot.services.BotRegService.BotRegService
import zio.{Has, ULayer, ZLayer}
//import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.syntax.kleisli._
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
import canoe.syntax._
import canoe.api._

object WebApp3 extends App {
  val token: String =
    "1967675782:AAHX6RgBeQktXWB3J9okfi2cBe28BhAwwPI"
  type AppEnvironment = BotRegService with Clock with Blocking

  type AppTask[A] = RIO[AppEnvironment, A]

  val appEnvironment: ZLayer[Any, Nothing, Clock with BotRegService] =
    Clock.live >+> BotRegService.live
//    Logger.liveWithMdc >+> Configuration.live >+> Blocking.live >+> DbHikariTransactor.live >+>
//      UserRegConfig.live >+> UserLoginConfig.live >+> LeadConfig.live >+> LeadViewConfig.live

//  val httApp = Router[AppTask](
//    "/" -> new UserApi[AppEnvironment]().registerRoutes,
//    "/" -> new LeadApi[AppEnvironment]().createRoutes,
//    "/" -> new LeadApi[AppEnvironment]().getActiveRoutes,
//    "/" -> SwaggerApi.routes
//  ).orNotFound

  val program = for {
    test <- ZIO.service[BotRegService.Service]
    server <- ZIO.runtime[AppEnvironment].flatMap { implicit runtime =>
      Stream
        .resource(BlazeClientBuilder[AppTask](runtime.platform.executor.asEC).resource)
        .map(TelegramClient.fromHttp4sClient[AppTask](token)(_))
        .flatMap { implicit client =>
          Bot.polling[AppTask].follow(test.register) //[AppEnvironment]
        }
        //.serve
        .compile //[AppTask, AppTask, CatsExitCode]
        .drain
    }
  } yield server

  override def run(args: List[String]): URIO[ZEnv, ExitCode] =
    program.provideSomeLayer[ZEnv](appEnvironment).exitCode

  def hi[F[_]: TelegramClient]: Scenario[F, Unit] =
    for {
      chat <- Scenario.expect(command("hi").chat)
      mess <- Scenario.expect(textMessage)
      _ <- Scenario.eval(chat.send(s"Nice, ${mess.from}"))

      chat2 <- Scenario.expect(command("we").chat)
      mess2 <- Scenario.expect(textMessage)
      _ <- Scenario.eval(chat2.send(s"we, ${mess2.from}"))
    } yield ()

  def echos[F[_]: TelegramClient]: Scenario[F, Unit] =
    for {
      msg <- Scenario.expect(any)
      _ <- Scenario.eval(echoBack(msg))
    } yield ()

  def echoBack[F[_]: TelegramClient](msg: TelegramMessage): F[_] = msg match {
    case textMessage: TextMessage => msg.chat.send(textMessage.text)
    case animationMessage: AnimationMessage => msg.chat.send(animationMessage.animation)
    case stickerMessage: StickerMessage => msg.chat.send(stickerMessage.sticker)
    case _ => msg.chat.send("Sorry! I can't echos that back.")
  }

}
