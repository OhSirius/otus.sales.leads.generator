package ru.otus.sales.leads.generator.apps.bot

import pureconfig.ConfigSource
import ru.otus.sales.leads.generator.services.cores.bot.config.BotApiConfiguration.{
  BotApiConfig,
  BotApiConfiguration
}
import zio.{Has, Layer, Task, ZLayer}

package object config {
  type Configuration = Has[BotConfig] with BotApiConfiguration //DbConfiguration

  final case class AppConfig(bot: BotConfig, api: BotApiConfig)
  final case class BotConfig(token: String)

  object Configuration {
    import pureconfig.generic.auto._
    val live: Layer[Throwable, Configuration] = ZLayer.fromEffectMany(
      Task
        .effect(ConfigSource.default.loadOrThrow[AppConfig])
        .map(c => Has(c.bot) ++ Has(c.api))
    )
  }
}
