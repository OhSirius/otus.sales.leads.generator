package ru.otus.sales.leads.generator.apps.api
package api

import sttp.tapir.ztapir.{ZEndpoint, ZServerEndpoint}
import ru.otus.sales.leads.generator.inf.common.extensions.ListOpts
import ru.otus.sales.leads.generator.inf.repository.transactors.DBTransactor
import ru.otus.sales.leads.generator.services.cores.leads.models.{LeadError, LeadInfo}
import ru.otus.sales.leads.generator.services.cores.leads.services.LeadService.LeadService
import ru.otus.sales.leads.generator.services.cores.users.services.UserLoginService.UserLoginService
import sttp.tapir.json.circe.jsonBody
import zio.logging.{LogAnnotation, Logging, log}
import sttp.tapir.generic.auto._
import sttp.tapir.server.http4s.ztapir.ZHttp4sServerInterpreter
import zio.clock.Clock
import sttp.tapir.ztapir._
import io.circe.generic.auto._
import org.http4s.HttpRoutes
import ru.otus.sales.leads.generator.apps.api.logging.Logger.botId
import ru.otus.sales.leads.generator.data.domain.entities.{BotId, LeadId, User}
import ru.otus.sales.leads.generator.inf.common.models.{AuthId, ErrorInfo}
import ru.otus.sales.leads.generator.services.cores.leads.endpoints.LeadEndpoint
import ru.otus.sales.leads.generator.services.cores.leads.services.LeadService
import ru.otus.sales.leads.generator.services.cores.users.models.UserLogin
import ru.otus.sales.leads.generator.services.ui.leads.endpoints.LeadViewEndpoint
import ru.otus.sales.leads.generator.services.ui.leads.models.{LeadView, LeadViewError, LeadViewTop}
import ru.otus.sales.leads.generator.services.ui.leads.services.LeadViewService.LeadViewService
import sttp.model.StatusCode.{BadRequest, Unauthorized}
import zio.{IO, RIO, UIO, ZIO}
import ru.otus.sales.leads.generator.services.ui.leads.services.LeadViewService
import zio.interop.catz._
import java.util.UUID

class LeadApi[
    R <: LeadViewService with UserLoginService with LeadService with DBTransactor with Logging]
    extends AuthApi {

  val createServerEndpoint: ZServerEndpoint[R, (AuthId, LeadInfo), ErrorInfo[LeadError], Int] =
    LeadEndpoint.create
      .zServerLogicPart[R, AuthId, LeadInfo, User](authorize(_))
      .andThen { case (user, info) =>
        for {
          correlationId <- UIO(Some(UUID.randomUUID()))
          id <- log.locally(
            _.annotate(botId, user.botId).annotate(LogAnnotation.CorrelationId, correlationId)) {
            LeadService.create(info, user).mapError(ErrorInfo("Ошибка", BadRequest, _))
          }
        } yield id
      }

  val createRoutes: HttpRoutes[ZIO[R with Clock, Throwable, *]] =
    ZHttp4sServerInterpreter
      .from(createServerEndpoint)
      .toRoutes

  val getActiveServerEndpoint
      : ZServerEndpoint[R, (AuthId, LeadViewTop), ErrorInfo[LeadViewError], List[LeadView]] =
    LeadViewEndpoint.getActive
      .zServerLogicPart[R, AuthId, LeadViewTop, User](authorize(_)) //
      .andThen { case (user, top) =>
        for {
          correlationId <- UIO(Some(UUID.randomUUID()))
          leads <- log.locally(
            _.annotate(botId, user.botId).annotate(LogAnnotation.CorrelationId, correlationId)) {
            LeadViewService
              .getActive(top, user)
              .mapError(e => ErrorInfo("Ошибка", BadRequest, ~e))
          }
        } yield leads
      }

  val getActiveRoutes: HttpRoutes[ZIO[R with Clock, Throwable, *]] =
    ZHttp4sServerInterpreter
      .from(getActiveServerEndpoint)
      .toRoutes

}
