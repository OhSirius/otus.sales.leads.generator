package ru.otus.sales.leads.generator.services.cores.leads
package repositories

import cats.effect.IO
import ru.otus.sales.leads.generator.data.domain.entities.{FullName, Lead, Person, Phone, User}
import ru.otus.sales.leads.generator.data.domain.records.{LeadRecord, PersonRecord}
import ru.otus.sales.leads.generator.inf.repository.transactors.{doobieContext => dc}
import zio.{Has, ULayer, ZLayer}

object PersonRepository {
  import dc._
  type PersonRepository = Has[Service]

  trait Service {
    def findBy(fullName: FullName, phone: Phone): Result[Option[Person]]
    def create(person: Person): Result[Person]
  }

  class ServiceImpl extends Service {
    val personRecordSchema = quote {
      querySchema[PersonRecord](""""persons"""")
    }

    override def findBy(fullName: FullName, phone: Phone): Result[Option[Person]] =
      dc.run(personRecordSchema.filter(p => p.fullName == lift(fullName) || p.phone == lift(phone)))
        .map(_.headOption.map(_.to()))

    override def create(person: Person): Result[Person] =
      dc.run(
        personRecordSchema
          .insert(lift(PersonRecord.from(person.copy(id = None))))
          .returningGenerated(_.id))
        .map(x => person.copy(id = x))
  }

  val live: ULayer[PersonRepository] = ZLayer.succeed(new ServiceImpl())

}
