package ru.otus.sales.leads.generator.services.cores.users
package repositories

import doobie.quill.DoobieContext
import io.getquill.{CompositeNamingStrategy2, Escape, Literal}
import ru.otus.sales.leads.generator.data.domain.entities.{BotId, Lead, LeadId, User}
import ru.otus.sales.leads.generator.data.domain.records.{LeadRecord, UserRecord}
import ru.otus.sales.leads.generator.inf.repository.transactor.{doobieContext => dc}
import zio.{ULayer, URLayer, ZLayer}
//import ru.otus.sales.leads.generator.services.cores.users.models.BotId
import zio.Has

object UserRepository {
  //val dc: DoobieContext.Postgres[CompositeNamingStrategy2[Escape.type, Literal.type]] =
  //  doobieContext //DBTransactor.
  //import dc._
  import dc._

  type UserRepository = Has[Service]

  trait Service {
    def getByBot(botId: BotId): Result[Option[User]]
    def create(user: User): Result[User]
    //def find(phone: String): Result[Option[PhoneRecord]]
    //def list(): Result[List[PhoneRecord]]
    //def insert(phoneRecord: PhoneRecord): Result[Unit]
    //def update(phoneRecord: PhoneRecord): Result[Unit]
    //def delete(id: String): Result[Unit]
  }

  class ServiceImpl extends Service {

    val userRecordSchema = quote {
      querySchema[UserRecord](""""users"""")
    }

    val leadSchema = quote {
      querySchema[LeadRecord](""""leads"""")
    }

    override def getByBot(botId: BotId): Result[Option[User]] =
      dc.run(userRecordSchema.filter(_.botId == lift(botId))).map(_.headOption.map(_.to()))

    override def create(user: User): Result[User] =
      dc.run(
        userRecordSchema
          .insert(lift(UserRecord.from(user.copy(id = None))))
          .returningGenerated(_.id))
        .map(x => user.copy(id = x))
  }

  val live: ULayer[UserRepository] = ZLayer.succeed(new ServiceImpl())

}
