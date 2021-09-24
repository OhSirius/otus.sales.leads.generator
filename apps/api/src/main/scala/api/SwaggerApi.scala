package ru.otus.sales.leads.generator.apps.api
package api

import WebApp.{AppEnvironment, AppTask}
import org.http4s.HttpRoutes
import sttp.tapir.swagger.http4s.SwaggerHttp4s
import zio.interop.catz._

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
