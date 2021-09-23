package ru.otus.sales.leads.generator.services.cores.users
package services

import ru.otus.sales.leads.generator.data.domain.entities.{BotId, Name, Surname, User}
import ru.otus.sales.leads.generator.inf.repository.transactors.{DBTransactor, makeTransactor}
import ru.otus.sales.leads.generator.services.cores.users.models.{UserReg, UserRegError}
import ru.otus.sales.leads.generator.services.cores.users.repositories.UserRepository
import zio.{Has, IO, Task, UIO, URLayer, ZIO, ZLayer, clock}
import zio.interop.catz._
import ru.otus.sales.leads.generator.inf.common.extensions.ListOpts
import ru.otus.sales.leads.generator.services.cores.users.repositories.UserRepository.UserRepository
import ru.otus.sales.leads.generator.services.cores.users.validators.UserRegValidator
import ru.otus.sales.leads.generator.services.cores.users.validators.UserRegValidator.UserRegValidator

object UserRegService {

  type UserRegService = Has[Service]

  trait Service {
    def register(info: UserReg): ZIO[DBTransactor, ::[UserRegError], Unit]
  }

  class ServiceImpl(repo: UserRepository.Service, validator: UserRegValidator.Service)
      extends Service {
    import doobie.implicits._

    override def register(info: UserReg): ZIO[DBTransactor, ::[UserRegError], Unit] = for {
      _ <- validator.validate(info)
      trans <- makeTransactor
      botId = BotId(info.bot)
      _ <- repo
        .getByBot(botId)
        .transact(trans)
        .some
        .foldM(
          {
            case None =>
              repo
                .create(User(None, info.name, info.surname, botId))
                .transact(trans)
                .orElseFail(~UserRegError.LostConnection)
            //case Some(_) => ZIO.fail(~UserRegError.LostConnection)
            case Some(ex) => UIO.succeed(println(ex)) *> ZIO.fail(~UserRegError.LostConnection)

          },
          user => ZIO.fail(~UserRegError.AlreadyRegistered(user.name))
        )
    } yield ()
  }

  val live: URLayer[UserRepository with UserRegValidator, UserRegService] =
    ZLayer.fromServices[UserRepository.Service, UserRegValidator.Service, Service](
      (repo, validator) => new ServiceImpl(repo, validator))

  def register(info: UserReg): ZIO[UserRegService with DBTransactor, ::[UserRegError], Unit] =
    ZIO.accessM(_.get.register(info))

}
