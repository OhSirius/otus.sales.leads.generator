package ru.otus.sales.leads.generator.data.domain
package records

import entities.{Lead, LeadId, Person, PersonId, User, UserId}

import java.util.Date

case class LeadRecord(
    id: Option[LeadId],
    person: PersonId,
    user: UserId,
    price: BigDecimal,
    createDate: Date)
object LeadRecord {
  def from(lead: Lead): Option[LeadRecord] = (lead.user, lead.person) match {
    case (User(Some(userId), _, _, _), Person(Some(personId), _, _, _)) =>
      Some(LeadRecord(lead.id, personId, userId, lead.price, lead.createDate))
    case _ => None
  }
}
