package ru.otus.sales.leads.generator.services.ui.leads
package models

import ru.otus.sales.leads.generator.data.domain.entities.LeadId

case class LeadView(
    id: Option[LeadId],
    fullName: LeadViewFullName,
    phone: LeadViewPhone,
    price: BigDecimal)
