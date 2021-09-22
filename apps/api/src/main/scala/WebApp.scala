package ru.otus.sales.leads.generator.apps.api

import cats.syntax.all._
import io.circe.generic.auto._
import org.http4s._
import org.http4s.server.Router
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.syntax.kleisli._
import ru.otus.sales.leads.generator.apps.api.api.UserApi
import zio.console.Console
//import ru.otus.sales.leads.generator.apps.api.api.UserApi.{registerUserEndpoint, registerUserRoutes}
import ru.otus.sales.leads.generator.inf.repository.config.DbConfiguration
import ru.otus.sales.leads.generator.inf.repository.transactor.{DBTransactor, DbHikariTransactor}
import ru.otus.sales.leads.generator.inf.repository.config.dbConfigLive
import ru.otus.sales.leads.generator.services.cores.users.bootstrap.userServiceLive
import ru.otus.sales.leads.generator.services.cores.users.services.UserRegService.UserRegService
import sttp.tapir.json.circe._
import sttp.tapir.generic.auto._
import sttp.tapir.server.http4s.ztapir.ZHttp4sServerInterpreter
import sttp.tapir.swagger.http4s.SwaggerHttp4s
import zio.blocking.Blocking
//import sttp.tapir.swagger.SwaggerUI
import sttp.tapir.ztapir._
import zio.clock.Clock
import zio.blocking.Blocking
import zio.interop.catz._
import zio.{App, ExitCode, IO, RIO, UIO, URIO, ZEnv, ZIO}
import cats.effect.{ExitCode => CatsExitCode}

//https://github.com/softwaremill/tapir/tree/master/examples/src/main/scala/sttp/tapir/examples
object WebApp extends App {
  case class Pet(species: String, url: String)

  // Sample endpoint, with the logic implemented directly using .toRoutes
  val petEndpoint: ZEndpoint[Int, String, Pet] =
    endpoint.get.in("pet" / path[Int]("petId")).errorOut(stringBody).out(jsonBody[Pet])

  val petRoutes: HttpRoutes[RIO[Clock, *]] = ZHttp4sServerInterpreter() // with Blocking
    .from(petEndpoint) { petId =>
      if (petId == 35) {
        UIO(Pet("Tapirus terrestris", "https://en.wikipedia.org/wiki/Tapir"))
      } else {
        IO.fail("Unknown pet id")
      }
    }
    .toRoutes

  // Same as above, but combining endpoint description with server logic:
  val petServerEndpoint: ZServerEndpoint[Any, Int, String, Pet] = petEndpoint.zServerLogic {
    petId =>
      if (petId == 35) {
        UIO(Pet("Tapirus terrestris", "https://en.wikipedia.org/wiki/Tapir"))
      } else {
        IO.fail("Unknown pet id")
      }
  }
  val petServerRoutes: HttpRoutes[RIO[Clock, *]] = // with Blocking
    ZHttp4sServerInterpreter().from(petServerEndpoint).toRoutes

  //

  val yaml: String = {
    import sttp.tapir.docs.openapi.OpenAPIDocsInterpreter
    import sttp.tapir.openapi.circe.yaml._
    OpenAPIDocsInterpreter().toOpenAPI(petEndpoint, "Our pets", "1.0").toYaml
  }

  type AppEnvironment = UserRegService
    with DBTransactor
    with Clock
    with Blocking
    with DbConfiguration
    with Console

  type AppTask[A] = RIO[AppEnvironment, A]

  val yaml2: String = {
    import sttp.tapir.docs.openapi.OpenAPIDocsInterpreter
    import sttp.tapir.openapi.circe.yaml._
    OpenAPIDocsInterpreter()
      .toOpenAPI(new UserApi[AppEnvironment].registerUserEndpoint, "Our pets2", "1.0")
      //.toOpenAPI(petEndpoint, "Our pets", "1.0")
      .toYaml
  }

  //http://localhost:8080/docs/docs.yaml
  //http://localhost:8080/docs/index.html
//  val swaggerRoutes: HttpRoutes[RIO[Clock, *]] = // with Blocking
//    new SwaggerHttp4s(yaml2).routes[RIO[Clock, *]] //with Blocking

  val swaggerRoutes2: HttpRoutes[AppTask] = // with Blocking
    new SwaggerHttp4s(yaml2).routes[AppTask] //with Blocking

  //ZHttp4sServerInterpreter().from(SwaggerUI[RIO[Clock with Blocking, *]](yaml)).toRoutes

  // Starting the server
  //https://stackoverflow.com/questions/46343040/generate-swagger-openapi-specification-from-scala-source-code-http4s
  //https://github.com/avoronets84/scala-dev-mooc-2021-03/blob/master/src/main/scala/module4/phoneBook/Server.scala
//  val serve: ZIO[ZEnv, Throwable, Unit] =
//    ZIO.runtime[ZEnv].flatMap {
//      implicit runtime => // This is needed to derive cats-effect instances for that are needed by http4s
//        BlazeServerBuilder[RIO[Clock, *]](runtime.platform.executor.asEC) // with Blocking
//          .bindHttp(8080, "localhost")
//          .withHttpApp(Router("/" -> petRoutes, "/" -> swaggerRoutes).orNotFound) //<+>
//          .serve
//          .compile
//          .drain

//  val appEnvironment =
//    Configuration.live >+> Blocking.live >+> DBTransactor.live >+> LiquibaseService.liquibaseLayer ++
//      PhoneRecordRepository.live >+> PhoneBookService.live ++ LiquibaseService.live
  val appEnvironment =
    dbConfigLive >+> Blocking.live >+> DbHikariTransactor.live >+> userServiceLive

  //val httApp = Router[AppTask]("/phoneBook" -> new PhoneBookAPI().route).orNotFound
  val httApp = Router[AppTask](
    "/" -> new UserApi[AppEnvironment]().registerUserRoutes,
    "/" -> swaggerRoutes2
  ).orNotFound
//  val httApp =
//    Router[AppTask]("/" -> new UserApi().registerUserRoutes).orNotFound //[AppEnvironment]

  val serve = //: ZIO[AppEnvironment, Throwable, Unit]
    ZIO.runtime[AppEnvironment].flatMap {
      implicit runtime => // This is needed to derive cats-effect instances for that are needed by http4s
        BlazeServerBuilder[AppTask](
          runtime.platform.executor.asEC
        ) // with Blocking
          .bindHttp(8080, "localhost")
          .withHttpApp(httApp) //<+>
          .serve
          .compile[AppTask, AppTask, CatsExitCode]
          .drain
    }
  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    serve.provideSomeLayer[zio.ZEnv](appEnvironment).exitCode
  //override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = serve.exitCode
}
