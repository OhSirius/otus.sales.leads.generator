package ru.otus.sales.leads.generator.services.cores.leads
package repositories

import cats.effect.IO
import ru.otus.sales.leads.generator.data.domain.entities.{FullName, Lead, Person, Phone, User}
import ru.otus.sales.leads.generator.data.domain.records.{LeadRecord, PersonRecord}
import ru.otus.sales.leads.generator.inf.repository.transactors.{doobieContext => dc}
import zio.{Has, ULayer, ZLayer}

object LeadRepository {
  import dc._
  type LeadRepository = Has[Service]

  trait Service {
    def create(lead: Lead): Result[Lead]
  }

  class ServiceImpl extends Service {
    val leadRecordSchema = quote {
      querySchema[LeadRecord](""""leads"""")
    }

    override def create(lead: Lead): Result[Lead] = for {
      leadRecord <- IO
        .fromEither(
          LeadRecord
            .from(lead.copy(id = None))
            .toRight(new Throwable(s"Ошибка в данных")))
        .to[Result]
      lead <- dc
        .run(
          leadRecordSchema
            .insert(lift(leadRecord))
            .returningGenerated(_.id))
        .map(x => lead.copy(id = x))
    } yield lead
  }

  val live: ULayer[LeadRepository] = ZLayer.succeed(new ServiceImpl())
}
