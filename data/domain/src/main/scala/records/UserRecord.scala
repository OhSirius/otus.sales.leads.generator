package ru.otus.sales.leads.generator.data.domain
package records

import entities.{BotId, Name, Surname, User, UserId}

case class UserRecord(id: Option[UserId], name: Name, surname: Surname, botId: BotId) {
  def to() = User(id, name, surname, botId)
}
object UserRecord {
  def from(user: User) = UserRecord(user.id, user.name, user.surname, user.botId)
}
