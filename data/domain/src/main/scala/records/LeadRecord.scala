package ru.otus.sales.leads.generator.data.domain
package records

import entities.{LeadId, PersonId, UserId}

case class LeadRecord(id: Option[LeadId], person: PersonId, user: UserId, price: BigDecimal)
