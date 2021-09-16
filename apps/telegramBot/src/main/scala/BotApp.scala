package ru.otus.sales.leads.generator.apps.telegramBot

import canoe.api._
import canoe.models.messages.{AnimationMessage, StickerMessage, TelegramMessage, TextMessage}
import canoe.syntax._
import cats.effect.{ExitCode, IO, IOApp}
import cats.syntax.functor._
import fs2.Stream

object BotApp extends IOApp {
  val token: String =
    "1967675782:AAHX6RgBeQktXWB3J9okfi2cBe28BhAwwPI" //https://web.telegram.org/z/#1967675782 //https://t.me/pav_test_bot

  def run(args: List[String]): IO[ExitCode] =
    Stream
      .resource(TelegramClient.global[IO](token))
      .flatMap { implicit client =>
        Bot.polling[IO].follow(echos)
      }
      .compile
      .drain
      .as(ExitCode.Success)

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
