package co.uk.foodgen.domain

import io.getquill.MappedEncoding
import zio.json.JsonEncoder

enum MealType derives JsonEncoder:
  case Breakfast, Lunch, Dinner

object MealType:
  given MappedEncoding[MealType, String] = MappedEncoding(_.productPrefix.toLowerCase)
  given MappedEncoding[String, MealType] = MappedEncoding(s => MealType.valueOf(s.toLowerCase.capitalize))
