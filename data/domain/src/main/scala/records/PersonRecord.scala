package ru.otus.sales.leads.generator.data.domain
package records

import entities.{FullName, Person, PersonId, Phone}

import java.util.Date

case class PersonRecord(id: Option[PersonId], fullName: FullName, phone: Phone, createDate: Date) {
  def to() = Person(id, fullName, phone, createDate)
}

object PersonRecord {
  def from(person: Person) =
    PersonRecord(person.id, person.fullName, person.phone, person.createDate)
}
