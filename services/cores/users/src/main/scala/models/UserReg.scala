package ru.otus.sales.leads.generator.services.cores.users
package models

import scala.util.Try

/** DTO-модель регистрации пользователя через TelegramBot
  * @param name
  * @param surname
  * @param bot
  */
case class UserReg(name: UserRegName, surname: UserRegSurname, bot: UserRegBot)

final case class UserRegName(value: String)
object UserRegName {
  def unapply(str: String): Option[UserRegName] = Option(
    UserRegName(str)
  )
}

final case class UserRegSurname(value: String)
object UserRegSurname {
  def unapply(str: String): Option[UserRegSurname] = Option(
    UserRegSurname(str)
  )
}

final case class UserRegBot(value: Int)
object RegBot {
  def unapply(str: String): Option[UserRegBot] =
    Try(UserRegBot(str.toInt)).toOption
}
