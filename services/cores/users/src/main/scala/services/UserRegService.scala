package ru.otus.sales.leads.generator.services.cores.users
package services

import cats.effect.{Async, ConcurrentEffect, LiftIO}
import ru.otus.sales.leads.generator.data.domain.entities.{BotId, Name, Surname, User}
import ru.otus.sales.leads.generator.inf.repository.transactor.{
  DBTransactor,
  doobieContext,
  makeTransactor
}
import ru.otus.sales.leads.generator.services.cores.users.models.{UserReg, UserRegError}
import ru.otus.sales.leads.generator.services.cores.users.repositories.UserRepository
import zio.{Has, IO, Task, UIO, URLayer, ZIO, ZLayer, clock}
import doobie.free.resultset
import doobie.quill.DoobieContext
import io.getquill.{CompositeNamingStrategy2, Escape, Literal}
import zio.interop.catz._
import cats.effect.implicits._
import ru.otus.sales.leads.generator.services.cores.users.repositories.UserRepository.UserRepository
import zio.clock.Clock
import zio.duration.durationInt

object UserRegService {

  type UserRegService = Has[Service]

  trait Service {
    def register(info: UserReg): ZIO[DBTransactor, UserRegError, Unit]
  }

  class ServiceImpl(repo: UserRepository.Service) extends Service {
    import doobie.implicits._

    override def register(info: UserReg): ZIO[DBTransactor, UserRegError, Unit] = for {
      trans <- makeTransactor
      botId = BotId(info.bot.value)
      _ <- repo
        .getByBot(botId)
        .transact(trans)
        .some
        .foldM(
          {
            case None =>
              repo
                .create(User(None, info.name.toString, info.surname.toString, botId))
                .transact(trans)
                .orElseFail(UserRegError.LostConnection)
            case Some(_) => ZIO.fail(UserRegError.LostConnection)

          },
          user => ZIO.fail(UserRegError.AlreadyRegistered(user.name))
        )
    } yield ()

  }

  val live: URLayer[UserRepository, UserRegService] =
    ZLayer.fromService[UserRepository.Service, Service](new ServiceImpl(_))

  // front-facing API, aka "accessor"
  def register(info: UserReg): ZIO[UserRegService with DBTransactor, UserRegError, Unit] =
    ZIO.accessM(_.get.register(info))

  //    override def register(info: UserReg): ZIO[DBTransactor, UserRegError, Boolean] = for {
  //      trans <- makeTransactor
  //      botId = BotId(info.bot.value)
  //      name: Name = info.name.toString
  //      surname: Surname = info.surname.toString
  ////      user <- repo
  ////        .getByBot(botId)
  ////        .transact(trans)
  ////        .some
  ////        .flipWith()
  ////        .some
  ////        .c
  ////        .foldM()
  ////        .mapError(_ => UserRegError.LostConnection)
  //      //user <- repo.getByBot(botId).transact(trans).some.c.foldM().mapError(_ => UserRegError.LostConnection)
  //      //_ <-
  //      //user <- repo.getByBot(botId).transact(trans).mapError(_ => UserRegError.LostConnection)
  //      user <- repo.getByBot(botId).transact(trans).mapError(_ => UserRegError.LostConnection)
  //      _ <-
  //        if (!user.isEmpty) ZIO.fail(UserRegError.AlreadyRegistered(user.get.name))
  //        else
  //          repo
  //            .create(User(None, name, surname, botId))
  //            .transact(trans)
  //            .mapError(_ => UserRegError.LostConnection)
  //    } yield true

  //    val dc: DoobieContext.Postgres[CompositeNamingStrategy2[Escape.type, Literal.type]] =
  //      doobieContext //DBTransactor.
  //    import dc._
  //
  //    type ClockTask[A] = ZIO[Any with Clock, UserRegError, A]
  //
  //    implicit val rts = zio.Runtime.default
  //
  ////    def getCE =
  ////      ZIO.runtime.map { implicit r => //: Runtime[Any]
  ////        val F: ConcurrentEffect[Task] = implicitly
  ////        F.toIO(Task.fail(new Exception("WTF")))
  ////      }
  //
  //    override def register(info: UserReg): ZIO[DBTransactor, UserRegError, Boolean] = for {
  //      trans <- makeTransactor
  //      botId = BotId(info.bot.value)
  //      name: Name = info.name.toString
  //      surname: Surname = info.surname.toString
  //      res <- (for {
  //        user <- repo.getByBot(botId) //.transact(trans).mapError(_ => UserRegError.LostConnection)
  //        _ <-
  //          if (!user.isEmpty) {
  //            //ZIO.runtime.map { implicit r: Runtime[Any] =>
  //            // val F: ConcurrentEffect[Task] = implicitly
  //            ConcurrentEffect[Task]
  //              //.toIO(clock.sleep(500.milliseconds))
  //              .toIO(Task.fail(new Exception("WTF")))
  //              .to[Result]
  //            //}
  //          }
  //          //            Task.concurrentEffectWith { implicit CE =>
  ////              CE.start(Task(println("Started task")))
  ////            }
  //
  //          //ConcurrentEffect[ClockTask].toIO(clock.sleep(500.milliseconds)).to[Result]
  //          //LiftIO.liftK[Result]()
  ////            Async[Result].liftIO(
  ////              ConcurrentEffect[ClockTask].toIO(
  ////                ZIO.fail(UserRegError.AlreadyRegistered(user.get.name)))
  ////            )
  ////            Async[Result].liftIO(
  ////              ZIO.fail(UserRegError.AlreadyRegistered(user.get.name)).toIO //.toIO //.to.toIO
  ////            ) //ConnectionIO
  //          //ZIO.fail(UserRegError.AlreadyRegistered(user.get.name)).to[Result]
  //          else
  //            repo
  //              .create(User(None, name, surname, botId))
  //        //.transact(trans)
  //        //.mapError(_ => UserRegError.LostConnection)
  //      } yield true).transact(trans).mapError(_ => UserRegError.LostConnection)
  //    } yield res
  //  }

}
