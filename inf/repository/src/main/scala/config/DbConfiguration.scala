package ru.otus.sales.leads.generator.inf.repository
package config

import zio.{Has, ULayer, ZLayer}

object DbConfiguration {
  type DbConfiguration = Has[DbConfig]

  type DbDriver = String
  type DbUrl = String
  type DbUser = String
  type DbPass = String
  final case class DbConfig(driver: DbDriver, url: DbUrl, user: DbUser, password: DbPass)

  val live: ULayer[DbConfiguration] = ZLayer.succeed(
    DbConfig(
      "org.postgresql.Driver",
      "jdbc:postgresql://127.0.0.1/otus?createDatabaseIfNotExist=true",
      "otus",
      "otus"))
}
