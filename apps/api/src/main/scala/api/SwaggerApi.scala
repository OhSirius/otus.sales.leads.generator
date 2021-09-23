package ru.otus.sales.leads.generator.apps.api
package api

import WebApp.{AppEnvironment, AppTask}

import org.http4s.HttpRoutes
import sttp.tapir.swagger.http4s.SwaggerHttp4s
import sttp.tapir.json.circe._
import sttp.tapir.generic.auto._
import sttp.tapir.server.http4s.ztapir.ZHttp4sServerInterpreter
import sttp.tapir.swagger.http4s.SwaggerHttp4s
import zio.clock.Clock
import zio.interop.catz._
import zio.{App, ExitCode, IO, RIO, UIO, URIO, ZEnv, ZIO}

object SwaggerApi {
  val yaml: String = {
    import sttp.tapir.docs.openapi.OpenAPIDocsInterpreter
    import sttp.tapir.openapi.circe.yaml._
    OpenAPIDocsInterpreter()
      .toOpenAPI(new UserApi[AppEnvironment].registerEndpoint, "Пользователи", "1.0")
      .toYaml
  }

  val routes: HttpRoutes[AppTask] = new SwaggerHttp4s(yaml).routes[AppTask]
}
