package co.uk.foodgen.domain

import zio.json.JsonEncoder
import zio.prelude.Equal
import zio.schema.Schema

object Entity:
  opaque type EntityId[A] = Long

  extension [A](id: EntityId[A]) def value: Long = id

  object EntityId:
    def apply[A](v: Long): EntityId[A] = v

  given [A]: Schema[EntityId[A]] = Schema.primitive[Long]
  given [A]: JsonEncoder[EntityId[A]] = JsonEncoder.long
  given [A]: Equal[EntityId[A]] = Equal.default[Long]
end Entity
