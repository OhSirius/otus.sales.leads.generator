package ru.otus.sales.leads.generator.services.cores.leads
package models

sealed trait LeadError
object LeadError {
  case object EmptyFullname extends LeadError
  case object EmptyPhone extends LeadError
  case class BadPrice(value: BigDecimal) extends LeadError
  case object LostConnection extends LeadError
}
