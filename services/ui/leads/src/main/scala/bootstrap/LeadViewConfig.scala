package ru.otus.sales.leads.generator.services.ui.leads
package bootstrap

import services.LeadViewService.LeadViewService

import ru.otus.sales.leads.generator.services.ui.leads.repositories.LeadRepository
import ru.otus.sales.leads.generator.services.ui.leads.services.LeadViewService
import zio.ULayer

object LeadViewConfig {
  val live: ULayer[LeadViewService] =
    LeadRepository.live >>> LeadViewService.live
}
