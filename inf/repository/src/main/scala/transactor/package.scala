package ru.otus.sales.leads.generator.inf.repository

import doobie.Transactor
import doobie.quill.DoobieContext
import io.getquill.{Escape, Literal, NamingStrategy}
import zio.{Has, Task, URIO, ZIO}

/** Объявление сервиса DBTransactor
  */
package object transactor {
  type DBTransactor = Has[Transactor[Task]]

  val doobieContext =
    new DoobieContext.Postgres(NamingStrategy(Escape, Literal)) // Literal naming scheme

  //https://blog.rockthejvm.com/structuring-services-with-zio-zlayer/
  def makeTransactor: URIO[DBTransactor, Transactor[Task]] = ZIO.service[Transactor[Task]]
}
