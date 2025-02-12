package co.uk.foodgen.models

import doobie.postgres.implicits.unliftedStringArrayType
import doobie.util.meta.Meta
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import sttp.tapir.Schema

enum DietaryRequirement extends Enum[DietaryRequirement]:
  case Vegan, Vegetarian, Halal, Kosher

  override def toString: String = productPrefix.toLowerCase

object DietaryRequirement:
  given Meta[List[DietaryRequirement]] = Meta[Array[String]].timap(
    _.map(str => DietaryRequirement.valueOf(str.capitalize)).toList
  )(_.map(_.toString).toArray)
  given Codec[DietaryRequirement] = deriveCodec
  given Schema[DietaryRequirement] = Schema.derivedEnumeration.defaultStringBased
