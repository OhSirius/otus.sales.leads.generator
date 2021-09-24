package ru.otus.sales.leads.generator.data.domain
package records

import entities.{FullName, Person, PersonId, Phone}

case class PersonRecord(id: Option[PersonId], fullName: FullName, phone: Phone) {
  def to() = Person(id, fullName, phone)
}

object PersonRecord {
  def from(person: Person) = PersonRecord(person.id, person.fullName, person.phone)
}
