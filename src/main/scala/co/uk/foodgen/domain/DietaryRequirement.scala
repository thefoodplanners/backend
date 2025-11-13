package co.uk.foodgen.domain

import io.getquill.MappedEncoding
import zio.json.JsonEncoder
import zio.schema.{Schema, derived}

enum DietaryRequirement derives Schema, JsonEncoder:
  case Vegan, Vegetarian, Halal, Kosher

object DietaryRequirement:
  given MappedEncoding[DietaryRequirement, String] = MappedEncoding(_.productPrefix.toLowerCase)
  given MappedEncoding[String, DietaryRequirement] = MappedEncoding(s => valueOf(s.toLowerCase.capitalize))
