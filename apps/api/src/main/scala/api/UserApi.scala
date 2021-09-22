package ru.otus.sales.leads.generator.apps.api
package api

import WebApp.{Pet, petEndpoint}

import zio.interop.catz._
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.ztapir.{ZEndpoint, ZServerEndpoint, endpoint, path, stringBody}
import zio.{IO, RIO, UIO, ZIO}
import sttp.tapir.ztapir._
import sttp.tapir.json.circe._
import io.circe.generic.auto._
import org.http4s.HttpRoutes
import org.http4s.Uri.RegName
import ru.otus.sales.leads.generator.inf.repository.transactor.DBTransactor
import ru.otus.sales.leads.generator.services.cores.users.models.{
  UserReg,
  UserRegBot,
  UserRegError,
  UserRegName,
  UserRegSurname
}
import ru.otus.sales.leads.generator.services.cores.users.services.UserRegService
import ru.otus.sales.leads.generator.services.cores.users.services.UserRegService.{
  UserRegService,
  register
}
import sttp.tapir.json.circe._
import sttp.tapir.generic.auto._
import sttp.tapir.server.http4s.ztapir.ZHttp4sServerInterpreter
import zio.clock.Clock

//https://blog.softwaremill.com/describe-then-interpret-http-endpoints-using-tapir-ac139ba565b0
class UserApi[R <: UserRegService with DBTransactor] {
  type UserTask[A] = RIO[R, A]

  val registerUserEndpoint: ZEndpoint[UserReg, UserRegError, Boolean] =
    endpoint
      .description("Регистрация нового пользователя")
      .post
      .in("users" / "register")
      .in(
        jsonBody[UserReg]
          .description("Модель регистрации")
          .example(UserReg(UserRegName("Александр"), UserRegSurname("Павлычев"), UserRegBot(156))))
      .errorOut(
        jsonBody[UserRegError]
          .description("Ошибки регистрации")
          .example(UserRegError.AlreadyRegistered("Александр")))
      .out(jsonBody[Boolean])

  val registerUserServerEndpoint
      : ZServerEndpoint[R, UserReg, UserRegError, Boolean] = //UserRegService with DBTransactor
    registerUserEndpoint.zServerLogic { reg =>
      for {
        //service <- ZIO.service[UserRegService.Service]
        _ <- register(reg)
      } yield true
    }

  val registerUserRoutes: HttpRoutes[ZIO[R with Clock, Throwable, *]] =
    //: HttpRoutes[RIO[Clock with R, *]] = // with Blocking
    ZHttp4sServerInterpreter[R]()
      .from(registerUserServerEndpoint)
      .toRoutes
}
