package ru.otus.sales.leads.generator.data.domain
package entities

import java.util.Date

case class PersonId(int: Int) extends AnyVal
case class Person(id: Option[PersonId], fullName: FullName, phone: Phone, createDate: Date)
