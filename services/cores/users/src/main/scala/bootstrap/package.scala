package ru.otus.sales.leads.generator.services.cores.users

import ru.otus.sales.leads.generator.inf.repository.transactor.DBTransactor
import ru.otus.sales.leads.generator.services.cores.users.repositories.UserRepository
import ru.otus.sales.leads.generator.services.cores.users.services.UserRegService
import ru.otus.sales.leads.generator.services.cores.users.services.UserRegService.UserRegService
import zio.URLayer
import zio.{ExitCode, Has, Task, ZIO, ZLayer}

package object bootstrap {
  val userServiceLive: URLayer[DBTransactor, UserRegService] =
    UserRepository.live >>> UserRegService.live
}
