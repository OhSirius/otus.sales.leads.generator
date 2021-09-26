package ru.otus.sales.leads.generator.services.cores.bot
package services

import canoe.api._
import canoe.syntax._
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
import ru.otus.sales.leads.generator.inf.common.models.ErrorInfo
import ru.otus.sales.leads.generator.services.cores.users.endpoints.UserEndpoint
import ru.otus.sales.leads.generator.services.cores.users.models.{UserReg, UserRegError}
import zio.{Has, IO, RIO, Task, ULayer, ZIO, ZLayer}
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
    ](implicit TС: TelegramClient[RIO[R, *]]): Scenario[RIO[R, *], Unit] = //
      for {
        _ <- Scenario.eval[RIO[R, *], Unit](Logging.info(s"Начало регистрации пользователя"))
        chat <- Scenario.expect(command("register").chat)
        _ <- Scenario.eval(chat.send("Подтвердите регистрацию (введите любой текст)"))
        mess <- Scenario.expect(textMessage)
        res <- Scenario
          .eval(
            registerUser[R](
              UserReg(
                mess.from.map(_.firstName).getOrElse(""),
                mess.from.flatMap(_.lastName).getOrElse(""),
                mess.from.map(_.id).getOrElse(-1))))
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
}
