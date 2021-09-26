package ru.otus.sales.leads.generator.services.cores.bot
package config

import zio.Has

object BotApiConfiguration {
  type BotApiConfiguration = Has[BotApiConfig]

  type BotApiHost = String
  type BotApiPort = Int

  final case class BotApiConfig(host: BotApiHost, port: BotApiPort)
}
