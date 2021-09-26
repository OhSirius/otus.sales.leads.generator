package ru.otus.sales.leads.generator.services.cores.leads
package endpoints

import ru.otus.sales.leads.generator.data.domain.entities.BotId
import ru.otus.sales.leads.generator.inf.common.endpoints.AuthEndpoint.authEndpoint
import ru.otus.sales.leads.generator.inf.common.models.{AuthId, ErrorInfo}
import ru.otus.sales.leads.generator.services.cores.leads.models.{LeadError, LeadInfo}
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.plainBody
import sttp.tapir.ztapir.ZEndpoint
import sttp.tapir.ztapir._
import io.circe.generic.auto._
import sttp.tapir.generic.auto._

object LeadEndpoint {

  val create: ZEndpoint[(AuthId, LeadInfo), ErrorInfo[LeadError], Boolean] =
    authEndpoint[LeadError]
      .description("Создание лида")
      .post
      .in("leads" / "create")
      .in(
        jsonBody[LeadInfo]
          .description("Модель лида")
          .example(LeadInfo("Павлычев Александр", "89202921268", 156.1)))
      .out(plainBody[Boolean])

}
