package ru.otus.sales.leads.generator.inf.common
package endpoints

import models.{AuthId, ErrorInfo}

import io.circe.{Decoder, Encoder}
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.{Endpoint, Schema, endpoint, header}

object AuthEndpoint {
  def authEndpoint[T](implicit
      E: Encoder[ErrorInfo[T]],
      D: Decoder[ErrorInfo[T]],
      S: Schema[ErrorInfo[T]]): Endpoint[AuthId, ErrorInfo[T], Unit, Any] =
    endpoint
      //.in(auth.apiKey(header[BotId]("X-Auth-Token")))
      .in(header[AuthId]("X-Auth-Token"))
      .errorOut(jsonBody[ErrorInfo[T]].description("Модель ошибки"))
}
