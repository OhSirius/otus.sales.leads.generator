package ru.otus.sales.leads.generator.inf.repository

import zio.{Has, ULayer, URLayer, ZLayer}

package object config {
  type DbConfiguration = Has[DbConfig]

  type DbDriver = String
  type DbUrl = String
  type DbUser = String
  type DbPass = String

  final case class DbConfig(driver: DbDriver, url: DbUrl, user: DbUser, password: DbPass)

  val dbConfigLive: ULayer[DbConfiguration] = ZLayer.succeed(
    DbConfig(
      "org.postgresql.Driver",
      "jdbc:postgresql://127.0.0.1/demo?createDatabaseIfNotExist=true",
      "docker",
      "docker"))
}
