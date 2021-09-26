package ru.otus.sales.leads.generator.services.cores.bot
package services

import canoe.api._
import canoe.syntax._
import config.BotApiConfiguration.{BotApiConfig, BotApiConfiguration}

import ru.otus.sales.leads.generator.inf.common.extensions.StringOpts
import canoe.api.{Scenario, TelegramClient}
import canoe.models.Chat
import canoe.models.messages.TextMessage
import canoe.syntax.{command, textMessage}
import cats.implicits.catsSyntaxApply
import ru.otus.sales.leads.generator.inf.common.models.{AuthId, ErrorInfo}
import ru.otus.sales.leads.generator.services.cores.bot.services.BotRegService.Service
import ru.otus.sales.leads.generator.services.cores.leads.endpoints.LeadEndpoint
import ru.otus.sales.leads.generator.services.cores.leads.models.LeadError.{
  BadPrice,
  EmptyFullname,
  EmptyPhone,
  LostConnection
}
import ru.otus.sales.leads.generator.services.cores.leads.models.{LeadError, LeadInfo}
import ru.otus.sales.leads.generator.services.cores.users.endpoints.UserEndpoint
import ru.otus.sales.leads.generator.services.cores.users.models.{UserReg, UserRegError}
import sttp.model.StatusCode.Unauthorized
import sttp.model.Uri
import sttp.tapir.client.sttp.SttpClientInterpreter
import zio.{Has, RIO, ULayer, ZIO, ZLayer}
import zio.logging.Logging

object BotLeadService {
  type BotLeadService = Has[Service]

  trait Service {
    def create[R <: BotApiConfiguration with Logging](implicit
        T: TelegramClient[RIO[R, *]]): Scenario[RIO[R, *], Unit]
  }

  class ServiceImpl extends Service {

    override def create[
        R <: BotApiConfiguration with Logging
    ](implicit TС: TelegramClient[RIO[R, *]]): Scenario[RIO[R, *], Unit] = //
      for {
        _ <- Scenario.eval[RIO[R, *], Unit](Logging.info(s"Начало создания лида"))
        chat <- Scenario.expect(command("createLead").chat)
        fullName <- provideField[R](chat, "ФИО")
        phone <- provideField[R](chat, "Телефон")
        price <- provideField[R](chat, "Сумма сделки")
        res <- Scenario
          .eval(
            createLead[R](
              LeadInfo(
                fullName.text,
                phone.text,
                BigDecimal.decimal(price.text.toDoubleOption.getOrElse(-1d))),
              price.from.map(_.id).getOrElse(-1)))
          .attempt
        _ <- res.fold(
          {
            case er: ErrorInfo[LeadError] if er.statusCode == Unauthorized =>
              Scenario.eval[RIO[R, *], Unit](Logging.warn(s"Пользователь не загеристрирован")) *>
                Scenario.eval(chat.send("Пройдите регистрацию через команду /register"))
            case er: ErrorInfo[LeadError] =>
              Scenario.eval[RIO[R, *], Unit](
                Logging.warn(s"Не удалось создать пользователя, т.к. $er")) *>
                Scenario.eval(chat.send(er.details.headOption match {
                  case Some(LostConnection) => "Проблемы на сервере CRM. Попробуйте позже"
                  case Some(BadPrice(_)) => s"Некорректная цена"
                  case Some(EmptyFullname) => "Некорректное ФИО"
                  case Some(EmptyPhone) => "Некорректный телефон"
                  case None => "Ошибка. Попробуйте позже"
                }))
            case e =>
              Scenario.eval[RIO[R, *], Unit](
                Logging.error(s"$e ${e.getStackTrace.mkString("Array(", ", ", ")")}")) *>
                Scenario.eval(chat.send("Нет соединения с сервером CRM. Попробуйте позже"))
          },
          _ => Scenario.eval(chat.send(s"Лид успешно создан в CRM"))
        )
        _ <- Scenario.eval[RIO[R, *], Unit](Logging.info(s"Завершение создания лида"))
      } yield ()

    def createLead[R <: BotApiConfiguration](info: LeadInfo, authId: AuthId): RIO[R, Boolean] =
      for {
        config <- ZIO.service[BotApiConfig]
        client = SttpClientInterpreter.toQuickClient(
          LeadEndpoint.create,
          Some(Uri.unsafeApply(config.host, config.port))
        ) // uri""/
        res <- RIO
          .effect(client((authId, info)))
          .foldM(
            e => ZIO.fail(e),
            {
              case Left(e) => ZIO.fail(e)
              case Right(v) => ZIO.succeed(v)
            })
      } yield res

    def provideField[R](chat: Chat, caption: String)(implicit
        TС: TelegramClient[RIO[R, *]]): Scenario[RIO[R, *], TextMessage] =
      for {
        _ <- Scenario.eval(chat.send(s"Введите $caption"))
        value <- Scenario.expect(textMessage)
        res <-
          if (value.text.isNullOrEmpty)
            Scenario.eval(chat.send(s"$caption не может быть пустым. Попробуйте снова")) >>
              provideField(chat, caption)
          else Scenario.pure[RIO[R, *]](value)
      } yield res

  }

  val live: ULayer[BotLeadService] =
    ZLayer.succeed[BotLeadService.Service](new ServiceImpl)

}
