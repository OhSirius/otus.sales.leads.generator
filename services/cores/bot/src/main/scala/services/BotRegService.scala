package ru.otus.sales.leads.generator.services.cores.bot
package services

import canoe.api._
import canoe.models.ParseMode
import canoe.models.outgoing.{ContactContent, TextContent}
import canoe.syntax._
import cats.Monad
import cats.effect.{ExitCode, IOApp, Sync}
import ru.otus.sales.leads.generator.services.cores.bot.config.BotApiConfiguration.{
  BotApiConfig,
  BotApiConfiguration
}
import ru.otus.sales.leads.generator.services.cores.users.models.UserRegError.{
  AlreadyRegistered,
  EmptyName,
  EmptySurname,
  InvalidBot,
  LostConnection
}
import sttp.model.Uri
import zio.RIO
import zio.logging.Logging
//import cats.implicits.{catsSyntaxApplicativeId, toFlatMapOps}
import cats.syntax.functor._
import fs2.Stream
import ru.otus.sales.leads.generator.inf.common.models
import ru.otus.sales.leads.generator.inf.common.models.ErrorInfo
import ru.otus.sales.leads.generator.services.cores.users.endpoints.UserEndpoint
import ru.otus.sales.leads.generator.services.cores.users.models.{UserReg, UserRegError}
import zio.{Has, IO, RIO, Task, ULayer, ZIO, ZLayer}
import zio.interop.catz._
import sttp.client3._
import sttp.tapir.client.sttp.SttpClientInterpreter
import cats.implicits.catsSyntaxApply

object BotRegService {

  type BotRegService = Has[Service]

  trait Service {
    def register[R <: BotApiConfiguration with Logging](implicit
        T: TelegramClient[RIO[R, *]]): Scenario[RIO[R, *], Unit]
  }

  class ServiceImpl extends Service {

    override def register[
        R <: BotApiConfiguration with Logging
    ](implicit T: TelegramClient[RIO[R, *]]): Scenario[RIO[R, *], Unit] = //
      for {
        _ <- Scenario.eval[RIO[R, *], Unit](Logging.info(s"Начало регистрации пользователя"))
        chat <- Scenario.expect(command("register").chat)
        _ <- Scenario.eval(chat.send("Подтвердите регистрацию (введите любой текст)"))
        mess <- Scenario.expect(textMessage)
        res <- Scenario
          .eval(
            registerUser[R](
              UserReg(
                mess.from.get.firstName,
                mess.from.get.lastName.getOrElse(" "),
                mess.from.get.id)))
          .attempt
        _ <- res.fold(
          {
            case er: ErrorInfo[UserRegError] =>
              Scenario.eval[RIO[R, *], Unit](
                Logging.warn(s"Не удалось создать пользователя, т.к. $er")) *>
                Scenario.eval(chat.send(er.details.headOption match {
                  case Some(LostConnection) => "Проблемы на сервере CRM. Попробуйте позже"
                  case Some(AlreadyRegistered(name)) => s"$name уже зарегистрирован(а)"
                  case Some(EmptyName) => "Не задано имя"
                  case Some(EmptySurname) => "Не задана фамилия"
                  case Some(InvalidBot) => "Не корректный id"
                  case None => "Ошибка. Попробуйте позже"
                }))
            case e =>
              Scenario.eval[RIO[R, *], Unit](
                Logging.error(s"$e ${e.getStackTrace.mkString("Array(", ", ", ")")}")) *>
                Scenario.eval(chat.send("Нет соединения с сервером CRM. Попробуйте позже"))
          },
          _ => Scenario.eval(chat.send(s"Вы успешно зарегистрированы в CRM"))
        )
        _ <- Scenario.eval[RIO[R, *], Unit](Logging.info(s"Завершение регистрации пользователя"))
      } yield ()

    def registerUser[R <: BotApiConfiguration](reg: UserReg): RIO[R, Boolean] = for {
      config <- ZIO.service[BotApiConfig]
      client = SttpClientInterpreter.toQuickClient(
        UserEndpoint.register,
        //Some(uri"http://localhost:4080")
        //Some(Uri.unsafeApply(config.uri))
        //Some(Uri.parse(config.uri).fold(Uri.))
        Some(Uri.unsafeApply(config.host, config.port))
      ) // uri""/
      res <- RIO
        .effect(client(reg))
        .foldM(
          e => ZIO.fail(e),
          {
            case Left(e) => ZIO.fail(e)
            case Right(v) => ZIO.succeed(v)
          })
    } yield res

    //def test[R](str: String): RIO[R, String] = ZIO.succeed(str)

  }

  val live: ULayer[BotRegService] =
    ZLayer.succeed[BotRegService.Service](new ServiceImpl)

//  def register(
//      info: LeadInfo,
//      user: User): ZIO[LeadService with DBTransactor with Logging, ::[LeadError], Unit] =
//    ZIO.accessM(_.get.create(info, user))

//  trait Service {
//    def register(implicit T: TelegramClient[Task]): Scenario[Task, Unit]
//  }
//
//  class ServiceImpl extends Service {
//
//    override def register(implicit T: TelegramClient[Task]): Scenario[Task, Unit] =
//      for {
//        chat <- Scenario.expect(command("register").chat)
//        mess <- Scenario.expect(textMessage)
//        //id <- Scenario.eval(chat.send(ContactContent("123123", "sdfsdf"), Option(1)))
//        //_    <- Scenario.eval(chat.send(TextContent("<html><h1>eeeee</h1></html>", Option(ParseMode.HTML))))
//        _ <- Scenario.eval(chat.send(s"Nice to meet you, ${mess.from}"))
//      } yield ()
//  }
//
//  val live: ULayer[BotRegService] =
//    ZLayer.succeed(new ServiceImpl)
}
