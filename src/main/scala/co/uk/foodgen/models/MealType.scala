package co.uk.foodgen.models

import doobie.postgres.implicits.pgEnumString
import doobie.util.meta.Meta
import io.circe.{Codec, Decoder, Encoder}
import sttp.tapir.Schema

enum MealType extends Enum[MealType]:
  case Breakfast, Lunch, Dinner

  override def toString: String = productPrefix.toLowerCase

object MealType:
  given Meta[MealType] = pgEnumString(
    "meal_type",
    str => MealType.valueOf(str.capitalize),
    _.toString
  )
  given Codec[MealType] = Codec.from(
    Decoder.decodeString.map(str => MealType.valueOf(str.capitalize)),
    Encoder.encodeString.contramap(_.toString)
  )
  given Schema[MealType] = Schema.derivedEnumeration.defaultStringBased
