package ru.otus.sales.leads.generator.apps

import ru.otus.sales.leads.generator.apps.api.config.Configuration
import ru.otus.sales.leads.generator.inf.repository.transactors.DBTransactor
import ru.otus.sales.leads.generator.services.cores.leads.services.LeadService.LeadService
import ru.otus.sales.leads.generator.services.cores.users.services.UserLoginService.UserLoginService
import ru.otus.sales.leads.generator.services.cores.users.services.UserRegService.UserRegService
import ru.otus.sales.leads.generator.services.ui.leads.services.LeadViewService.LeadViewService
import zio.RIO
import zio.blocking.Blocking
import zio.clock.Clock
import zio.logging.Logging

package object api {
  type AppEnvironment = UserRegService
    with DBTransactor
    with Clock
    with Blocking
    with Configuration
    with Logging
    with UserLoginService
    with LeadService
    with LeadViewService

  type AppTask[A] = RIO[AppEnvironment, A]
}
