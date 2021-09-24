package ru.otus.sales.leads.generator.services.cores.users
package services

import ru.otus.sales.leads.generator.data.domain.entities.{BotId, User}
import ru.otus.sales.leads.generator.inf.repository.transactors.{DBTransactor, makeTransactor}
import ru.otus.sales.leads.generator.services.cores.users.models.{UserLogin, UserLoginError}
import ru.otus.sales.leads.generator.services.cores.users.repositories.UserRepository
import ru.otus.sales.leads.generator.services.cores.users.validators.UserLoginValidator
import ru.otus.sales.leads.generator.inf.common.extensions.ListOpts
import ru.otus.sales.leads.generator.services.cores.users.repositories.UserRepository.UserRepository
import ru.otus.sales.leads.generator.services.cores.users.validators.UserLoginValidator.UserLoginValidator
import zio.{Has, URLayer, ZIO, ZLayer}
import zio.logging.Logging
import zio.interop.catz._

object UserLoginService {
  type UserLoginService = Has[Service]

  trait Service {
    def login(info: UserLogin): ZIO[DBTransactor with Logging, ::[UserLoginError], User]
  }

  class ServiceImpl(repo: UserRepository.Service, validator: UserLoginValidator.Service)
      extends Service {
    import doobie.implicits._

    override def login(info: UserLogin): ZIO[DBTransactor with Logging, ::[UserLoginError], User] =
      (for {
        _ <- Logging.info(s"Начало авторизации пользователя $info")
        _ <- validator.validate(info)
        trans <- makeTransactor
        botId = BotId(info.bot)
        user <- repo
          .getByBot(botId)
          .transact(trans)
          .tapCause(Logging.error("Получение пользователя", _))
          .some
          .mapError(
            {
              case None => ~UserLoginError.NotExists
              case Some(_) => ~UserLoginError.LostConnection
            }
          )
        _ <- Logging.info(s"Завершение авторизации пользователя $user")
      } yield user).tapCause(Logging.error("Авторизация пользователя", _))
  }

  val live: URLayer[UserRepository with UserLoginValidator, UserLoginService] =
    ZLayer.fromServices[UserRepository.Service, UserLoginValidator.Service, Service](
      (repo, validator) => new ServiceImpl(repo, validator))

  def login(info: UserLogin)
      : ZIO[UserLoginService with DBTransactor with Logging, ::[UserLoginError], User] =
    ZIO.accessM(_.get.login(info))
}
