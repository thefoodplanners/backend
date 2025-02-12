package co.uk.foodgen.models

import cats.Eq
import cats.implicits.catsSyntaxEq
import doobie.util.meta.Meta
import neotype.*
import sttp.tapir.{Codec, CodecFormat, Schema}

trait HasId:
  type Id = Id.Type
  object Id extends Newtype[Int]

  given Eq[Id] = Eq.instance { (x, y) => x.unwrap === y.unwrap }
  given Codec[String, Id, CodecFormat.TextPlain] = Codec.int.map(Id.apply)(_.unwrap)
  given Meta[Id] = Meta[Int].timap(Id.apply)(_.unwrap)
  given Schema[Id] = Schema.schemaForInt.as[Id]

end HasId
