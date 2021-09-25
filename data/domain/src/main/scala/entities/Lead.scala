package ru.otus.sales.leads.generator.data.domain
package entities

import java.util.Date

case class LeadId(id: Int) extends AnyVal
case class Lead(id: Option[LeadId], person: Person, user: User, price: BigDecimal, createDate: Date)
