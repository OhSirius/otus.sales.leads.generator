package ru.otus.sales.leads.generator.services.cores.users
package endpoints

import ru.otus.sales.leads.generator.inf.common.models.ErrorInfo
import ru.otus.sales.leads.generator.inf.common.endpoints.AuthEndpoint.authEndpoint
import ru.otus.sales.leads.generator.inf.common.models.{AuthId, ErrorInfo}
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.plainBody
import sttp.tapir.ztapir.ZEndpoint
import sttp.tapir.ztapir._
import io.circe.generic.auto._
import ru.otus.sales.leads.generator.services.cores.users.models.{UserReg, UserRegError}
import sttp.model.StatusCode.BadRequest
import sttp.tapir.generic.auto._
import ru.otus.sales.leads.generator.inf.common.extensions.ListOpts

object UserEndpoint {
  val register: ZEndpoint[UserReg, ErrorInfo[UserRegError], Boolean] =
    endpoint
      .description("Регистрация нового пользователя")
      .post
      .in("users" / "register")
      .in(
        jsonBody[UserReg]
          .description("Модель регистрации")
          .example(UserReg("Александр", "Павлычев", 156)))
      .errorOut(
        jsonBody[ErrorInfo[UserRegError]]
          .description("Ошибки регистрации")
          .example(
            ErrorInfo[UserRegError](
              "Ошибка",
              BadRequest,
              ~UserRegError.AlreadyRegistered("Александр"))))
      .out(plainBody[Boolean])
}
