package ru.otus.sales.leads.generator.inf.repository
package transactor

import config.{DbConfig, DbConfiguration}

import zio.interop.catz._
import cats.effect.Blocker
import doobie.hikari.HikariTransactor
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
      //config <- ZIO
      //  .access[DbConfiguration](_.get)
      //  .toManaged_ //ZIO.environment[Has[DbConfig]].get.toManaged_
//      //.get //[DbConfig] // zio.config.getConfig[Config].toManaged_
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

//    for {
//      blocking <- ZIO.blockingExecutor.map(_.asExecutionContext).toManaged
//      runtime <- ZIO.runtime[Any].toManaged_
//      transactor <- HikariTransactor
//        .newHikariTransactor[Task](
//          ???,
//          ???,
//          ???,
//          ???,
//          runtime.platform.executor.asExecutionContext,
//          Blocker.liftExecutionContext(blocking)
//        )
//        .toManagedZIO
//    } yield transactor
