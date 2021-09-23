package ru.otus.sales.leads.generator.apps.api

import pureconfig.ConfigSource
import ru.otus.sales.leads.generator.inf.repository.config.DbConfiguration.{
  DbConfig,
  DbConfiguration
}
import zio.{Has, Layer, Task, URIO, ZIO, ZLayer}

//https://github.com/wi101/zio-examples/blob/master/src/main/scala/com/zio/examples/http4s_doobie/configuration/package.scala
package object config {
  type Configuration = Has[ApiConfig] with DbConfiguration //DbConfiguration

  final case class AppConfig(api: ApiConfig, db: DbConfig)
  final case class ApiConfig(host: String, port: Int)

  object Configuration {
    import pureconfig.generic.auto._
    val live: Layer[Throwable, Configuration] = ZLayer.fromEffectMany(
      Task
        .effect(ConfigSource.default.loadOrThrow[AppConfig])
        .map(c => Has(c.api) ++ Has(c.db))
    )
  }
}
