package ru.otus.sales.leads.generator.apps.api
package api

import org.http4s.HttpRoutes
import ru.otus.sales.leads.generator.services.cores.leads.endpoints.LeadEndpoint
import ru.otus.sales.leads.generator.services.cores.users.endpoints.UserEndpoint
import ru.otus.sales.leads.generator.services.ui.leads.endpoints.LeadViewEndpoint
import sttp.tapir.swagger.http4s.SwaggerHttp4s
import zio.interop.catz._

object SwaggerApi {
  val yaml: String = {
    import sttp.tapir.docs.openapi.OpenAPIDocsInterpreter
    import sttp.tapir.openapi.circe.yaml._

    OpenAPIDocsInterpreter
      .toOpenAPI(
        List(UserEndpoint.register, LeadEndpoint.create, LeadViewEndpoint.getActive),
        "CRM",
        "1.0")
      .toYaml
  }

  val routes: HttpRoutes[AppTask] = new SwaggerHttp4s(yaml).routes[AppTask]
}
