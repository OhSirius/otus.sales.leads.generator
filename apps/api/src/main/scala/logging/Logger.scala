package ru.otus.sales.leads.generator.apps.api
package logging

import zio.ULayer
import zio.logging.{LogAnnotation, Logging}
import zio.logging.slf4j.Slf4jLogger

object Logger {
  val live: ULayer[Logging] =
    Slf4jLogger.make((context, message) => message)
  //"[correlation-id = %s] %s".format(context(correlationId), message))

  val botId = LogAnnotation[Int](
    name = "bot-id",
    initialValue = 0,
    combine = (_, newValue) => newValue,
    render = _.toString
  )

  val liveWithMdc = Slf4jLogger.makeWithAnnotationsAsMdc(
    List(botId, LogAnnotation.CorrelationId, LogAnnotation.Cause))
}
