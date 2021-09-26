package ru.otus.sales.leads.generator.apps.api

import ru.otus.sales.leads.generator.data.domain.entities.BotId
import ru.otus.sales.leads.generator.services.cores.leads.models.LeadInfo
import sttp.tapir.Codec.stringCodec
import sttp.tapir.CodecFormat.TextPlain
import sttp.tapir.{Codec, Schema, SchemaType}

package object codecs {
//  implicit val botSchema: Schema[BotId] = Schema(
//    SchemaType.SInteger()
//  ) //.validate(Validator.min(1).contramap(_.v))
//
//  implicit val botCodec: Codec[String, BotId, TextPlain] =
//    stringCodec[BotId](s => BotId(s.toInt)).schema(botSchema)

//  implicit val iMinusT: sttp.tapir.typelevel.ParamSubtract.Aux[
//    ru.otus.sales.leads.generator.data.domain.entities.BotId,
//    ru.otus.sales.leads.generator.data.domain.entities.BotId,
//    LeadInfo]()
}
