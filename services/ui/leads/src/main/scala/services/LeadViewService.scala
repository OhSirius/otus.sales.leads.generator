package ru.otus.sales.leads.generator.services.ui.leads
package services

import ru.otus.sales.leads.generator.inf.common.extensions.ListOpts
import ru.otus.sales.leads.generator.data.domain.entities.User
import ru.otus.sales.leads.generator.inf.repository.transactors.{DBTransactor, makeTransactor}
import ru.otus.sales.leads.generator.services.ui.leads.models.{LeadView, LeadViewError}
import ru.otus.sales.leads.generator.services.ui.leads.repositories.LeadRepository
import ru.otus.sales.leads.generator.services.ui.leads.repositories.LeadRepository.LeadRepository
import zio.{Has, URLayer, ZIO, ZLayer}
import zio.logging.Logging
import zio.interop.catz._

object LeadViewService {
  type LeadViewService = Has[Service]

  trait Service {
    def getActive(
        top: Int,
        user: User): ZIO[DBTransactor with Logging, LeadViewError, List[LeadView]]
  }

  class ServiceImpl(repo: LeadRepository.Service) extends Service {
    import doobie.implicits._

    override def getActive(
        top: Int,
        user: User): ZIO[DBTransactor with Logging, LeadViewError, List[LeadView]] =
      (for {
        _ <- Logging.info(s"Поиск активных лидов пользователя $user")
        trans <- makeTransactor
        user <- repo
          .getActive(top, user.id.get)
          .transact(trans)
          .orElseFail(LeadViewError.LostConnection)
        _ <- Logging.info(s"Завершение поиска активных лидов пользователя $user")
      } yield user).tapCause(Logging.error("Поиск активных лидов пользователя", _))
  }

  val live: URLayer[LeadRepository, LeadViewService] =
    ZLayer.fromService[LeadRepository.Service, Service](new ServiceImpl(_))

  def getActive(top: Int, user: User)
      : ZIO[LeadViewService with DBTransactor with Logging, LeadViewError, List[LeadView]] =
    ZIO.accessM(_.get.getActive(top, user))

}
