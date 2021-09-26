package ru.otus.sales.leads.generator.services.cores.bot
package services

import canoe.api._
import canoe.models.ParseMode
import canoe.models.outgoing.{ContactContent, TextContent}
import canoe.syntax._
import cats.Monad
import cats.effect.{ExitCode, IO, IOApp, Sync}
//import cats.implicits.{catsSyntaxApplicativeId, toFlatMapOps}
import cats.syntax.functor._
import fs2.Stream
import ru.otus.sales.leads.generator.inf.common.models
import ru.otus.sales.leads.generator.inf.common.models.ErrorInfo
import ru.otus.sales.leads.generator.services.cores.users.endpoints.UserEndpoint
import ru.otus.sales.leads.generator.services.cores.users.models.{UserReg, UserRegError}
import zio.{Has, RIO, Task, ULayer, ZIO, ZLayer}
import zio.interop.catz._

object BotRegService {

  type BotRegService = Has[Service]

  trait Service {
    def register[R](implicit T: TelegramClient[RIO[R, *]]): Scenario[RIO[R, *], Unit]
  }

  class ServiceImpl extends Service {

    override def register[R](implicit T: TelegramClient[RIO[R, *]]): Scenario[RIO[R, *], Unit] =
      for {
        chat <- Scenario.expect(command("register").chat)
        mess <- Scenario.expect(textMessage)
        //id <- Scenario.eval(chat.send(ContactContent("123123", "sdfsdf"), Option(1)))
        //_    <- Scenario.eval(chat.send(TextContent("<html><h1>eeeee</h1></html>", Option(ParseMode.HTML))))
        res <- Scenario
          .eval(
            makeClientRequest[R](
              UserReg(
                mess.from.get.firstName,
                mess.from.get.lastName.getOrElse("1"),
                mess.from.get.id)))
          .attempt
        _ <- res.fold(
          e =>
            Scenario
              .eval(
                Sync[RIO[R, *]].pure(
                  "Something went wrong while making your order. Please try again."))
              .flatMap(m => Scenario.eval(chat.send(m))),
          orderId =>
            Scenario.eval(chat.send(s"Order successfully made. Here's your order id: $orderId"))
        )
        _ <- Scenario.eval(chat.send(s"Nice to meet you, ${mess.from}"))
      } yield ()
  }

  val live: ULayer[BotRegService] =
    ZLayer.succeed[BotRegService.Service](new ServiceImpl)

  def makeClientRequest[R](reg: UserReg): RIO[R, Boolean] = {
    import sttp.client3._
    import sttp.tapir.client.sttp.SttpClientInterpreter

    val client =
      SttpClientInterpreter.toQuickClient(UserEndpoint.register, Some(uri"http://localhost:4080"))

    //val res: Either[ErrorInfo[UserRegError], Boolean] = client(reg)
    //ZIO.fromEither(client(reg))
    RIO
      .effect(client(reg))
      .foldM(
        e => ZIO.fail(e),
        r =>
          r match {
            case Left(e) => ZIO.fail(e)
            case Right(v) => ZIO.succeed(v)
          })

    //val result: Either[String, Vector[Book]] = client(Some(3))

    //logger.info("Result of listing request with limit 3: " + result)
  }

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
