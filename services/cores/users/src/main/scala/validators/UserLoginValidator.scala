package ru.otus.sales.leads.generator.services.cores.users
package validators

import models.{UserLogin, UserLoginError}

import zio.{Has, IO, Ref, ULayer, ZIO, ZLayer}

object UserLoginValidator {
  type UserLoginValidator = Has[Service]

  trait Service {
    def validate(info: UserLogin): IO[::[UserLoginError], Unit]
  }

  class ServiceImpl extends Service {
    override def validate(info: UserLogin): IO[::[UserLoginError], Unit] = for {
      ref <- Ref.make(List.empty[UserLoginError])
      _ <- (if (info.bot <= 0) ref.update(UserLoginError.InvalidBot :: _) else ZIO.none)
      errors <- ref.get
      _ <- if (errors.isEmpty) IO.none else IO.fail(::(errors.head, errors.tail))
    } yield ()
  }

  val live: ULayer[UserLoginValidator] = ZLayer.succeed(new ServiceImpl)

}
