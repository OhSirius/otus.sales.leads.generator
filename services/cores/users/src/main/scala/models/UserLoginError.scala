package ru.otus.sales.leads.generator.services.cores.users
package models

sealed trait UserLoginError extends Product with Serializable
object UserLoginError {
  case object NotExists extends UserLoginError
  case object InvalidBot extends UserLoginError
  case object LostConnection extends UserLoginError
}
