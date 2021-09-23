package ru.otus.sales.leads.generator.services.cores.users
package models

/** DTO-модель ошибки регистрации
  */
sealed trait UserRegError extends Product with Serializable
object UserRegError {
  case class AlreadyRegistered(name: String) extends UserRegError
  case object LostConnection extends UserRegError
  case object EmptyName extends UserRegError
  case object EmptySurname extends UserRegError
  case object InvalidBot extends UserRegError
}
