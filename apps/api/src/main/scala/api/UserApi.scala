package ru.otus.sales.leads.generator.apps.api
package api

import sttp.tapir.json.circe.jsonBody
import sttp.tapir.ztapir.{ZEndpoint, ZServerEndpoint, endpoint, path, stringBody}
import zio.{IO, RIO, UIO, ZIO}
import sttp.tapir.ztapir._
import io.circe.generic.auto._
import org.http4s.HttpRoutes
import ru.otus.sales.leads.generator.apps.api.logging.Logger.botId
import ru.otus.sales.leads.generator.inf.repository.transactors.DBTransactor
import ru.otus.sales.leads.generator.services.cores.users.models.{UserReg, UserRegError}
import ru.otus.sales.leads.generator.services.cores.users.services.UserRegService.{
  UserRegService,
  register
}
import sttp.tapir.generic.auto._
import sttp.tapir.server.http4s.ztapir.ZHttp4sServerInterpreter
import zio.clock.Clock
import ru.otus.sales.leads.generator.inf.common.extensions.ListOpts
import zio.logging.Logging
import zio.logging._

import java.util.UUID

class UserApi[R <: UserRegService with DBTransactor with Logging] {
  type UserTask[A] = RIO[R, A]

  val registerEndpoint: ZEndpoint[UserReg, ::[UserRegError], Boolean] =
    endpoint
      .description("Регистрация нового пользователя")
      .post
      .in("users" / "register")
      .in(
        jsonBody[UserReg]
          .description("Модель регистрации")
          .example(UserReg("Александр", "Павлычев", 156)))
      .errorOut(
        jsonBody[::[UserRegError]]
          .description("Ошибки регистрации")
          .example(~UserRegError.AlreadyRegistered("Александр")))
      .out(plainBody[Boolean])

  val registerServerEndpoint: ZServerEndpoint[R, UserReg, ::[UserRegError], Boolean] =
    registerEndpoint.zServerLogic { reg =>
      for {
        correlationId <- UIO(Some(UUID.randomUUID()))
        _ <- log.locally(
          _.annotate(botId, reg.bot).annotate(LogAnnotation.CorrelationId, correlationId)) {
          register(reg)
        }
      } yield true
    }

  val registerRoutes: HttpRoutes[ZIO[R with Clock, Throwable, *]] =
    ZHttp4sServerInterpreter[R]()
      .from(registerServerEndpoint)
      .toRoutes
}
