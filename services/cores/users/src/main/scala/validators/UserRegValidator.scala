package ru.otus.sales.leads.generator.services.cores.users
package validators

import ru.otus.sales.leads.generator.inf.common.extensions.StringOpts
import ru.otus.sales.leads.generator.services.cores.users.models.{UserReg, UserRegError}
import zio.{Has, IO, Ref, ULayer, ZIO, ZLayer}

object UserRegValidator {

  type UserRegValidator = Has[Service]

  trait Service {
    def validate(info: UserReg): IO[::[UserRegError], Unit]
  }

  class ServiceImpl extends Service {
    override def validate(info: UserReg): IO[::[UserRegError], Unit] = for {
      ref <- Ref.make(List.empty[UserRegError])
      _ <- (if (info.name.isNullOrEmpty) ref.update(UserRegError.EmptyName :: _) else ZIO.none) *>
        (if (info.surname.isNullOrEmpty) ref.update(UserRegError.EmptySurname :: _)
         else ZIO.none) *>
        (if (info.bot <= 0) ref.update(UserRegError.InvalidBot :: _) else ZIO.none)
      errors <- ref.get
      _ <- if (errors.isEmpty) IO.none else IO.fail(::(errors.head, errors.tail)) //.succeed(true)
    } yield ()
  }

  val live: ULayer[UserRegValidator] = ZLayer.succeed(new ServiceImpl)
}
