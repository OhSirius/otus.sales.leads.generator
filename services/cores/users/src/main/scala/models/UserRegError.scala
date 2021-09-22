package ru.otus.sales.leads.generator.services.cores.users
package models

/** DTO-модель ошибки регистрации
  */
sealed trait UserRegError
object UserRegError {
  case class AlreadyRegistered(name: String) extends UserRegError
  case object LostConnection extends UserRegError
}
