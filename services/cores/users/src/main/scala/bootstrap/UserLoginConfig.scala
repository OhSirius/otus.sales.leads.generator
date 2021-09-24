package ru.otus.sales.leads.generator.services.cores.users
package bootstrap

import repositories.UserRepository
import ru.otus.sales.leads.generator.services.cores.users.services.UserLoginService
import ru.otus.sales.leads.generator.services.cores.users.services.UserLoginService.UserLoginService
import ru.otus.sales.leads.generator.services.cores.users.validators.UserLoginValidator
import zio.{ULayer, URLayer}

object UserLoginConfig {
  val live: ULayer[UserLoginService] =
    UserRepository.live ++ UserLoginValidator.live >>> UserLoginService.live
}
