package ru.otus.sales.leads.generator.inf.repository
package transactors

import zio.interop.catz._
import cats.effect.Blocker
import doobie.hikari.HikariTransactor
import ru.otus.sales.leads.generator.inf.repository.config.DbConfiguration.{
  DbConfig,
  DbConfiguration
}
import zio.{Task, ZIO, ZLayer, ZManaged}
import zio.blocking.Blocking

/** Реализация ZLayer[Transactor] на базе HikariTransactor
  */
object DbHikariTransactor {

  val transactorResource: ZManaged[
    Blocking with DbConfiguration,
    Throwable,
    HikariTransactor[Task]
  ] =
    for {
      config <- ZIO.service[DbConfig].toManaged_
      ec <- ZIO.descriptor.map(_.executor.asEC).toManaged_
      blocingEC <- zio.blocking.blockingExecutor.map(_.asEC).toManaged_
      transactor <- HikariTransactor
        .newHikariTransactor[Task](
          config.driver,
          config.url,
          config.user,
          config.password,
          ec,
          Blocker.liftExecutionContext(blocingEC)
        )
        .toManagedZIO
    } yield transactor

  val live: ZLayer[DbConfiguration with Blocking, Throwable, DBTransactor] =
    ZLayer.fromManaged(transactorResource)

}
