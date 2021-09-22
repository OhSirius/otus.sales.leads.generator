package ru.otus.sales.leads.generator.data.domain
package entities

case class UserId(id: Int) extends AnyVal
case class BotId(id: Int) extends AnyVal
//case class Role(id: Int, name: String) //extends Embedded
//case class Role2(id: Int) extends AnyVal
case class User(id: Option[UserId], name: Name, surname: Surname, botId: BotId)
