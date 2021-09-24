package ru.otus.sales.leads.generator.services.cores.users
package services

import ru.otus.sales.leads.generator.data.domain.entities.{BotId, User}
import ru.otus.sales.leads.generator.inf.repository.transactors.{DBTransactor, makeTransactor}
import ru.otus.sales.leads.generator.services.cores.users.models.{UserReg, UserRegError}
import ru.otus.sales.leads.generator.services.cores.users.repositories.UserRepository
import zio.{Has, URLayer, ZIO, ZLayer}
import zio.interop.catz._
import ru.otus.sales.leads.generator.inf.common.extensions.ListOpts
import ru.otus.sales.leads.generator.services.cores.users.repositories.UserRepository.UserRepository
import ru.otus.sales.leads.generator.services.cores.users.validators.UserRegValidator
import ru.otus.sales.leads.generator.services.cores.users.validators.UserRegValidator.UserRegValidator
import zio.logging.Logging

object UserRegService {

  type UserRegService = Has[Service]

  trait Service {
    def register(info: UserReg): ZIO[DBTransactor with Logging, ::[UserRegError], Unit]
  }

  class ServiceImpl(repo: UserRepository.Service, validator: UserRegValidator.Service)
      extends Service {
    import doobie.implicits._

    override def register(info: UserReg): ZIO[DBTransactor with Logging, ::[UserRegError], Unit] =
      (for {
        _ <- Logging.info(s"Начало регистрации пользователя $info")
        _ <- validator.validate(info)
        trans <- makeTransactor
        botId = BotId(info.bot)
        user <- repo
          .getByBot(botId)
          .transact(trans)
          .tapCause(Logging.error("Получение пользователя", _))
          .some
          .foldM(
            {
              case None =>
                repo
                  .create(User(None, info.name, info.surname, botId))
                  .transact(trans)
                  .tapCause(Logging.error("Создание пользователя", _))
                  .orElseFail(~UserRegError.LostConnection)
              case Some(_) => ZIO.fail(~UserRegError.LostConnection)
            },
            user => ZIO.fail(~UserRegError.AlreadyRegistered(user.name))
          )
        _ <- Logging.info(s"Завершение регистрации пользователя $user")
      } yield ()).tapCause(Logging.error("Регистрация пользователя", _))
  }

  val live: URLayer[UserRepository with UserRegValidator, UserRegService] =
    ZLayer.fromServices[UserRepository.Service, UserRegValidator.Service, Service](
      (repo, validator) => new ServiceImpl(repo, validator))

  def register(
      info: UserReg): ZIO[UserRegService with DBTransactor with Logging, ::[UserRegError], Unit] =
    ZIO.accessM(_.get.register(info))

}
