package ru.otus.sales.leads.generator.services.cores.leads
package validators

import ru.otus.sales.leads.generator.inf.common.extensions.StringOpts
import ru.otus.sales.leads.generator.services.cores.leads.models.{LeadError, LeadInfo}
import zio.{Has, IO, Ref, ULayer, ZIO, ZLayer}

object LeadValidator {
  type LeadValidator = Has[Service]

  trait Service {
    def validate(info: LeadInfo): IO[::[LeadError], Unit]
  }

  class ServiceImpl extends Service {
    override def validate(info: LeadInfo): IO[::[LeadError], Unit] = for {
      ref <- Ref.make(List.empty[LeadError])
      _ <- (if (info.fullName.isNullOrEmpty || info.fullName.length > 256)
              ref.update(LeadError.EmptyFullname :: _)
            else ZIO.none) *>
        (if (info.phone.isNullOrEmpty || info.phone.length > 128)
           ref.update(LeadError.EmptyPhone :: _)
         else ZIO.none) *>
        (if (info.price <= 0) ref.update(LeadError.BadPrice(info.price) :: _) else ZIO.none)
      errors <- ref.get
      ret <- if (errors.isEmpty) IO.succeed(true) else IO.fail(::(errors.head, errors.tail))
    } yield ()
  }

  val live: ULayer[LeadValidator] = ZLayer.succeed(new ServiceImpl)

}
