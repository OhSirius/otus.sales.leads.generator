package ru.otus.sales.leads.generator.services.ui.leads
package repositories

import io.getquill.Ord
import ru.otus.sales.leads.generator.data.domain.entities.UserId
import ru.otus.sales.leads.generator.data.domain.records.{LeadRecord, PersonRecord}
import ru.otus.sales.leads.generator.inf.repository.transactors.{doobieContext => dc}
import ru.otus.sales.leads.generator.services.ui.leads.models.LeadView
import zio.{Has, ULayer, ZLayer}

object LeadRepository {
  import dc._
  type LeadRepository = Has[Service]

  trait Service {
    def getActive(top: Int, userId: UserId): Result[List[LeadView]]
  }

  class ServiceImpl extends Service {
    val leadRecordSchema = quote {
      querySchema[LeadRecord](""""leads"""")
    }

    val personRecordSchema = quote {
      querySchema[PersonRecord](""""persons"""")
    }

    override def getActive(top: Int, userId: UserId): Result[List[LeadView]] = for {
      lead <- dc
        .run(
          leadRecordSchema
            .join(personRecordSchema)
            .on({ case (lead, person) => person.id.exists(_ == lead.person) })
            .take(lift(top))
            .filter({ case (lead, _) => lead.user == lift(userId) })
            .sortBy({ case (lead, _) => lead.createDate })(Ord.desc)
            .map({ case (lead, person) =>
              LeadView(lead.id, person.fullName, person.phone, lead.price)
            }))
    } yield lead
  }

  val live: ULayer[LeadRepository] = ZLayer.succeed(new ServiceImpl())

}
