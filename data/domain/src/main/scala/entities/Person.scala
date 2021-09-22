package ru.otus.sales.leads.generator.data.domain
package entities

case class PersonId(int: Int) extends AnyVal
case class Person(id: Option[PersonId], fullName: FullName, phone: Phone)
