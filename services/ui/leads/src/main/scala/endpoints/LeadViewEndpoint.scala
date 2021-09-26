package ru.otus.sales.leads.generator.services.ui.leads
package endpoints

import ru.otus.sales.leads.generator.inf.common.models.ErrorInfo
import ru.otus.sales.leads.generator.inf.common.endpoints.AuthEndpoint.authEndpoint
import ru.otus.sales.leads.generator.inf.common.models.{AuthId, ErrorInfo}
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.plainBody
import sttp.tapir.ztapir.ZEndpoint
import sttp.tapir.ztapir._
import io.circe.generic.auto._
import ru.otus.sales.leads.generator.services.ui.leads.models.{LeadView, LeadViewError, LeadViewTop}
import sttp.tapir.generic.auto._

object LeadViewEndpoint {
  val getActive: ZEndpoint[(AuthId, LeadViewTop), ErrorInfo[LeadViewError], List[LeadView]] =
    authEndpoint[LeadViewError]
      .description("Активные лиды")
      .get
      .in("leads" / "active" / query[LeadViewTop]("top").description("Количество лидов"))
      .out(
        jsonBody[List[LeadView]]
          .description("Список активных лидов")
          .example(List(LeadView(1, "Павлычев Александр", "89202921268", 13.2))))

}
