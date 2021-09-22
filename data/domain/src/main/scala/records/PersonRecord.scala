package ru.otus.sales.leads.generator.data.domain
package records

import entities.{FullName, PersonId, Phone}

case class PersonRecord(id: PersonId, fullName: FullName, phone: Phone)
