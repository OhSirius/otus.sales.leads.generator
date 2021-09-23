package ru.otus.sales.leads.generator.inf.repository

import doobie.Transactor
import doobie.quill.DoobieContext
import io.getquill.{Escape, Literal, NamingStrategy}
import zio.{Has, Task, URIO, ZIO}

/** Объявление сервиса DBTransactor
  */
package object transactors {
  type DBTransactor = Has[Transactor[Task]]

  val doobieContext =
    new DoobieContext.Postgres(NamingStrategy(Escape, Literal)) // Literal naming scheme

  def makeTransactor: URIO[DBTransactor, Transactor[Task]] = ZIO.service[Transactor[Task]]
}
