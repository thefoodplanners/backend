package co.uk.foodgen.models

import doobie.postgres.implicits.pgEnumString
import doobie.util.meta.Meta
import io.circe.generic.semiauto.deriveCodec
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
  given Codec[MealType] = deriveCodec
  given Schema[MealType] = Schema.derivedEnumeration.defaultStringBased
