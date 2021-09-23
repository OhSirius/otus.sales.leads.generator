package ru.otus.sales.leads.generator.services.cores.users
package models

/** DTO-модель регистрации пользователя через TelegramBot
  * @param name
  * @param surname
  * @param bot
  */
case class UserReg(name: UserRegName, surname: UserRegSurname, bot: UserRegBot)
