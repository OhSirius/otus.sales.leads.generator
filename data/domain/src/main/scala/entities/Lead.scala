package ru.otus.sales.leads.generator.data.domain
package entities

case class LeadId(id: Int) extends AnyVal
case class Lead(id: Option[LeadId], person: Person, user: User, price: BigDecimal)
