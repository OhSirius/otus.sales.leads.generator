package ru.otus.sales.leads.generator.apps.api
package api

import io.circe.{Decoder, Encoder}
import ru.otus.sales.leads.generator.data.domain.entities.{BotId, User}
import sttp.model.StatusCode.Unauthorized
import sttp.tapir.{Endpoint, Schema, endpoint}
import sttp.tapir.ztapir.{ZEndpoint, auth, header, statusCode, stringBody}
import ru.otus.sales.leads.generator.apps.api.codecs._
import ru.otus.sales.leads.generator.inf.common.models.{AuthId, ErrorInfo}
import ru.otus.sales.leads.generator.inf.repository.transactors.DBTransactor
import ru.otus.sales.leads.generator.services.cores.users.models.UserLogin
import ru.otus.sales.leads.generator.services.cores.users.services.UserLoginService
import ru.otus.sales.leads.generator.services.cores.users.services.UserLoginService.UserLoginService
import sttp.tapir.json.circe.jsonBody
import zio.ZIO
import zio.logging.Logging

class AuthApi {
//  def authEndpoint[T](implicit
//      E: Encoder[ErrorInfo[T]],
//      D: Decoder[ErrorInfo[T]],
//      S: Schema[ErrorInfo[T]]): Endpoint[BotId, ErrorInfo[T], Unit, Any] =
//    endpoint
//      //.in(auth.apiKey(header[BotId]("X-Auth-Token")))
//      .in(header[BotId]("X-Auth-Token"))
//      .errorOut(jsonBody[ErrorInfo[T]].description("Модель ошибки"))

  def authorize[R <: UserLoginService with DBTransactor with Logging, E](
      authId: AuthId): ZIO[R, ErrorInfo[E], User] = UserLoginService
    .login(UserLogin(authId))
    .orElseFail(ErrorInfo[E]("Не авторизован", Unauthorized))
}

//      .errorOut(
//        stringBody
//          .description("An error message when authentication failed")
//          .and(statusCode) //(Unauthorized)
//          .mapTo[ErrorInfo[T]])
//      .in(auth.apiKey(cookie[String]("Token")).mapTo(AuthToken))
//      .in("api" / "1.0")
//      .errorOut(error)
