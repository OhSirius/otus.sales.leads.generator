package ru.otus.sales.leads.generator.services.cores.leads
package bootstrap

import ru.otus.sales.leads.generator.services.cores.leads.repositories.{
  LeadRepository,
  PersonRepository
}
import ru.otus.sales.leads.generator.services.cores.leads.services.LeadService
import ru.otus.sales.leads.generator.services.cores.leads.services.LeadService.LeadService
import ru.otus.sales.leads.generator.services.cores.leads.validators.LeadValidator
import zio.ULayer

object LeadConfig {
  val live: ULayer[LeadService] =
    PersonRepository.live ++ LeadRepository.live ++ LeadValidator.live >>> LeadService.live
}
