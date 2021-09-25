package ru.otus.sales.leads.generator.services.ui.leads
package models

sealed trait LeadViewError
object LeadViewError {
  case object LostConnection extends LeadViewError
}
