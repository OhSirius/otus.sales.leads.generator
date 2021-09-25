package ru.otus.sales.leads.generator.apps.api
package api

import io.circe.Encoder
import sttp.tapir.ztapir.{ZEndpoint, ZServerEndpoint}
import ru.otus.sales.leads.generator.inf.common.extensions.ListOpts
import ru.otus.sales.leads.generator.inf.repository.transactors.DBTransactor
import ru.otus.sales.leads.generator.services.cores.leads.models.{LeadError, LeadInfo}
import ru.otus.sales.leads.generator.services.cores.leads.services.LeadService.LeadService
import ru.otus.sales.leads.generator.services.cores.users.services.UserLoginService.{
  UserLoginService,
  login
}
import sttp.tapir.{Codec, Schema, SchemaType, endpoint}
import sttp.tapir.json.circe.jsonBody
import zio.logging.{LogAnnotation, Logging, log}
import sttp.tapir.generic.auto._
import sttp.tapir.server.http4s.ztapir.ZHttp4sServerInterpreter
import zio.clock.Clock
import sttp.tapir.ztapir._
import io.circe.generic.auto._
import org.http4s.HttpRoutes
import ru.otus.sales.leads.generator.apps.api.logging.Logger.botId
import ru.otus.sales.leads.generator.apps.api.models.ErrorInfo
import ru.otus.sales.leads.generator.data.domain.entities.{BotId, User}
import ru.otus.sales.leads.generator.services.cores.leads.services.LeadService
import ru.otus.sales.leads.generator.services.cores.users.models.UserLogin
import ru.otus.sales.leads.generator.services.ui.leads.models.{LeadView, LeadViewError, LeadViewTop}
import ru.otus.sales.leads.generator.services.ui.leads.services.LeadViewService.LeadViewService
import sttp.model.StatusCode.{BadRequest, Unauthorized}
//import sttp.tapir.EndpointInput.Auth.ApiKey
import zio.{IO, RIO, UIO, ZIO}
//import sttp.tapir._
//import sttp.tapir.generic.auto._
//import io.circe.{Decoder, Encoder}
//import io.circe.generic.semiauto._
import ru.otus.sales.leads.generator.services.ui.leads.services.LeadViewService
import sttp.tapir.Codec.stringCodec
import sttp.tapir.CodecFormat.TextPlain
//import sttp.tapir.generic.auto._
//import sttp.tapir.json.circe._
//import sttp.tapir._

import java.util.UUID

class LeadApi[
    R <: LeadViewService with UserLoginService with LeadService with DBTransactor with Logging]
    extends AuthApi {

  val createEndpoint: ZEndpoint[(BotId, LeadInfo), ErrorInfo[LeadError], Boolean] =
    authEndpoint[LeadError]
      .description("Создание лида")
      .post
      .in("leads" / "create")
      .in(
        jsonBody[LeadInfo]
          .description("Модель лида")
          .example(LeadInfo("Павлычев Александр", "89202921268", 156.1)))
//      .errorOut(jsonBody[::[LeadError]]
//        .description("Ошибки создания лида")
//        .example(~LeadError.BadPrice(BigDecimal(17.4))))
      .out(plainBody[Boolean])

  val createServerEndpoint: ZServerEndpoint[R, (BotId, LeadInfo), ErrorInfo[LeadError], Boolean] =
    createEndpoint
      .zServerLogicPart[R, BotId, LeadInfo, User](authorize(_))
      .andThen { case (user, info) =>
        for {
          correlationId <- UIO(Some(UUID.randomUUID()))
          _ <- log.locally(
            _.annotate(botId, user.botId).annotate(LogAnnotation.CorrelationId, correlationId)) {
            LeadService.create(info, user).mapError(ErrorInfo[LeadError]("Ошибка", BadRequest, _))
          }
        } yield true
      }

  val createRoutes: HttpRoutes[ZIO[R with Clock, Throwable, *]] =
    ZHttp4sServerInterpreter[R]()
      .from(createServerEndpoint)
      .toRoutes

  val getActiveEndpoint: ZEndpoint[(BotId, LeadViewTop), ErrorInfo[LeadViewError], List[LeadView]] =
    authEndpoint[LeadViewError]
      .description("Активные лиды")
      .get
      .in("leads" / "active" / query[LeadViewTop]("top").description("Количество лидов"))
      .out(jsonBody[List[LeadView]].description("Список активных лидов"))

  val getActiveServerEndpoint
      : ZServerEndpoint[R, (BotId, LeadViewTop), ErrorInfo[LeadViewError], List[LeadView]] =
    getActiveEndpoint
      .zServerLogicPart[R, BotId, LeadViewTop, User](authorize(_)) //
      .andThen { case (user, top) =>
        for {
          correlationId <- UIO(Some(UUID.randomUUID()))
          leads <- log.locally(
            _.annotate(botId, user.botId).annotate(LogAnnotation.CorrelationId, correlationId)) {
            LeadViewService
              .getActive(top, user)
              .mapError(e => ErrorInfo[LeadViewError]("Ошибка", BadRequest, ~e))
          }
        } yield leads
      }

  val getActiveRoutes: HttpRoutes[ZIO[R with Clock, Throwable, *]] =
    ZHttp4sServerInterpreter[R]()
      .from(getActiveServerEndpoint)
      .toRoutes

}
