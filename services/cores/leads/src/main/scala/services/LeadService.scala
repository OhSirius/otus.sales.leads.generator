package ru.otus.sales.leads.generator.services.cores.leads
package services

import cats.effect.IO
import ru.otus.sales.leads.generator.data.domain.entities.{Lead, Person, User}
import ru.otus.sales.leads.generator.inf.repository.transactors.{DBTransactor, makeTransactor}
import ru.otus.sales.leads.generator.services.cores.leads.models.{LeadError, LeadInfo}
import ru.otus.sales.leads.generator.services.cores.leads.repositories.{
  LeadRepository,
  PersonRepository
}
import ru.otus.sales.leads.generator.services.cores.leads.validators.LeadValidator
import zio.logging.Logging
import zio.{Has, URLayer, ZIO, ZLayer}
import ru.otus.sales.leads.generator.inf.repository.transactors.{doobieContext => dc}
import zio.interop.catz._
import ru.otus.sales.leads.generator.inf.common.extensions.ListOpts
import ru.otus.sales.leads.generator.services.cores.leads.repositories.LeadRepository.LeadRepository
import ru.otus.sales.leads.generator.services.cores.leads.repositories.PersonRepository.PersonRepository
import ru.otus.sales.leads.generator.services.cores.leads.validators.LeadValidator.LeadValidator

import java.util.Date

object LeadService {
  type LeadService = Has[Service]

  trait Service {
    def create(info: LeadInfo, user: User): ZIO[DBTransactor with Logging, ::[LeadError], Int]
  }

  class ServiceImpl(
      leadRepo: LeadRepository.Service,
      personRepo: PersonRepository.Service,
      validator: LeadValidator.Service)
      extends Service {
    import doobie.implicits._
    import dc._

    override def create(
        info: LeadInfo,
        user: User): ZIO[DBTransactor with Logging, ::[LeadError], Int] =
      (for {
        _ <- Logging.info(s"Начало создания лида $info")
        _ <- validator.validate(info)
        trans <- makeTransactor
        lead <- (for {
          person <- personRepo
            .findBy(info.fullName, info.phone)
            .flatMap(p =>
              p.fold(personRepo.create(Person(None, info.fullName, info.phone, new Date())))(
                IO.pure(_).to[Result]))
          lead <- leadRepo.create(Lead(None, person, user, info.price, new Date()))
        } yield lead)
          .transact(trans)
          .tapCause(Logging.error("Создание лида", _))
          .orElseFail(~LeadError.LostConnection)
        _ <- Logging.info(s"Завершение создания лида $lead")
      } yield lead.id.map(_.id).getOrElse(-1)).tapCause(Logging.error("Создание лида", _))
  }

  val live: URLayer[LeadRepository with PersonRepository with LeadValidator, LeadService] =
    ZLayer.fromServices[
      LeadRepository.Service,
      PersonRepository.Service,
      LeadValidator.Service,
      Service]((leadRepo, personRepo, validator) =>
      new ServiceImpl(leadRepo, personRepo, validator))

  def create(
      info: LeadInfo,
      user: User): ZIO[LeadService with DBTransactor with Logging, ::[LeadError], Int] =
    ZIO.accessM(_.get.create(info, user))

}
