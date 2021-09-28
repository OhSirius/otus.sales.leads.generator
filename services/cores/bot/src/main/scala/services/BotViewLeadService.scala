package ru.otus.sales.leads.generator.services.cores.bot
package services

import config.BotApiConfiguration.{BotApiConfig, BotApiConfiguration}

import canoe.api.{Scenario, TelegramClient}
import canoe.syntax.{command, textMessage}
import ru.otus.sales.leads.generator.inf.common.models.{AuthId, ErrorInfo}
import ru.otus.sales.leads.generator.services.cores.bot.services.BotRegService.Service
import ru.otus.sales.leads.generator.services.cores.users.endpoints.UserEndpoint
import ru.otus.sales.leads.generator.services.cores.users.models.{UserReg, UserRegError}
import sttp.model.Uri
import sttp.tapir.client.sttp.SttpClientInterpreter
import zio.{Has, RIO, ULayer, ZIO, ZLayer}
import zio.logging.Logging
import canoe.api._
import canoe.models.outgoing.ContactContent
import canoe.syntax._
import cats.Applicative
import cats.effect.Sync
import cats.implicits.catsSyntaxApply
import ru.otus.sales.leads.generator.services.ui.leads.endpoints.LeadViewEndpoint
import ru.otus.sales.leads.generator.services.ui.leads.models.LeadViewError.LostConnection
import ru.otus.sales.leads.generator.services.ui.leads.models.{LeadView, LeadViewError, LeadViewTop}
import sttp.model.StatusCode.Unauthorized
import zio.interop.catz._

object BotViewLeadService {
  type BotViewLeadService = Has[Service]

  trait Service {
    def getActive[R <: BotApiConfiguration with Logging](implicit
        T: TelegramClient[RIO[R, *]]): Scenario[RIO[R, *], Unit]
  }

  class ServiceImpl extends Service {

    override def getActive[
        R <: BotApiConfiguration with Logging
    ](implicit TС: TelegramClient[RIO[R, *]]): Scenario[RIO[R, *], Unit] = //
      for {
        _ <- Scenario.eval[RIO[R, *], Unit](Logging.info(s"Начало получения лидов"))
        chat <- Scenario.expect(command("activeLead").chat)
        _ <- Scenario.eval(chat.send("Количество лидов"))
        mess <- Scenario.expect(textMessage)
        res <- Scenario
          .eval(
            getActiveLeads[R](
              mess.text.toIntOption.getOrElse(-1),
              mess.from.map(_.id).getOrElse(-1)))
          .attempt
        _ <- res.fold(
          {
            case er: ErrorInfo[LeadViewError] if er.statusCode == Unauthorized =>
              Scenario.eval[RIO[R, *], Unit](Logging.warn(s"Пользователь не загеристрирован")) *>
                Scenario.eval(chat.send("Пройдите регистрацию через команду /register"))
            case er: ErrorInfo[LeadViewError] =>
              Scenario.eval[RIO[R, *], Unit](
                Logging.warn(s"Не удалось создать пользователя, т.к. $er")) *>
                Scenario.eval(chat.send(er.details.headOption match {
                  case Some(LostConnection) => "Проблемы на сервере CRM. Попробуйте позже"
                  case None => "Ошибка. Попробуйте позже"
                }))
            case e =>
              Scenario.eval[RIO[R, *], Unit](
                Logging.error(s"$e ${e.getStackTrace.mkString("Array(", ", ", ")")}")) *>
                Scenario.eval(chat.send("Нет соединения с сервером CRM. Попробуйте позже"))
          },
          leads =>
            //Sync[Scenario[RIO[R, *], Unit]].
            //Scenario.traverse

//            leads.map(lead =>
//              Scenario.eval(
//                chat.send(
//                  ContactContent(
//                    lead.phone,
//                    lead.fullName,
//                    Some(lead.id.toString),
//                    Some(lead.price.toString()))))) *>
//            leads.foldLeft(Scenario.pure[RIO[R, *]]())(
//              (sc: Scenario[RIO[R, *], Unit], lead: LeadView) =>
//                sc *> Scenario.eval(
//                  chat.send(
//                    ContactContent(
//                      lead.phone,
//                      lead.fullName,
//                      Some(lead.id.toString),
//                      Some(lead.price.toString())))))
//            Scenario.eval(
//              chat.send(
//                ContactContent(
//                  leads.head.phone,
//                  leads.head.fullName,
//                  Some(s"№${leads.head.id}, цена ${leads.head.price}"),
//                  Some(leads.head.price.toString()))))
//            leads
//              .map(lead =>
//                Scenario.eval(
//                  chat.send(
//                    ContactContent(
//                      lead.phone,
//                      lead.fullName,
//                      Some(s"№${leads.head.id}, цена ${leads.head.price}"),
//                      Some(lead.price.toString())))))
//              .fold(Scenario.eval[RIO[R, *], Unit](Logging.info(s"Список лидов:")))((a, b) =>
//                a *> b)
            //Scenario.eval[RIO[R, *], Unit](Logging.info(s"WTF $leads")) *>

//            leads
//              .map(lead =>
//                Scenario.eval(
//                  chat.send(
//                    ContactContent(
//                      lead.phone,
//                      lead.fullName,
//                      Some(s"№${lead.id}, цена ${lead.price}"),
//                      Some(lead.price.toString())))))
//              .fold(Scenario.eval[RIO[R, *], Unit](Logging.info(s"Список лидов:")))(_ *> _)
            leads
              .map(lead =>
                Scenario.eval(chat.send(ContactContent(
                  lead.phone,
                  lead.fullName,
                  Some(s" лид №${lead.id}, цена ${lead.price}"),
                  //s"№${lead.id}, цена ${lead.price},",
                  //Some(lead.fullName),
                  //Some(lead.price.toString())
                  None
                ))))
              .fold(Scenario.pure[RIO[R, *]](ZIO.none))(_ *> _)
        )
        _ <- Scenario.eval[RIO[R, *], Unit](Logging.info(s"Завершение получения лидов"))
      } yield ()

    def getActiveLeads[R <: BotApiConfiguration](
        top: LeadViewTop,
        authId: AuthId): RIO[R, List[LeadView]] = for {
      config <- ZIO.service[BotApiConfig]
      client = SttpClientInterpreter.toQuickClient(
        LeadViewEndpoint.getActive,
        Some(Uri.unsafeApply(config.host, config.port))
      ) // uri""/
      res <- RIO
        .effect(client((authId, top)))
        .foldM(
          e => ZIO.fail(e),
          {
            case Left(e) => ZIO.fail(e)
            case Right(v) => ZIO.succeed(v)
          })
    } yield res
  }

  val live: ULayer[BotViewLeadService] =
    ZLayer.succeed[BotViewLeadService.Service](new ServiceImpl)
}
